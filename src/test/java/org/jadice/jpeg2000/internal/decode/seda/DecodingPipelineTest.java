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
package org.jadice.jpeg2000.internal.decode.seda;

import java.io.InputStream;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.JPEG2000RenderingTest;
import org.jadice.jpeg2000.internal.codestream.BlockContribution;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.Receiver;
import org.jadice.jpeg2000.internal.decode.push.stage.ForwardReading;
import org.jadice.jpeg2000.internal.decode.push.stage.PacketBodyReading;
import org.jadice.jpeg2000.internal.decode.seda.BlockContributionStage;
import org.jadice.jpeg2000.internal.decode.seda.Consumer;
import org.jadice.jpeg2000.internal.decode.seda.ExecutionStrategy;
import org.jadice.jpeg2000.internal.decode.seda.ForwardReadingStage;
import org.jadice.jpeg2000.internal.decode.seda.InverseDWTStage;
import org.jadice.jpeg2000.internal.decode.seda.InverseMCTStage;
import org.jadice.jpeg2000.internal.decode.seda.InverseQuantizationStage;
import org.jadice.jpeg2000.internal.decode.seda.LevelShiftStage;
import org.jadice.jpeg2000.internal.decode.seda.Pipeline;
import org.jadice.jpeg2000.internal.decode.seda.Producer;
import org.jadice.jpeg2000.internal.decode.seda.StageByStageBufferedExecutionStrategy;
import org.jadice.jpeg2000.internal.decode.seda.StageConfigurer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.levigo.jadice.document.io.IOUtils;
import com.levigo.jadice.document.io.SeekableInputStream;

public class DecodingPipelineTest {

  @Test
  @Disabled
  public void fooTest() throws Exception {
    final String resourcePath = "/codestreams/profile0/p0_01.j2k";

    final Class<JPEG2000RenderingTest> cls = JPEG2000RenderingTest.class;
    final InputStream inputStream = cls.getResourceAsStream(resourcePath);
    final SeekableInputStream source = IOUtils.wrap(inputStream);

    final Codestream codestream = new Codestream(source);
    codestream.init();

    new ForwardReading(new PacketBodyReading(new Receiver<BlockContribution>() {
      @Override
      public void receive(BlockContribution pushable, DecoderParameters parameters) throws JPEG2000Exception {
        System.out.println(pushable);
      }
    })).receive(codestream, new DecoderParameters());
  }

  @Test
  @Disabled
  public void testDecodingPipeline() throws Exception {
    final String resourcePath = "/codestreams/profile0/p0_01.j2k";

    final Class<JPEG2000RenderingTest> cls = JPEG2000RenderingTest.class;
    final InputStream inputStream = cls.getResourceAsStream(resourcePath);
    final SeekableInputStream source = IOUtils.wrap(inputStream);

    final Codestream codestream = new Codestream(source);
    codestream.init();

    final DecoderParameters parameters = new DecoderParameters();
    parameters.region = codestream.region().absolute();

    final Pipeline<JPEG2000Exception> pipeline = Pipeline.startWith(new Producer<Codestream, JPEG2000Exception>() {
      @Override
      public void run(Consumer<Codestream, JPEG2000Exception> next) throws JPEG2000Exception {
        next.consume(codestream);
      }
    })//
    .append(new ForwardReadingStage())//
    .append(new BlockContributionStage())//
    .append(new InverseQuantizationStage())//
    .append(new InverseDWTStage())//
    .append(new LevelShiftStage())//
    .append(new InverseMCTStage())//
    .finishWith(new Consumer<Object, JPEG2000Exception>() {
      @Override
      public void consume(Object it) {
        System.out.println("Got: " + it);
      }
    });

    StageConfigurer.configure(pipeline, parameters);

    final ExecutionStrategy xs = new StageByStageBufferedExecutionStrategy();
    xs.execute(pipeline);
  }
}
