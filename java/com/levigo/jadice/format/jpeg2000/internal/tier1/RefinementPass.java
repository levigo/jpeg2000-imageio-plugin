package com.levigo.jadice.format.jpeg2000.internal.tier1;

import static com.levigo.jadice.format.jpeg2000.internal.Debug.PROTOCOL_EBCOT;
import static com.levigo.jadice.format.jpeg2000.internal.Debug.blockCodingProtocol;
import static com.levigo.jadice.format.jpeg2000.internal.tier1.States.EXTRA_CONTEXT_WORDS;
import static com.levigo.jadice.format.jpeg2000.internal.tier1.States.KAPPA_MAG_BASE;
import static com.levigo.jadice.format.jpeg2000.internal.tier1.States.MU_BIT;
import static com.levigo.jadice.format.jpeg2000.internal.tier1.States.NBRHD_MASK;

import java.io.IOException;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.SubbandType;

public class RefinementPass implements Pass {

  public static final CleanupPass CLEANUP_PASS = new CleanupPass();

  @Override
  public boolean run(MQDecoder mq, CodeBlock block, SubbandType type, int p, int width, int ctxRowGap)
      throws JPEG2000Exception, IOException {
    
    final int halfLSB = (1 << p) >> 1;
    final int widthBy2 = width + width;
    final int widthBy3 = widthBy2 + width;

    final PassState ps = new PassState();
    ps.cp = ctxRowGap + 1;
    
    final int[] samples = block.state.sampleBuffer;
    final int[] ctx = block.state.ctxBuffer;

    final ContextContainer mqCtx = block.mqCtx;

    for (int r = 0; r < block.numStripes; r++, ps.cp += EXTRA_CONTEXT_WORDS, ps.sp += widthBy3) {
      final int stripeHeight = block.stripeHeights[r];
      for (int c = width; c > 0; c--, ps.sp++, ps.cp++) {
        if ((ctx[ps.cp] & (MU_BIT | (MU_BIT << 3) | (MU_BIT << 6) | (MU_BIT << 9))) == 0) {
          // Invoke speedup trick to skip over runs of all-0 neighbourhoods
          for (ps.cp += 2; ctx[ps.cp] == 0; ps.cp += 2) {
            c -= 2;
            ps.sp += 2;
          }

          ps.cp -= 2;
          continue;
        }

        ps.ctxWord = ctx[ps.cp];

        if (PROTOCOL_EBCOT) {
          blockCodingProtocol().contextWord(ps.ctxWord);
        }

        processRow(ps, mq, mqCtx, samples, halfLSB, p, 0, 0);

        if (stripeHeight > 1) {
          processRow(ps, mq, mqCtx, samples, halfLSB, p, 3, width);

          if (stripeHeight > 2) {
            processRow(ps, mq, mqCtx, samples, halfLSB, p, 6, widthBy2);

            if (stripeHeight > 3) {
              processRow(ps, mq, mqCtx, samples, halfLSB, p, 9, widthBy3);
            }
          }
        }

        if (Debug.VISUALIZE_EBCOT_PASSES) {
          Passes.addDebugResult(this, block, type, samples, r, c);
        }
      }
    }

    return false;
  }

  private void processRow(PassState ps, MQDecoder mq, ContextContainer mqCtx, int[] samples, int halfLSB, int p,
      int ctxShift, int rowOffset) throws IOException {
    if ((ps.ctxWord & (MU_BIT << ctxShift)) != 0) {
      ps.value = samples[ps.sp + rowOffset];

      if (PROTOCOL_EBCOT) {
        blockCodingProtocol().sample(ps.value);
      }
      
      ps.symbol = (ps.value & Integer.MAX_VALUE) >> p;

      int stateIdx = KAPPA_MAG_BASE;
      if (ps.symbol < 4) {
        if ((ps.ctxWord & (NBRHD_MASK << ctxShift)) != 0) {
          stateIdx++;
        }
      } else {
        stateIdx += 2;
      }

      mqCtx.setIndex(stateIdx);
      ps.symbol = mq.decode(mqCtx);
      if (PROTOCOL_EBCOT) {
        blockCodingProtocol().refinement(ps.symbol);
      }
      ps.value ^= ((1 - ps.symbol) << p);
      ps.value |= halfLSB;
      samples[ps.sp + rowOffset] = ps.value;
      
      if (PROTOCOL_EBCOT) {
        blockCodingProtocol().sample(ps.value);
      }
    }
  }

  @Override
  public Pass next(CodeBlock block) {
    return CLEANUP_PASS;
  }
}
