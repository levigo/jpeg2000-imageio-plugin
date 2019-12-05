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
import static org.jadice.jpeg2000.internal.tier1.States.CLEANUP_MEMBER_MASK;
import static org.jadice.jpeg2000.internal.tier1.States.EXTRA_CONTEXT_WORDS;
import static org.jadice.jpeg2000.internal.tier1.States.KAPPA_RUN;
import static org.jadice.jpeg2000.internal.tier1.States.KAPPA_SIGN_BASE;
import static org.jadice.jpeg2000.internal.tier1.States.KAPPA_SIG_BASE;
import static org.jadice.jpeg2000.internal.tier1.States.KAPPA_UNI;
import static org.jadice.jpeg2000.internal.tier1.States.MU_BIT;
import static org.jadice.jpeg2000.internal.tier1.States.MU_POS;
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
import static org.jadice.jpeg2000.internal.tier1.States.getSignificanceLUT;

import java.io.IOException;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.Debug;
import org.jadice.jpeg2000.internal.image.CodeBlock;
import org.jadice.jpeg2000.internal.image.SubbandType;
import org.jadice.jpeg2000.internal.marker.COx;

public class CleanupPass implements Pass {

  @Override
  public boolean run(MQDecoder mq, CodeBlock block, SubbandType type, int p, int width, int ctxRowGap)
      throws JPEG2000Exception, IOException {

    final byte[] lut = getSignificanceLUT(type);

    int onePointFive = 1 << p;
    onePointFive += (onePointFive >> 1);

    final int widthBy2 = width + width;
    final int widthBy3 = widthBy2 + width;

    final PassState ps = new PassState();
    ps.cp = ctxRowGap + 1;
    final int[] ctx = block.state.ctxBuffer;
    final int[] samples = block.state.sampleBuffer;

    for (int r = 0; r < block.numStripes; r++, ps.cp += EXTRA_CONTEXT_WORDS, ps.sp += widthBy3) {
      final int height = block.stripeHeights[r];
      if (height == 4) {
        fullStripe(mq, block, type, ctxRowGap, lut, onePointFive, width, widthBy2, widthBy3, ps, ctx, samples, r);
      } else {
        shorter(mq, block, type, width, ctxRowGap, lut, onePointFive, widthBy2, widthBy3, ps, ctx, samples, r, height);
      }
    }

    // At the end of the cleanup pass we have finished with a bitplane.
    block.state.bitplane--;

    if (block.segmentationMarks) {
      final ContextContainer mqCtx = block.mqCtx;
      mqCtx.setIndex(KAPPA_UNI);
      final int sym = mq.decode(mqCtx) << 3 | (mq.decode(mqCtx) << 2) | (mq.decode(mqCtx) << 1) | mq.decode(mqCtx);

      if (PROTOCOL_EBCOT) {
        blockCodingProtocol().segmentationMark(sym);
      }

      if (sym != 0x0A) {
        return true;
      }
    }

    return false;
  }

  private void shorter(MQDecoder mq, CodeBlock block, SubbandType type, int width, int ctxRowGap, byte[] sigLUT,
      int onePointFive, int widthBy2, int widthBy3, PassState ps, int[] ctx, int[] samples, int r, int height)
      throws IOException {

    final ContextContainer mqCtx = block.mqCtx;

    for (int c = width; c > 0; c--, ps.sp++, ps.cp++) {
      ps.ctxWord = ctx[ps.cp];

      if (PROTOCOL_EBCOT) {
        blockCodingProtocol().contextWord(ps.ctxWord);
      }

      if (significant(ps, mq, mqCtx, sigLUT, 0)) {
        processRow0(ps, mq, mqCtx, ctx, samples, onePointFive, ctxRowGap, block.causalCtx);
      }
      if (height > 1) {
        if (significant(ps, mq, mqCtx, sigLUT, 3)) {
          processRow1(ps, mq, mqCtx, ctx, samples, onePointFive, width);
        }
        if (height > 2) {
          if (significant(ps, mq, mqCtx, sigLUT, 6)) {
            processRow2(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy2);
          }
          if (height > 3 && significant(ps, mq, mqCtx, sigLUT, 9)) {
            processRow3(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy3, ctxRowGap);
          }
        }
      }

      if (PROTOCOL_EBCOT) {
        blockCodingProtocol().contextWord(ps.ctxWord);
      }

      ps.ctxWord |= (ps.ctxWord << (MU_POS - SIGMA_CC_POS)) & (MU_BIT | (MU_BIT << 3) | (MU_BIT << 6) | (MU_BIT << 9));
      ps.ctxWord &= ~(PI_BIT | (PI_BIT << 3) | (PI_BIT << 6) | (PI_BIT << 9));

      if (PROTOCOL_EBCOT) {
        blockCodingProtocol().contextWord(ps.ctxWord);
      }

      ctx[ps.cp] = ps.ctxWord;

      if (Debug.VISUALIZE_EBCOT_PASSES) {
        Passes.addDebugResult(this, block, type, samples, r, c);
      }
    }
  }

