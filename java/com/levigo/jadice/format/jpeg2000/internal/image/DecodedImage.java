package com.levigo.jadice.format.jpeg2000.internal.image;

import java.awt.image.RenderedImage;

import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;

public class DecodedImage implements Pushable {
  public RenderedImage image;

  public DecodedImage(RenderedImage image) {
    this.image = image;
  }

  @Override
  public void start(DecoderParameters parameters) {
    
  }

  @Override
  public void free() {

  }
}
