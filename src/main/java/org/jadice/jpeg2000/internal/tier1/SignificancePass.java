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

import static org.jadice.jpeg2000.internal.Debug.PROTOCOL_EBCOT;
import static org.jadice.jpeg2000.internal.Debug.blockCodingProtocol;
import static org.jadice.jpeg2000.internal.tier1.States.CHI_BIT;
import static org.jadice.jpeg2000.internal.tier1.States.CHI_POS;
import static org.jadice.jpeg2000.internal.tier1.States.EXTRA_CONTEXT_WORDS;
import static org.jadice.jpeg2000.internal.tier1.States.KAPPA_SIGN_BASE;
import static org.jadice.jpeg2000.internal.tier1.States.KAPPA_SIG_BASE;
import static org.jadice.jpeg2000.internal.tier1.States.NBRHD_MASK;
import static org.jadice.jpeg2000.internal.tier1.States.NEXT_CHI_POS;
import static org.jadice.jpeg2000.internal.tier1.States.PI_BIT;
import static org.jadice.jpeg2000.internal.tier1.States.PREV_CHI_POS;
import static org.jadice.jpeg2000.internal.tier1.States.SIGMA_BC_BIT;
import static org.jadice.jpeg2000.internal.tier1.States.SIGMA_BL_BIT;
import static org.jadice.jpeg2000.internal.tier1.States.SIGMA_BR_BIT;
import static org.jadice.jpeg2000.internal.tier1.States.SIGMA_CC_BIT;
import static org.jadice.jpeg2000.internal.tier1.States.SIGMA_CC_POS;
import static org.jadice.jpeg2000.internal.tier1.States.SIGMA_CL_BIT;
import static org.jadice.jpeg2000.internal.tier1.States.SIGMA_CR_BIT;
import static org.jadice.jpeg2000.internal.tier1.States.SIGMA_TC_BIT;
import static org.jadice.jpeg2000.internal.tier1.States.SIGMA_TL_BIT;
import static org.jadice.jpeg2000.internal.tier1.States.SIGMA_TR_BIT;
import static org.jadice.jpeg2000.internal.tier1.States.SIGN_LUT;
import static org.jadice.jpeg2000.internal.tier1.States.SIG_PROP_MEMBER_MASK;
import static org.jadice.jpeg2000.internal.tier1.States.getSignificanceLUT;

import java.io.IOException;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.Debug;
import org.jadice.jpeg2000.internal.image.CodeBlock;
import org.jadice.jpeg2000.internal.image.SubbandType;

public class SignificancePass implements Pass {

  public static final RefinementRawPass REFINEMENT_RAW_PASS = new RefinementRawPass();
  public static final RefinementPass REFINEMENT_PASS = new RefinementPass();

  @Override
  public boolean run(MQDecoder mq, CodeBlock block, SubbandType type, int p, int width, int ctxRowGap)
      throws JPEG2000Exception, IOException {

    final byte[] sigLUT = getSignificanceLUT(type);

    int onePointFive = 1 << p;
    onePointFive += (onePointFive >> 1);

    final int widthBy2 = width + width;
    final int widthBy3 = widthBy2 + width;

    final PassState ps = new PassState();
    ps.cp = ctxRowGap + 1;

    final int[] ctx = block.state.ctxBuffer;
    final int[] samples = block.state.sampleBuffer;

    final ContextContainer mqCtx = block.mqCtx;

    for (int r = 0; r < block.numStripes; r++, ps.cp += EXTRA_CONTEXT_WORDS, ps.sp += widthBy3) {
      final int stripeHeight = block.stripeHeights[r];
      for (int c = width; c > 0; c--, ps.cp++, ps.sp++) {

        if (ctx[ps.cp] == 0) {
          // Invoke speedup trick to skip over runs of all-0 neighbourhoods
          for (ps.cp += 3; ctx[ps.cp] == 0; ps.cp += 3) {
            c -= 3;
            ps.sp += 3;
          }

          ps.cp -= 3;
          continue;
        }

        ps.ctxWord = ctx[ps.cp];

        if (PROTOCOL_EBCOT) {
          blockCodingProtocol().contextWord(ps.ctxWord);
        }

        if (significant(ps, mq, mqCtx, sigLUT, 0)) {
          processRow0(ps, mq, mqCtx, ctx, samples, onePointFive, ctxRowGap, block.causalCtx);
        }

        if (stripeHeight > 1) {
          if (significant(ps, mq, mqCtx, sigLUT, 3)) {
            processRow1(ps, mq, mqCtx, ctx, samples, onePointFive, width);
          }

          if (stripeHeight > 2) {
            if (significant(ps, mq, mqCtx, sigLUT, 6)) {
              processRow2(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy2);
            }

            if (stripeHeight > 3 && significant(ps, mq, mqCtx, sigLUT, 9)) {
              processRow3(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy3, ctxRowGap);
            }
          }
        }

        if (PROTOCOL_EBCOT) {
          blockCodingProtocol().contextWord(ps.ctxWord);
        }

        block.state.ctxBuffer[ps.cp] = ps.ctxWord;

        if (Debug.VISUALIZE_EBCOT_PASSES) {
          Passes.addDebugResult(this, block, type, samples, r, c);
        }
      }
    }

    return false;
  }

