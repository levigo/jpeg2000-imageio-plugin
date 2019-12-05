package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;

// part of an experiment. Currently not in use.
public class LevelShiftStage extends ConfigurableStage
    implements
      Transformer<Resolution, DummyDataBuffer, JPEG2000Exception> {

  @Override
  public void transform(Resolution s, Consumer<? super DummyDataBuffer, ? extends JPEG2000Exception> next)
      throws JPEG2000Exception {
    // TODO Auto-generated method stub
  }
}
