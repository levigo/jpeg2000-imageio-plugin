package com.levigo.jadice.format.jpeg2000.internal.decode.push.stage;

import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;

public interface GateKeeper<P extends Pushable> {
  boolean allows(P pushable);
}