  private void fullStripe(MQDecoder mq, CodeBlock block, SubbandType type, int ctxRowGap, byte[] sigLUT,
      int onePointFive, int width, int widthBy2, int widthBy3, PassState ps, int[] ctx, int[] samples, int r)
      throws IOException {

    final ContextContainer mqCtx = block.mqCtx;

    for (int c = width; c > 0; c--, ps.sp++, ps.cp++) {
      if (ctx[ps.cp] == 0) {
        mqCtx.setIndex(KAPPA_RUN);
        ps.symbol = mq.decode(mqCtx);

        if (PROTOCOL_EBCOT) {
          blockCodingProtocol().run(ps.symbol);
        }

        if (ps.symbol == 0) {
          continue;
        }
        processRunlength(ps, mq, block, sigLUT, onePointFive, width, widthBy2, widthBy3, ctxRowGap);

      } else {
        ps.ctxWord = ctx[ps.cp];

        if (PROTOCOL_EBCOT) {
          blockCodingProtocol().contextWord(ps.ctxWord);
        }

        if (significant(ps, mq, mqCtx, sigLUT, 0)) {
          processRow0(ps, mq, mqCtx, ctx, samples, onePointFive, ctxRowGap, block.causalCtx);
        }
        if (significant(ps, mq, mqCtx, sigLUT, 3)) {
          processRow1(ps, mq, mqCtx, ctx, samples, onePointFive, width);
        }
        if (significant(ps, mq, mqCtx, sigLUT, 6)) {
          processRow2(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy2);
        }
        if (significant(ps, mq, mqCtx, sigLUT, 9)) {
          processRow3(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy3, ctxRowGap);
        }
      }

      if (PROTOCOL_EBCOT) {
        blockCodingProtocol().contextWord(ps.ctxWord);
      }

      ps.ctxWord |= (ps.ctxWord << (MU_POS - SIGMA_CC_POS)) & (MU_BIT | (MU_BIT << 3) | (MU_BIT << 6) | (MU_BIT << 9));
      ps.ctxWord &= ~(PI_BIT | (PI_BIT << 3) | (PI_BIT << 6) | (PI_BIT << 9));

      if (PROTOCOL_EBCOT) {
        blockCodingProtocol().contextWord(ps.ctxWord);
      }

      ctx[ps.cp] = ps.ctxWord;

      if (Debug.VISUALIZE_EBCOT_PASSES) {
        Passes.addDebugResult(this, block, type, samples, r, c);
      }
    }
  }

  private static void processRunlength(PassState ps, MQDecoder mq, CodeBlock block, byte[] sigLUT, int onePointFive,
      int width, int widthBy2, int widthBy3, int ctxRowGap) throws IOException {

    final ContextContainer mqCtx = block.mqCtx;

    mqCtx.setIndex(KAPPA_UNI);
    ps.symbol = mq.decode(mqCtx) << 1 | mq.decode(mqCtx);
    ps.ctxWord = block.state.ctxBuffer[ps.cp];

    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().runlength(ps.symbol);
      blockCodingProtocol().contextWord(ps.ctxWord);
    }

    int[] ctx = block.state.ctxBuffer;
    int[] samples = block.state.sampleBuffer;

