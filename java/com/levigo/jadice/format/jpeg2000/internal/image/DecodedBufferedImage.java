package com.levigo.jadice.format.jpeg2000.internal.image;

import java.awt.image.BufferedImage;

import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;

public class DecodedBufferedImage implements Pushable {
  public BufferedImage image;
  public final Codestream codestream;

  public DecodedBufferedImage(BufferedImage image, Codestream codestream) {
    this.image = image;
    this.codestream = codestream;
  }

  @Override
  public void start(DecoderParameters parameters) {
    
  }

  @Override
  public void free() {

  }
}