  private static boolean significant(PassState ps, MQDecoder mq, ContextContainer mqCtx, byte[] sigLUT, int ctxShift)
      throws IOException {
    if ((ps.ctxWord & (NBRHD_MASK << ctxShift)) != 0 && (ps.ctxWord & (SIG_PROP_MEMBER_MASK << ctxShift)) == 0) {
      mqCtx.setIndex(KAPPA_SIG_BASE + sigLUT[(ps.ctxWord >> ctxShift) & NBRHD_MASK]);

      final boolean significant = mq.decode(mqCtx) == 1;
      if (PROTOCOL_EBCOT) {
        blockCodingProtocol().significance(significant);
      }

      if (!significant) {
        ps.ctxWord |= (PI_BIT << ctxShift);

        if (PROTOCOL_EBCOT) {
          blockCodingProtocol().contextWord(ps.ctxWord);
        }
      }


      return significant;
    }

    return false;
  }

  private static void processRow0(PassState ps, MQDecoder mq, ContextContainer mqCtx, int[] ctx, int[] samples,
      int onePointFive, int ctxRowGap, boolean causal) throws IOException {
    ps.symbol = ps.ctxWord & ((CHI_BIT >> 3) | (SIGMA_CC_BIT >> 3) | (CHI_BIT << 3) | (SIGMA_CC_BIT << 3));
    ps.symbol >>= 1; // Shift down so that top sigma bit has address 0
    ps.symbol |= (ctx[ps.cp - 1] & (CHI_BIT | SIGMA_CC_BIT)) >> (1 + 1);
    ps.symbol |= (ctx[ps.cp + 1] & (CHI_BIT | SIGMA_CC_BIT)) >> (1 - 1);
    ps.symbol |= (ps.symbol >> (CHI_POS - 1 - SIGMA_CC_POS)); // Interleave chi & sigma

    ps.value = SIGN_LUT[ps.symbol & 0x000000FF];

    mqCtx.setIndex(KAPPA_SIGN_BASE + (ps.value >> 1));

    ps.symbol = mq.decode(mqCtx);

    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().sign(ps.symbol);
    }

    ps.symbol ^= (ps.value & 1); // Sign bit recovered in LSB.

    // Broadcast neighbourhood context changes
    if (!causal) {
      ctx[ps.cp - ctxRowGap - 1] |= (SIGMA_BR_BIT << 9);
      ctx[ps.cp - ctxRowGap] |= (SIGMA_BC_BIT << 9) | (ps.symbol << NEXT_CHI_POS);
      ctx[ps.cp - ctxRowGap + 1] |= (SIGMA_BL_BIT << 9);
    }

