package com.levigo.jadice.format.jpeg2000.internal.decode.push.stage;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;

public class ResultCollector<P extends Pushable> implements Receiver<P> {
  private P result;

  @Override
  public void receive(P pushable, DecoderParameters parameters) throws JPEG2000Exception {
    result = pushable;
  }

  public P getResult() {
    return result;
  }
}
