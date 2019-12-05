package com.levigo.jadice.format.jpeg2000.internal.decode.push;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;

public interface Pushable {
  void start(DecoderParameters parameters);
  void free() throws JPEG2000Exception;
}
