package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.Subband;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponent;
import com.levigo.jadice.format.jpeg2000.internal.marker.Qxx;
import com.levigo.jadice.format.jpeg2000.internal.tcq.InverseReversibleQuantizationTask;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;

// part of an experiment. Currently not in use.
public class InverseQuantizationStage extends ConfigurableStage
    implements
      Transformer<CodeBlock, CodeBlock, JPEG2000Exception> {

  @Override
  public void transform(CodeBlock block, Consumer<? super CodeBlock, ? extends JPEG2000Exception> next)
      throws JPEG2000Exception {
    if (block.state.finished) {
      final DummyDataBuffer buffer = getSampleBuffer(block);
      final DecodeTask<CodeBlock> task = createTask(block, buffer);
      try {
        final CodeBlock codeBlock = task.call();
        next.consume(codeBlock);
      } catch (final Exception e) {
        throw new JPEG2000Exception(GeneralMessages.WRAPPED_EXCEPTION, e);
      }
    }
  }

  private DummyDataBuffer getSampleBuffer(CodeBlock block) {
    final TileComponent tileComp = block.bandPrecinct.precinct.resolution.tileComp;
    if (tileComp.state == null) {
      tileComp.start(parameters);
    }
    return tileComp.state.sampleBuffer;
  }

  private DecodeTask<CodeBlock> createTask(CodeBlock block, DummyDataBuffer buffer) throws JPEG2000Exception {
    final Subband band = block.bandPrecinct.subband;
    final TileComponent tileComp = band.resolution.tileComp;
    final Qxx qxx = Qxx.accessQCx(tileComp.codestream, tileComp.tile, tileComp.comp.idx);

    switch (qxx.Sqxx_style){
      case Qxx.VALUE_NO_QUANTIZATION :
        return new InverseReversibleQuantizationTask(block, buffer);

        // TODO implement at least the other two cases defined in ITU-T.800:
        // case Qxx.VALUE_SCALAR_DERIVED :
        // case Qxx.VALUE_SCALAR_EXPOUNDED :

      default :
        throw new JPEG2000Exception(CodestreamMessages.UNSUPPORTED_CODESTREAM_FEATURE, "(de)quantization style "
            + Integer.toBinaryString(qxx.Sqxx_style));
    }
  }
}
