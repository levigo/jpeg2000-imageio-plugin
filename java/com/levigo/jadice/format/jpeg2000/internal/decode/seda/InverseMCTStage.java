package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;

// part of an experiment. Currently not in use.
public class InverseMCTStage extends ConfigurableStage
    implements
      Transformer<DummyDataBuffer, Object, JPEG2000Exception> {

  @Override
  public void transform(DummyDataBuffer s, Consumer<? super Object, ? extends JPEG2000Exception> next)
      throws JPEG2000Exception {
    // TODO Auto-generated method stub
  }
}
