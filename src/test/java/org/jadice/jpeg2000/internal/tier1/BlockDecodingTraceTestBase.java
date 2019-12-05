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

import java.io.IOException;
import java.util.Arrays;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.image.CodeBlockMock;
import org.jadice.jpeg2000.internal.image.Pair;
import org.jadice.jpeg2000.internal.image.Region;
import org.jadice.jpeg2000.internal.image.SubbandType;
import org.jadice.jpeg2000.internal.tier1.ContextContainer;
import org.jadice.jpeg2000.internal.tier1.Pass;
import org.jadice.util.log.Logger;
import org.jadice.util.log.LoggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class BlockDecodingTraceTestBase {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());
  
  protected Pair blockIndices;
  protected Region blockRegion;
  protected int blockWidth;
  protected CodeBlockMock block;
  protected int ctxRowGap;
  protected SubbandType subbandType;
  protected Pass[] passes;

  protected int[][] decisions;
  protected int[][] expectedSamples;
  protected int[][] expectedCtx;
  protected CtxExpectation[][] ctxExpectations;

  @BeforeEach
  public void setup() {
    initCodeBlock();

    passes = createPasses();
    decisions = createDecisions();
    expectedSamples = createExpectedSamples();
    expectedCtx = createExpectedContexts();
    ctxExpectations = createCtxExpectations();
  }

  protected abstract void initCodeBlock();

  protected abstract Pass[] createPasses();

  protected abstract int[][] createDecisions();

  protected abstract int[][] createExpectedSamples();

  protected abstract int[][] createExpectedContexts();

  protected abstract CtxExpectation[][] createCtxExpectations();

  protected static CtxExpectation ctx(int ctxBase, int d) {
    return new CtxExpectation(ctxBase + d);
  }

  protected static CtxExpectation ctx(int ctx) {
    return new CtxExpectation(ctx);
  }
  
  @AfterEach
  public void cleanup() {
    passes = null;
    decisions = null;
    expectedSamples = null;
    expectedCtx = null;
    ctxExpectations = null;
    block.state = null;
    block = null;
  }
  
  @Test
  public void run() throws JPEG2000Exception, IOException {
    for (int i = 0; i < passes.length; i++) {
      final Pass pass = passes[i];
      
      logger.info("Pass (" + i + "): " + pass.getClass().getSimpleName());

      final int[] passDecisions = decisions[i];
      final int[] passResultSamples = expectedSamples[i];
      final int[] passResultCtx = expectedCtx[i];
      final CtxExpectation[] passCtxExpectations = ctxExpectations[i];

      final PrepopulatedMQDecoder mq = new PrepopulatedMQDecoder(passDecisions, passCtxExpectations);
      block.mqCtx = new ContextContainer();

      final boolean errorFound = pass.run(mq, block, subbandType, block.state.bitplane, blockWidth, ctxRowGap);

      final String passIdent = pass.getClass().getSimpleName() + ", bitplane " + block.state.bitplane + ", passIdx " + i;
      if (errorFound) {
        Assertions.fail("Error found in " + passIdent);
      }

      Assertions.assertEquals(mq.ctxExpectations.length, mq.expectationIdx);
      Assertions.assertEquals(mq.decisions.length, mq.decisionIdx);
      
      Assertions.assertArrayEquals(passResultSamples, block.state.sampleBuffer, "Samples differed for " + passIdent);

      if (passResultCtx != null && passResultCtx.length > 0) {
        // Eliminate context information in padding
        final int ctxBufferLength = block.state.ctxBuffer.length;
        final int[] actualCtx = Arrays.copyOf(block.state.ctxBuffer, ctxBufferLength);
        Arrays.fill(actualCtx, 0, ctxRowGap - 1, 0);
        Arrays.fill(actualCtx, ctxBufferLength - ctxRowGap, ctxBufferLength - 1, 0);

        Assertions.assertArrayEquals(passResultCtx, actualCtx, "Contexts differed for " + passIdent);
      }
    }
  }

}
