/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jadice.jpeg2000.internal.tier1;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.hamcrest.MatcherAssert;
import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.Tests;
import org.jadice.jpeg2000.internal.codestream.BlockContribution;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.DecodeTask;
import org.jadice.jpeg2000.internal.decode.push.Receiver;
import org.jadice.jpeg2000.internal.decode.push.stage.BlockContributionStage;
import org.jadice.jpeg2000.internal.decode.push.stage.ForwardTilePartReading;
import org.jadice.jpeg2000.internal.decode.push.stage.PacketBodyReading;
import org.jadice.jpeg2000.internal.decode.push.stage.TilePartBodyReading;
import org.jadice.jpeg2000.internal.image.BandPrecinct;
import org.jadice.jpeg2000.internal.image.CodeBlock;
import org.jadice.jpeg2000.internal.image.Component;
import org.jadice.jpeg2000.internal.image.Resolution;
import org.jadice.jpeg2000.internal.image.Subband;
import org.jadice.jpeg2000.internal.image.Tile;
import org.jadice.jpeg2000.internal.image.TileComponent;
import org.jadice.jpeg2000.internal.tier1.BlockContributionTask;
import org.jadice.jpeg2000.internal.tier1.MQDecoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.levigo.jadice.document.io.SeekableInputStream;

public abstract class BlockDecodingSpyTestBase {

  public static class BlockDecodingTrace {
    public ExpectedBlockData[] data;
  }
  
  public static class ExpectedBlockData {
    public String identifier;
    public int[] decisions;
    public CtxExpectation[] contexts;
    public int[][] samples;
    public MQState[] mqStates;
    
    public int passIndex;
    
    public MQDecoderSpy mqDecoder;
  }
  
  public static class MQState {
    public int icx;
    public int a;
    public int b;
    public long c;
    public int cT;
  }
  
  private static class MyBlockContributionStage extends BlockContributionStage {

    private final BlockDecodingTrace trace;

    private int testedBlockCount;

    public MyBlockContributionStage(Receiver<CodeBlock> nextStage, BlockDecodingTrace trace) {
      super(nextStage);
      this.trace = trace;
    }

    @Override
    public DecodeTask<CodeBlock> createTask(BlockContribution blockContribution) {
      final CodeBlock block = blockContribution.block;
      final BandPrecinct precinct = block.bandPrecinct;
      final Subband subband = precinct.subband;
      final Resolution resolution = subband.resolution;
      final TileComponent tileComp = resolution.tileComp;
      final Tile tile = tileComp.tile;
      final Component comp = tileComp.comp;

      final String identifier =
          "t" + tile.idx + 
          "c" + comp.idx + 
          "r" + resolution.resLevel + 
          "b" + subband.type.id + 
          "p" + precinct.precinct.idx + 
          "cb" + block.indices.x + "," + block.indices.y;

      for (ExpectedBlockData data : trace.data) {
        if (data.identifier.equalsIgnoreCase(identifier)) {
          testedBlockCount++;
          return new MyBlockContributionTask(blockContribution, data);
        }
      }

      return super.createTask(blockContribution);
    }

  }
  
  private static class MyBlockContributionTask extends BlockContributionTask {

    private final ExpectedBlockData trace;

    public MyBlockContributionTask(BlockContribution blockContribution, ExpectedBlockData trace) {
      super(blockContribution);
      this.trace = trace;
    }

    @Override
    protected void inspect(CodeBlock block) {
      if (trace.samples != null) {
        final int[] expectedSamples = trace.samples[trace.passIndex];
        final int[] actualSamples = block.state.sampleBuffer;
        Assertions.assertArrayEquals(expectedSamples, actualSamples, "samples");
      }

      trace.passIndex++;
    }

    @Override
    protected MQDecoder createMQDecoder(SeekableInputStream input) throws IOException {
      final MQDecoder mqDecoder = super.createMQDecoder(input);
      final MQDecoderSpy spy = getSpy();
      spy.setTarget(mqDecoder);
      return spy;
    }

    public MQDecoderSpy getSpy() {
      if (trace.mqDecoder == null) {
        trace.mqDecoder = new MQDecoderSpy(trace.decisions, trace.contexts, trace.mqStates);
      }
      return trace.mqDecoder;
    }
  }
  
  private static Gson GSON;

  @BeforeAll
  public static void setupGson() {
    final GsonBuilder gson = new GsonBuilder();
    gson.registerTypeAdapter(CtxExpectation.class, new CtxExpectationAdapter());
    GSON = gson.create();  
  }

  protected BlockDecodingTrace trace;
  protected Codestream codestream;

  @BeforeEach
  public void readTraceData() {
    final String filePath = getTraceFileResourcePath();
    try {
      final SeekableInputStream inputStream = Tests.openResource(filePath);
      final Reader json = new BufferedReader(new InputStreamReader(inputStream));
      trace = GSON.fromJson(json, BlockDecodingTrace.class);
    } catch (IOException e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }
  
  @BeforeEach
  public void readCodestream() throws IOException, JPEG2000Exception {
    final String filePath = getInputFileResourcePath();
    final SeekableInputStream inputStream = Tests.openResource(filePath);
    codestream = new Codestream(inputStream);
    codestream.init();
  }

  protected abstract String getInputFileResourcePath();

  protected abstract String getTraceFileResourcePath();
  
  @Test
  public void runTest() throws JPEG2000Exception {
    final MyBlockContributionStage blockContribution = new MyBlockContributionStage(new Receiver<CodeBlock>() {
      @Override
      public void receive(CodeBlock pushable, DecoderParameters parameters) throws JPEG2000Exception {
       // ignore 
      }
    }, trace);
    final PacketBodyReading packetBodyReading = new PacketBodyReading(blockContribution);
    final TilePartBodyReading tilePartBodyReading = new TilePartBodyReading(packetBodyReading);
    final ForwardTilePartReading forwardTilePartReading = new ForwardTilePartReading(tilePartBodyReading);

    final DecoderParameters parameters = new DecoderParameters();
    parameters.validate = false;
    parameters.region = codestream.region().absolute();

    forwardTilePartReading.receive(codestream, parameters);

    MatcherAssert.assertThat(blockContribution.testedBlockCount, is(equalTo(trace.data.length)));
  }
}
