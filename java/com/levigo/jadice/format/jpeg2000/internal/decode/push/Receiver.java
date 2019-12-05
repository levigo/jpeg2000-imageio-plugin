package com.levigo.jadice.format.jpeg2000.internal.decode.push;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;

public interface Receiver<P extends Pushable> {

  void receive(P pushable, DecoderParameters parameters) throws JPEG2000Exception;

}
