package com.levigo.jadice.format.jpeg2000.internal.decode.push.stage;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;

public class Gate<P extends Pushable> implements Receiver<P> {
  private final Receiver<P> nextStage;
  private final GateKeeper<P> gateKeeper;

  public Gate(Receiver<P> nextStage, GateKeeper<P> gateKeeper) {
    this.nextStage = nextStage;
    this.gateKeeper = gateKeeper;
  }

  @Override
  public void receive(P pushable, DecoderParameters parameters) throws JPEG2000Exception {
    if (gateKeeper.allows(pushable)) {
      nextStage.receive(pushable, parameters);
    }
  }
}
