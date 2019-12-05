package com.levigo.jadice.format.jpeg2000.internal.image;

import java.awt.image.WritableRaster;

import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;

public class DecodedRaster implements Pushable {
  public WritableRaster raster;
  public final Codestream codestream;

  public DecodedRaster(WritableRaster raster, Codestream cs) {
    this.raster = raster;
    codestream = cs;
  }

  @Override
  public void start(DecoderParameters parameters) {
    
  }

  @Override
  public void free() {

  }
}