    switch (ps.symbol){
      case 0 :
        processRow0(ps, mq, mqCtx, ctx, samples, onePointFive, ctxRowGap, block.causalCtx);
        if (significant(ps, mq, mqCtx, sigLUT, 3)) {
          processRow1(ps, mq, mqCtx, ctx, samples, onePointFive, width);
        }
        if (significant(ps, mq, mqCtx, sigLUT, 6)) {
          processRow2(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy2);
        }
        if (significant(ps, mq, mqCtx, sigLUT, 9)) {
          processRow3(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy3, ctxRowGap);
        }
        break;

      case 1 :
        processRow1(ps, mq, mqCtx, ctx, samples, onePointFive, width);
        if (significant(ps, mq, mqCtx, sigLUT, 6)) {
          processRow2(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy2);
        }
        if (significant(ps, mq, mqCtx, sigLUT, 9)) {
          processRow3(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy3, ctxRowGap);
        }
        break;

      case 2 :
        processRow2(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy2);
        if (significant(ps, mq, mqCtx, sigLUT, 9)) {
          processRow3(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy3, ctxRowGap);
        }
        break;

      case 3 :
        processRow3(ps, mq, mqCtx, ctx, samples, onePointFive, widthBy3, ctxRowGap);
    }
  }

  private static boolean significant(PassState ps, MQDecoder mq, ContextContainer mqCtx, byte[] sigLUT, int ctxShift)
      throws IOException {
    if ((ps.ctxWord & (CLEANUP_MEMBER_MASK << ctxShift)) == 0) {
      mqCtx.setIndex(KAPPA_SIG_BASE + sigLUT[(ps.ctxWord >> ctxShift) & NBRHD_MASK]);

      final boolean symbol = mq.decode(mqCtx) == 1;

      if (PROTOCOL_EBCOT) {
        blockCodingProtocol().significance(symbol);
      }

      return symbol;
    }

    return false;
  }

  static void processRow0(PassState ps, MQDecoder mq, ContextContainer mqCtx, int[] ctx, int[] samples,
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
    ps.ctxWord |= SIGMA_CC_BIT | (ps.symbol << CHI_POS);
    samples[ps.sp] = (ps.symbol << 31) + onePointFive;
    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().contextWord(ps.ctxWord);
      blockCodingProtocol().sample(samples[ps.sp]);
    }
  }

  static void processRow1(PassState ps, MQDecoder mq, ContextContainer mqCtx, int[] ctx, int[] samples,
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
    ps.ctxWord |= (SIGMA_CC_BIT << 3) | (ps.symbol << (CHI_POS + 3));
    samples[ps.sp + width] = (ps.symbol << 31) + onePointFive;
    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().contextWord(ps.ctxWord);
      blockCodingProtocol().sample(samples[ps.sp + width]);
    }
  }

  static void processRow2(PassState ps, MQDecoder mq, ContextContainer mqCtx, int[] ctx, int[] samples,
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
    ps.ctxWord |= (SIGMA_CC_BIT << 6) | (ps.symbol << (CHI_POS + 6));
    samples[ps.sp + widthBy2] = (ps.symbol << 31) + onePointFive;
    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().contextWord(ps.ctxWord);
      blockCodingProtocol().sample(samples[ps.sp + widthBy2]);
    }
  }

  static void processRow3(PassState ps, MQDecoder mq, ContextContainer mqCtx, int[] ctx, int[] samples,
      int onePointFive, int widthBy3, int ctxRowGap) throws IOException {
    ps.symbol = ps.ctxWord & ((CHI_BIT << 6) | (SIGMA_CC_BIT << 6) | (SIGMA_CC_BIT << 12));
    ps.symbol >>= 10; // Shift down so that top sigma bit has address 0
    if (ps.ctxWord < 0) {
      ps.symbol |= CHI_BIT << (12 - 10);
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
    ctx[ps.cp + ctxRowGap - 1] |= SIGMA_TR_BIT;
    ctx[ps.cp + ctxRowGap] |= SIGMA_TC_BIT | (ps.symbol << PREV_CHI_POS);
    ctx[ps.cp + ctxRowGap + 1] |= SIGMA_TL_BIT;
    ctx[ps.cp - 1] |= (SIGMA_CR_BIT << 9);
    ctx[ps.cp + 1] |= (SIGMA_CL_BIT << 9);
    ps.ctxWord |= (SIGMA_CC_BIT << 9) | (ps.symbol << (CHI_POS + 9));
    samples[ps.sp + widthBy3] = (ps.symbol << 31) + onePointFive;
    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().contextWord(ps.ctxWord);
      blockCodingProtocol().sample(samples[ps.sp + widthBy3]);
    }
  }

  /**
   * Checks if the flag for <i>selective arithmetic coding bypass</i> (
   * {@link COx#MASK_MODE_ARITHMETIC_BYPASS}) was set. If <code>true</code>, the arithmetic
   * (de)coder for the significance propagation pass and magnitude refinement coding passes starting
   * in the fifth significant bit-plane of the code-block is bypassed. In this case the method
   * returns an instance of {@link SignificanceRawPass}, otherwise {@link SignificancePass}.
   *
   * @param block the code-block representation which is currently in this <i>Tier 1</i> decoding
   *          stage.
   * @return an instance of {@link SignificanceRawPass} or {@link SignificancePass} based on the
   *         constraints described above.
   */
  @Override
  public Pass next(CodeBlock block) {
    return (block.bypass && block.state.passIdx >= 10) ? Passes.SIGNIFICANCE_RAW_PASS : Passes.SIGNIFICANCE_PASS;
  }
}
