package com.levigo.jadice.format.jpeg2000.internal.decode.push.stage;

import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;

public class HighestResolutionOnly implements GateKeeper<Resolution> {
  @Override
  public boolean allows(Resolution resolution) {
    return resolution.dwtLevel == 0;
  }
}
