package com.levigo.jadice.format.jpeg2000;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.junit.Ignore;

@Ignore
public class ImageFrame extends JFrame {
  private static final long serialVersionUID = 1L;

  public static void main(String[] args) {
    int w = 250;
    int h = 250;

    // (w+7) / 8 entspricht Aufrundung!
    int scanlineStride = (w + 7) / 8;

    // hier sind die Daten
    byte data[] = new byte[h * scanlineStride];

    // dummy-Daten erzeugen
    for (int i = 0; i < data.length; i++)
      data[i] = (byte) i;

    new ImageFrame(data, w, h, scanlineStride);
  }

  public ImageFrame(byte data[], int w, int h, int scanlineStride) {
    super("Demobild");

    // Color-Model sagt: bit = 0 -> schwarz, bit = 1 -> weiss. Ggf. umdrehen.
    ColorModel colorModel = new IndexColorModel(1, 2, new byte[]{
        (byte) 0xff, 0x00
    }, new byte[]{
        (byte) 0xff, 0x00
    }, new byte[]{
        (byte) 0xff, 0x00
    });

    DataBuffer dataBuffer = new DataBufferByte(data, data.length);
    SampleModel sampleModel = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, w, h, 1, scanlineStride, 0);
    WritableRaster writableRaster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));

    BufferedImage image = new BufferedImage(colorModel, writableRaster, false, null);

    ImageComponent imageComponent = new ImageComponent(image);
    // imageComponent.setScale(4);

    JScrollPane sp = new JScrollPane(imageComponent);

    setContentPane(sp);

    pack();
    setSize(new Dimension(1600, 900));
    setVisible(true);

    try {
      System.in.read();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public ImageFrame(BufferedImage bufferedImage) {
    this(bufferedImage, 1);
  }

  public ImageFrame(BufferedImage bufferedImage, int scaleFactor) {
    super("ImageFrame");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ImageComponent imageComponent = new ImageComponent(bufferedImage);
    imageComponent.setScale(scaleFactor);

    JScrollPane sp = new JScrollPane(imageComponent);

    setContentPane(sp);

    setSize(new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight()));
    setVisible(true);
    pack();

    try {
      System.in.read();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
