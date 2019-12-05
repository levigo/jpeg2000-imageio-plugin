package com.levigo.jadice.format.jpeg2000.internal.tier1;

import java.io.IOException;
import java.util.List;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.BlockContribution;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codeword;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.image.BandPrecinct;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlockState;
import com.levigo.jadice.format.jpeg2000.internal.image.Pair;
import com.levigo.jadice.format.jpeg2000.internal.image.Subband;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;

/**
 * Performs EBCoT Decoding
 */
@Refer(to = Spec.J2K_CORE, page = 84, section = "Annex D", called = "Coefficient Bit Modelling")
public class BlockContributionTask implements DecodeTask<CodeBlock> {

  private final BlockContribution blockContribution;

  public BlockContributionTask(BlockContribution blockContribution) {
    this.blockContribution = blockContribution;
  }

  @Override
  public CodeBlock call() throws JPEG2000Exception {
    final CodeBlock block = blockContribution.block;

    final CodeBlockState state = block.state;

    Pass pass = state.lastPass;

    final BandPrecinct bandPrecinct = block.bandPrecinct;
    final Subband subband = bandPrecinct.subband;
    final Pair size = block.region().absolute().size;
    final int ctxRowGap = size.x + States.EXTRA_CONTEXT_WORDS;

    final List<Codeword> codewords = blockContribution.codewords;
    for (Codeword codeword : codewords) {
      try {
        MQDecoder mq = null;

        while (codeword.passes > 0) {

          if (mq == null || block.restart) {
            mq = createMQDecoder(codeword.input);
          }

          if (block.mqCtx == null || block.resetCtx) {
            block.mqCtx = new ContextContainer();
          }

          pass = pass != null ? pass.next(block) : new CleanupPass();

          final boolean errorFound = pass.run(mq, block, subband.type, state.bitplane, size.x, ctxRowGap);

          inspect(block);
          
          state.passIdx++;
          codeword.passIdx++;
          codeword.passes--;

          if (errorFound) {
            // Kakadu says "mq_coder must be properly closed down."
          }

          if (codeword.passes == 0) {
            // FIXME how should we react?
          }

        }
      } catch (IOException e) {
        throw new JPEG2000Exception(GeneralMessages.IO_ERROR, e);
      }
    }

    if (blockContribution.isLastLayer) {
      state.finished = true;
    } else {
      state.lastPass = pass;
    }

    return block;
  }

  protected MQDecoder createMQDecoder(SeekableInputStream input) throws IOException {
    return new AdaptiveMQDecoder(input);
  }

  protected void inspect(CodeBlock block) {

  }

}
