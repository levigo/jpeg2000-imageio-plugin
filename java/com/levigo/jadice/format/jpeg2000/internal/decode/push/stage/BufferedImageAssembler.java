package com.levigo.jadice.format.jpeg2000.internal.decode.push.stage;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.util.Hashtable;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.image.DecodedBufferedImage;
import com.levigo.jadice.format.jpeg2000.internal.image.DecodedRaster;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;

public class BufferedImageAssembler implements Receiver<DecodedRaster> {
  private final Receiver<? super DecodedBufferedImage> nextStage;

  public BufferedImageAssembler(Receiver<? super DecodedBufferedImage> nextStage) {
    this.nextStage = nextStage;
  }

  @Override
  public void receive(DecodedRaster raster, DecoderParameters parameters) throws JPEG2000Exception {
    ColorModel cm = null;

    // FIXME
    switch (raster.codestream.numComps){
      case 1 :
        cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[]{
          8
        }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        break;
      case 3 :
        cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{
            8, 8, 8
        }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        break;
      default :
        throw new JPEG2000Exception(GeneralMessages.UNSUPPORTED_COMPONENT_COUNT, raster.codestream.numComps);
    }

    final BufferedImage image = new BufferedImage(cm, raster.raster, false, new Hashtable<>());

    nextStage.receive(new DecodedBufferedImage(image, raster.codestream), parameters);
  }
}
