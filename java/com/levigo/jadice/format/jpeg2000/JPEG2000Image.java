package com.levigo.jadice.format.jpeg2000;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

import com.levigo.jadice.document.internal.model.cs.ColorSpaceDeviceCMYK;
import com.levigo.jadice.document.internal.model.cs.ColorSpaceDeviceRGB;
import com.levigo.jadice.document.internal.render.j2d.rendered.BaseRenderedImage;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.decode.Decoder;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.CodestreamDecoder;
import com.levigo.jadice.format.jpeg2000.internal.image.GridRegion;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.Regions;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;
import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

public class JPEG2000Image extends BaseRenderedImage {

  private static final QualifiedLogger LOGGER = LoggerFactory.getQualifiedLogger(JPEG2000Image.class);

  private final Codestream codestream;

  private ColorModel colorModel;
  private SampleModel sampleModel;

  public JPEG2000Image(Codestream codestream) {
    this.codestream = codestream;

    final GridRegion gridElement = codestream.region();
    final Region canvas = gridElement.absolute();
    setBounds(canvas.bounds());
  }

  @Override
  public ColorModel getColorModel() {
    synchronized (codestream) {
      if (colorModel == null) {
        switch (codestream.numComps){
          case 1 :
            colorModel = createGrayscaleColorModel();
            break;

          case 3 :
            colorModel = createRGBColorModel();
            break;

          case 4 :
            colorModel = createCMYKColorModel();
            break;

          default :
            throw new UnsupportedOperationException(
                "Component configurations other than one or three are currently not supported.");
        }
      }
    }

    return colorModel;
  }

  private static ColorModel createRGBColorModel() {
    return new ComponentColorModel(ColorSpaceDeviceRGB.INSTANCE, false, false, Transparency.OPAQUE,
        DataBuffer.TYPE_BYTE);
  }

  private ColorModel createGrayscaleColorModel() {
    return new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[]{
        8
    }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
  }

  private ColorModel createCMYKColorModel() {
    return new ComponentColorModel(ColorSpaceDeviceCMYK.INSTANCE, new int[]{
        8, 8, 8, 8
    }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
  }

  @Override
  public SampleModel getSampleModel() {
    synchronized (codestream) {
      if (sampleModel == null) {
        try {
          final GridRegion gridElement = codestream.region();
          final Region canvas = gridElement.absolute();
          sampleModel = getColorModel().createCompatibleSampleModel(canvas.size.x, canvas.size.y);
        } catch (Exception e) {
          throw new UnsupportedOperationException(
              "Component configurations other than one or three are currently not supported.");
        }
      }
    }

    return sampleModel;
  }

  @Override
  protected Raster computeRect(Rectangle bounds) {
    final DecoderParameters parameters = new DecoderParameters();
    parameters.region = Regions.createFromRectangle(bounds);

    try {
      synchronized (codestream) {

        final Decoder decoder;
        // if (Regions.equalAreas(codestream.canvas, bounds)) {
        // Decode the codestream fully and sequentially.
        decoder = new CodestreamDecoder(codestream);
        // } else {
        // Decode the codestream partially with a tile-based pull concept.
        // decoder = new PullDecoder(getSampleModel(), getColorModel());
        // }
        return  decoder.decode(parameters);
      }
    } catch (JPEG2000Exception e) {
      LOGGER.error(GeneralMessages.PROCESSING_ERROR, e);

      // re-throw as a runtime exception
      throw new JPEG2000RuntimeException(e.getMessageID(), e.getCause(), e.getArguments());
    }
  }

  @Override
  public int getNumXTiles() {
    return codestream.numTiles.x;
  }

  @Override
  public int getNumYTiles() {
    return codestream.numTiles.y;
  }

  @Override
  public int getTileGridXOffset() {
    return codestream.tilePartition.x0();
  }

  @Override
  public int getTileGridYOffset() {
    return codestream.tilePartition.y0();
  }

  @Override
  public int getTileWidth() {
    return codestream.tilePartition.width();
  }

  @Override
  public int getTileHeight() {
    return codestream.tilePartition.height();
  }
}