    ctx[ps.cp - 1] |= SIGMA_CR_BIT;
    ctx[ps.cp + 1] |= SIGMA_CL_BIT;
    ps.ctxWord |= SIGMA_CC_BIT | PI_BIT | (ps.symbol << CHI_POS);
    samples[ps.sp] = (ps.symbol << 31) + onePointFive;

    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().contextWord(ps.ctxWord);
      blockCodingProtocol().sample(samples[ps.sp]);
    }
  }

  private static void processRow1(PassState ps, MQDecoder mq, ContextContainer mqCtx, int[] ctx, int[] samples,
      int onePointFive, int width) throws IOException {
    ps.symbol = ps.ctxWord & (CHI_BIT | SIGMA_CC_BIT | (CHI_BIT << 6) | (SIGMA_CC_BIT << 6));
    ps.symbol >>= 4; // Shift down so that top sigma bit has address 0
    ps.symbol |= (ctx[ps.cp - 1] & ((CHI_BIT << 3) | (SIGMA_CC_BIT << 3))) >> (4 + 1);
    ps.symbol |= (ctx[ps.cp + 1] & ((CHI_BIT << 3) | (SIGMA_CC_BIT << 3))) >> (4 - 1);
    ps.symbol |= (ps.symbol >> (CHI_POS - 1 - SIGMA_CC_POS)); // Interleave chi & sigma

    ps.value = SIGN_LUT[ps.symbol & 0x000000FF];

    mqCtx.setIndex(KAPPA_SIGN_BASE + (ps.value >> 1));

    ps.symbol = mq.decode(mqCtx);

    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().sign(ps.symbol);
    }

    ps.symbol ^= (ps.value & 1); // Sign bit recovered in LSB.

    // Broadcast neighbourhood context changes
    ctx[ps.cp - 1] |= (SIGMA_CR_BIT << 3);
    ctx[ps.cp + 1] |= (SIGMA_CL_BIT << 3);
    ps.ctxWord |= (SIGMA_CC_BIT << 3) | (PI_BIT << 3) | (ps.symbol << (CHI_POS + 3));
    samples[ps.sp + width] = (ps.symbol << 31) + onePointFive;

    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().contextWord(ps.ctxWord);
      blockCodingProtocol().sample(samples[ps.sp + width]);
    }
  }

  private static void processRow2(PassState ps, MQDecoder mq, ContextContainer mqCtx, int[] ctx, int[] samples,
      int onePointFive, int widthBy2) throws IOException {
    ps.symbol = ps.ctxWord & ((CHI_BIT << 3) | (SIGMA_CC_BIT << 3) | (CHI_BIT << 9) | (SIGMA_CC_BIT << 9));
    ps.symbol >>= 7; // Shift down so that top sigma bit has address 0
    ps.symbol |= (ctx[ps.cp - 1] & ((CHI_BIT << 6) | (SIGMA_CC_BIT << 6))) >> (7 + 1);
    ps.symbol |= (ctx[ps.cp + 1] & ((CHI_BIT << 6) | (SIGMA_CC_BIT << 6))) >> (7 - 1);
    ps.symbol |= (ps.symbol >> (CHI_POS - 1 - SIGMA_CC_POS)); // Interleave chi & sigma

    ps.value = SIGN_LUT[ps.symbol & 0x000000FF];

    mqCtx.setIndex(KAPPA_SIGN_BASE + (ps.value >> 1));

    ps.symbol = mq.decode(mqCtx);

    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().sign(ps.symbol);
    }

    ps.symbol ^= (ps.value & 1); // Sign bit recovered in LSB.

    // Broadcast neighbourhood context changes
    ctx[ps.cp - 1] |= (SIGMA_CR_BIT << 6);
    ctx[ps.cp + 1] |= (SIGMA_CL_BIT << 6);
    ps.ctxWord |= (SIGMA_CC_BIT << 6) | (PI_BIT << 6) | (ps.symbol << (CHI_POS + 6));
    samples[ps.sp + widthBy2] = (ps.symbol << 31) + onePointFive;

    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().contextWord(ps.ctxWord);
      blockCodingProtocol().sample(samples[ps.sp + widthBy2]);
    }
  }

  private static void processRow3(PassState ps, MQDecoder mq, ContextContainer mqCtx, int[] ctx, int[] samples,
      int onePointFive, int widthBy3, int contextRowGap) throws IOException {
    ps.symbol = ps.ctxWord & ((CHI_BIT << 6) | (SIGMA_CC_BIT << 6) | (SIGMA_CC_BIT << 12));
    ps.symbol >>= 10; // Shift down so that top sigma bit has address 0
    if (ps.ctxWord < 0) {
      // Use the fact that NEXT_CHI_BIT = 31
      ps.symbol |= (CHI_BIT << (12 - 10));
    }
    ps.symbol |= (ctx[ps.cp - 1] & ((CHI_BIT << 9) | (SIGMA_CC_BIT << 9))) >> (10 + 1);
    ps.symbol |= (ctx[ps.cp + 1] & ((CHI_BIT << 9) | (SIGMA_CC_BIT << 9))) >> (10 - 1);
    ps.symbol |= (ps.symbol >> (CHI_POS - 1 - SIGMA_CC_POS)); // Interleave chi & sigma

    ps.value = SIGN_LUT[ps.symbol & 0x000000FF];

    mqCtx.setIndex(KAPPA_SIGN_BASE + (ps.value >> 1));

    ps.symbol = mq.decode(mqCtx);

    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().sign(ps.symbol);
    }
    
    ps.symbol ^= (ps.value & 1); // Sign bit recovered in LSB.

    // Broadcast neighbourhood context changes
    ctx[ps.cp + contextRowGap - 1] |= SIGMA_TR_BIT;
    ctx[ps.cp + contextRowGap] |= SIGMA_TC_BIT | (ps.symbol << PREV_CHI_POS);
    ctx[ps.cp + contextRowGap + 1] |= SIGMA_TL_BIT;
    ctx[ps.cp - 1] |= (SIGMA_CR_BIT << 9);
    ctx[ps.cp + 1] |= (SIGMA_CL_BIT << 9);
    ps.ctxWord |= (SIGMA_CC_BIT << 9) | (PI_BIT << 9) | (ps.symbol << (CHI_POS + 9));
    samples[ps.sp + widthBy3] = (ps.symbol << 31) + onePointFive;

    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().contextWord(ps.ctxWord);
      blockCodingProtocol().sample(samples[ps.sp + widthBy3]);
    }
  }

  @Override
  public Pass next(CodeBlock block) {
    return (block.bypass && block.state.passIdx >= 10) ? REFINEMENT_RAW_PASS : REFINEMENT_PASS;
  }
}
