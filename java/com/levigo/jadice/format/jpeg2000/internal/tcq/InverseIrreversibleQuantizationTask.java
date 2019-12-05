package com.levigo.jadice.format.jpeg2000.internal.tcq;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;

public class InverseIrreversibleQuantizationTask implements DecodeTask<CodeBlock> {

  private final CodeBlock block;
  private final DummyDataBuffer sampleBuffer;

  public InverseIrreversibleQuantizationTask(CodeBlock block, DummyDataBuffer sampleBuffer) {
    this.block = block;
    this.sampleBuffer = sampleBuffer;
  }

  @Override
  public CodeBlock call() throws JPEG2000Exception {
    Quantizations.inverseIrreversible(block, sampleBuffer);
    return block;
  }

}
