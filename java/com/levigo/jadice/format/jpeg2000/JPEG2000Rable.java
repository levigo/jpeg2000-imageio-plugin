package com.levigo.jadice.format.jpeg2000;

import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;

import com.levigo.jadice.document.internal.model.renderable.BaseRable;
import com.levigo.jadice.document.internal.model.renderable.Rable;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;

public class JPEG2000Rable extends BaseRable {

  private final Codestream codestream;
  private final JPEG2000Image image;

  public JPEG2000Rable(Codestream codestream) {
    this(codestream, new JPEG2000Image(codestream));
  }

  private JPEG2000Rable(Codestream codestream, JPEG2000Image image) {
    this.codestream = codestream;
    this.image = image;
  }

  @Override
  public RenderedImage createRendering(RenderContext renderContext) {
    return image;
  }

  @Override
  public Rable createSnapshot() {
    return new JPEG2000Rable(codestream, image);
  }

  @Override
  public float getWidth() {
    return image.getWidth();
  }

  @Override
  public float getHeight() {
    return image.getHeight();
  }

  public Codestream getCodestream() {
    return codestream;
  }

  public JPEG2000Image getImage() {
    return image;
  }
}
