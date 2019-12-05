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
package com.levigo.jadice.format.jpeg2000.internal.tier2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.Tests;
import com.levigo.jadice.format.jpeg2000.internal.codestream.BlockContribution;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codeword;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestreams;
import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.ForwardTilePartReading;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.PacketBodyReading;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.TilePartBodyReading;

public abstract class PacketHeaderReaderTestBase {

  protected Codestream codestream;
  protected PacketHeaderTest testStage;

  @Before
  public void setup() throws IOException, JPEG2000Exception {
    createTestStage();
    createCodestream();
  }

  private void createTestStage() {
    final String traceFileResourcePath = getTraceFileResourcePath();
    try {
      final SeekableInputStream inputStream = Tests.openResource(traceFileResourcePath);
      final Reader json = new BufferedReader(new InputStreamReader(inputStream));
      final Gson gson = new Gson();
      final Tier2TraceData traceData = gson.fromJson(json, Tier2TraceData.class);

      final Receiver<BlockContribution> nirvana = new Receiver<BlockContribution>() {
        @Override
        public void receive(BlockContribution pushable, DecoderParameters parameters) throws JPEG2000Exception {
          // nothing to do. let the block contribution end up in nirvana.
        }
      };

      testStage = new PacketHeaderTest(traceData, new PacketBodyReading(nirvana));
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  private void createCodestream() throws IOException, JPEG2000Exception {
    final String subjectPath = getSubjectPath();
    final SeekableInputStream source = Tests.openResource(subjectPath);
    final SeekableInputStream codestreamSource = Codestreams.createCodestreamSource(source);
    codestream = new Codestream(codestreamSource);
    codestream.init();
  }

  protected abstract String getTraceFileResourcePath();
  protected abstract String getSubjectPath();

  @Test
  public void runTest() throws JPEG2000Exception {
    final DecoderParameters parameters = new DecoderParameters();
    parameters.validate = false;
    parameters.region = codestream.region().absolute();

    final ForwardTilePartReading pipe = new ForwardTilePartReading(new TilePartBodyReading(testStage));
    pipe.receive(codestream, parameters);

    assertTrue(testStage.satisfied);
  }

  private class PacketHeaderTest implements Receiver<PacketHeader> {

    private final Iterator<TracePacket> tracePacketIterator;
    private final PacketBodyReading packetBodyReading;

    public boolean satisfied = false;

    public PacketHeaderTest(Tier2TraceData traceData, PacketBodyReading packetBodyReading) {
      this.packetBodyReading = packetBodyReading;
      final List<TracePacket> tracePackets = Arrays.asList(traceData.packets);
      tracePacketIterator = tracePackets.iterator();
    }

    @Override
    public void receive(PacketHeader packetHeader, DecoderParameters parameters) throws JPEG2000Exception {
      assertFalse("test satisfied", satisfied);
      assertTrue("trace packet iterator has next", tracePacketIterator.hasNext());

      final TracePacket tracePacket = tracePacketIterator.next();

      assertEquals("comp", tracePacket.c, packetHeader.comp);
      assertEquals("res", tracePacket.r, packetHeader.res);
      assertEquals("layer", tracePacket.l, packetHeader.layer);

      assertEquals("empty", tracePacket.empty, packetHeader.empty);

      final String packetIdentifier = "c=" + tracePacket.c + ", r=" + tracePacket.r + ", l=" + tracePacket.l;

      if (!packetHeader.empty) {
        final TraceBlockContribution[] expectedContributions = tracePacket.blocks;
        assertNotNull(packetIdentifier, expectedContributions);

        final List<BlockContribution> actualContributions = packetHeader.blockContributions;
        final Iterator<BlockContribution> actualContributionIterator = actualContributions.iterator();

        for (int i = 0; i < expectedContributions.length; i++) {
          final TraceBlockContribution expectedContribution = expectedContributions[i];
          assertTrue(packetIdentifier, actualContributionIterator.hasNext());
          final BlockContribution actualContribution = actualContributionIterator.next();

          if (expectedContribution.included) {

            final TraceCodeword[] expectedCodewords = expectedContribution.codewords;
            final List<Codeword> actualCodewords = actualContribution.codewords;

            for (int j = 0; j < expectedCodewords.length; j++) {

              if (j >= actualCodewords.size()) {
                fail((j + 1) + ". segment missing for " + (i + 1) + ". contribution of " + packetHeader);
              }

              final TraceCodeword expectedCodeword = expectedCodewords[j];
              final Codeword actualCodeword = actualCodewords.get(j);

              assertEquals(packetIdentifier + " contrib beta", expectedContribution.beta,
                  actualContribution.block.beta);
              assertEquals(packetIdentifier + " contrib passes", expectedContribution.passes,
                  actualContribution.passes);

              if (expectedContribution.msbs != -1) {
                assertEquals(packetIdentifier + " contrib msbs", expectedContribution.msbs,
                    actualContribution.block.missingMSBs);
              }

              assertEquals(packetIdentifier + " codeword passes", expectedCodeword.passes, actualCodeword.passes);
              assertEquals(packetIdentifier + " codeword length", expectedCodeword.length, actualCodeword.numBytes);

            }
          }
        }
      }

      if (!tracePacketIterator.hasNext()) {
        testStage.satisfied = true;
      }

      packetBodyReading.receive(packetHeader, parameters);
    }
  }
}
