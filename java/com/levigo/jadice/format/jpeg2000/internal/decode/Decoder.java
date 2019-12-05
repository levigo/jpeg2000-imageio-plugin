package com.levigo.jadice.format.jpeg2000.internal.decode;

import java.awt.image.Raster;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;

public interface Decoder {
  Raster decode(DecoderParameters parameters) throws JPEG2000Exception;
}
