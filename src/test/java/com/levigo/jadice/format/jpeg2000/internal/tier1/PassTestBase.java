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
package com.levigo.jadice.format.jpeg2000.internal.tier1;

import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.Tests;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlockMock;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlockState;
import com.levigo.jadice.format.jpeg2000.internal.image.DefaultGridRegion;
import com.levigo.jadice.format.jpeg2000.internal.image.SubbandType;
import org.jadice.util.log.qualified.QualifiedLogger;

public abstract class PassTestBase {

  private static final QualifiedLogger LOGGER = getQualifiedLogger(PassTestBase.class);
  
  private static final Gson GSON;
  static {
    final GsonBuilder gson = new GsonBuilder();
    gson.registerTypeAdapter(Pass.class, new PassAdapter());
    gson.registerTypeAdapter(CtxExpectation.class, new CtxExpectationAdapter());
    GSON = gson.create();
  }

  protected PassTestExpectation traceData;
  
  @Before
  public void setup() {
    final String filePath = getTraceFileResourcePath();
    try {
      final SeekableInputStream inputStream = Tests.openResource(filePath);
      final Reader json = new BufferedReader(new InputStreamReader(inputStream));
      traceData = GSON.fromJson(json, PassTestExpectation.class);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
  
  protected abstract String getTraceFileResourcePath();
  
  @Test
  public void tracingTest() throws JPEG2000Exception, IOException {
    final PassTestExpectation.TraceTile[] tiles = traceData.tiles;
    for (PassTestExpectation.TraceTile tile : tiles) {
      final PassTestExpectation.TraceComp[] comps = tile.comps;
      for (PassTestExpectation.TraceComp comp : comps) {
        final PassTestExpectation.TraceResolution[] resolutions = comp.resolutions;
        for (PassTestExpectation.TraceResolution resolution : resolutions) {
          final PassTestExpectation.TraceSubband[] subbands = resolution.subbands;
          for (PassTestExpectation.TraceSubband subband : subbands) {
            final SubbandType type = subband.type;
            final PassTestExpectation.TracePrecinct[] precincts = subband.precincts;
            for (PassTestExpectation.TracePrecinct precinct : precincts) {
              final PassTestExpectation.TraceBlock[] blocks = precinct.blocks;
              for (PassTestExpectation.TraceBlock block : blocks) {

                LOGGER.info("t=" + tile.id + ", c=" + comp.id + ", r=" + resolution.id + ", b=" + subband.type + ", bp="
                    + precinct.id);

                final CodeBlockMock codeBlock = new CodeBlockMock();
                codeBlock.setGridRegion(new DefaultGridRegion(block.region));
                codeBlock.indices = block.indices;
                codeBlock.stripeHeights = block.stripes;
                codeBlock.numStripes = block.stripes.length;
                codeBlock.segmentationMarks = block.segmentationSymbols;
                codeBlock.causalCtx = block.causalCtx;
                codeBlock.state = new CodeBlockState();
                codeBlock.state.sampleBuffer = new int[(codeBlock.numStripes << 2) * block.region.size.x];
                codeBlock.state.bitplane = block.bitplane;
                codeBlock.state.ctxBuffer = block.ctxBuffer;

                final int width = block.region.size.x;

                final Pass[] passes = block.passes;
                for (int i = 0; i < passes.length; i++) {
                  final Pass pass = passes[i];

                  final String passName = pass.getClass().getSimpleName();
                  LOGGER.info("Pass (" + i + "): " + passName);

                  final int[] predefinedDecisions = block.decisionTrace[i];
                  final int[] expectedSamples = block.sampleTrace[i];
                  final int[] expectedCtx = block.ctxTrace[i];
                  final CtxExpectation[] passCtxExpectations = block.stateTrace[i];

                  final PrepopulatedMQDecoder mq = new PrepopulatedMQDecoder(predefinedDecisions, passCtxExpectations);
                  codeBlock.mqCtx = new ContextContainer();

                  final int bitplane = codeBlock.state.bitplane;
                  final boolean errorFound = pass.run(mq, codeBlock, type, bitplane, width, block.ctxRowGap);

                  final String passIdent = passName + ", bitplane " + bitplane + ", passIdx " + i;
                  if (errorFound) {
                    fail("Error found in " + passIdent);
                  }

                  assertEquals("state array length", mq.ctxExpectations.length, mq.expectationIdx);
                  assertEquals("decision array length", mq.decisions.length, mq.decisionIdx);

                  assertArrayEquals("Samples differed for " + passIdent, expectedSamples, codeBlock.state.sampleBuffer);

                  if (expectedCtx != null && expectedCtx.length > 0) {
                    // Eliminate context information in padding
                    final int ctxBufferLength = codeBlock.state.ctxBuffer.length;
                    final int[] actualCtx = Arrays.copyOf(codeBlock.state.ctxBuffer, ctxBufferLength);
                    Arrays.fill(actualCtx, 0, block.ctxRowGap - 1, 0);
                    Arrays.fill(actualCtx, ctxBufferLength - block.ctxRowGap, ctxBufferLength - 1, 0);

                    assertArrayEquals("Contexts differed for " + passIdent, expectedCtx, actualCtx);
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  
}
