package com.levigo.jadice.format.jpeg2000.internal.decode.push.stage;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.image.DecodedImage;
import com.levigo.jadice.format.jpeg2000.internal.image.DecodedRaster;

public class RenderedImageAssemblerStage implements Receiver<DecodedRaster> {
  private final Receiver<DecodedImage> nextStage;

  public RenderedImageAssemblerStage(Receiver<DecodedImage> nextStage) {
    this.nextStage = nextStage;
  }

  @Override
  public void receive(DecodedRaster pushable, DecoderParameters parameters) throws JPEG2000Exception {

  }
}
