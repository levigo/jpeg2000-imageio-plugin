/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jadice.jpeg2000.internal;

import org.jadice.util.log.Logger;
import org.jadice.util.log.LoggerFactory;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);

  private static final byte CM_TYPE_COMPONENT = 2;
  private static final byte CM_TYPE_DIRECT = 1;
  private static final byte CM_TYPE_INDEXED = 0;

  public static BufferedImage readImage(InputStream is) throws IOException, ClassNotFoundException {
    final ObjectInputStream in = new ObjectInputStream(new InflaterInputStream(is));

    final int transferType = in.readInt();
    final int width = in.readInt();
    final int height = in.readInt();

    // reconstruct the color model
    ColorModel cm = null;
    final int pixelSize = in.readInt();
    final int transparency = in.readInt();
    switch (in.readByte()){
      case CM_TYPE_INDEXED :
        final int mapSize = in.readInt();
        final int transparentPixel = in.readInt();

        cm = new IndexColorModel(pixelSize, mapSize, (int[]) in.readObject(), 0,
            transparency == Transparency.TRANSLUCENT, transparentPixel, transferType);
        break;
      case CM_TYPE_DIRECT :
        cm = new DirectColorModel(pixelSize, in.readInt(), in.readInt(), in.readInt(), in.readInt());
        break;
      case CM_TYPE_COMPONENT :
        cm = new ComponentColorModel((ColorSpace) in.readObject(), (int[]) in.readObject(), false, false,
            Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        break;
      default :
        throw new IOException("Unknown ColorModel type received");
    }

    // read data buffer
    DataBuffer db = null;
    switch (transferType){
      case DataBuffer.TYPE_BYTE :
        db = new DataBufferByte((byte[][]) in.readObject(), in.readInt(), (int[]) in.readObject());
        break;
      case DataBuffer.TYPE_INT :
        db = new DataBufferInt((int[][]) in.readObject(), in.readInt(), (int[]) in.readObject());
        break;
      default :
        throw new IOException("Can't write this data buffer: " + db);
    }

    final SampleModel sampleModel = cm.createCompatibleSampleModel(width, height);
    final WritableRaster raster = Raster.createWritableRaster(sampleModel, db, null);
    return new BufferedImage(cm, raster, false, null);
  }

  public static void saveImage(BufferedImage image, OutputStream os) throws IOException {
    final ObjectOutputStream out = new ObjectOutputStream(new DeflaterOutputStream(os));

    try {
      // write transfer type
      final int transferType = image.getRaster().getTransferType();
      out.writeInt(transferType);
      out.writeInt(image.getWidth());
      out.writeInt(image.getHeight());

      // determine type of color model
      final ColorModel cm = image.getColorModel();
      out.writeInt(cm.getPixelSize());
      out.writeInt(cm.getTransparency());
      if (cm instanceof IndexColorModel) {
        final IndexColorModel icm = (IndexColorModel) cm;
        out.writeByte(CM_TYPE_INDEXED);
        out.writeInt(icm.getMapSize());

        // decide what kind of transparency to use
        out.writeInt(icm.getTransparentPixel());

        // write components
        final int cmap[] = new int[icm.getMapSize()];
        icm.getRGBs(cmap);
        out.writeObject(cmap);
      } else if (cm instanceof DirectColorModel) {
        final DirectColorModel dcm = (DirectColorModel) cm;
        out.write(CM_TYPE_DIRECT);
        out.writeInt(dcm.getRedMask());
        out.writeInt(dcm.getGreenMask());
        out.writeInt(dcm.getBlueMask());
        out.writeInt(dcm.getAlphaMask());
      } else if (cm instanceof ComponentColorModel) {
        final ComponentColorModel ccm = (ComponentColorModel) cm;
        out.write(CM_TYPE_COMPONENT);
        out.writeObject(ccm.getColorSpace());
        out.writeObject(ccm.getComponentSize());
      } else
        throw new IOException("Can't write an image with this color model: " + cm);

      // write raster
      final DataBuffer db = image.getRaster().getDataBuffer();
      switch (transferType){
        case DataBuffer.TYPE_BYTE :
          final DataBufferByte dbb = (DataBufferByte) db;
          out.writeObject(dbb.getBankData());
          out.writeInt(dbb.getSize());
          out.writeObject(dbb.getOffsets());
          break;
        case DataBuffer.TYPE_INT :
          final DataBufferInt dbi = (DataBufferInt) db;
          out.writeObject(dbi.getBankData());
          out.writeInt(dbi.getSize());
          out.writeObject(dbi.getOffsets());
          break;
        default :
          throw new IOException("Can't write this data buffer: " + db);
      }
    } finally {
      out.close();
    }
  }

  public static void saveDiff(BufferedImage actual, BufferedImage expected, File failedDir, String testNameName)
      throws IOException {

    final Iterator<ImageWriter> i = ImageIO.getImageWritersByFormatName("PNG");
    if (!i.hasNext()) {
      LOGGER.error("Can't save diff: no writer for PNG");
      return;
    }
    final ImageWriter pngWriter = i.next();

    if (null != expected) {
      final File f = new File(failedDir, testNameName + "-expected.png");

      // support testNames with slashes
      f.getParentFile().mkdirs();

      final MemoryCacheImageOutputStream os = new MemoryCacheImageOutputStream(new FileOutputStream(f));
      pngWriter.setOutput(os);
      pngWriter.write(expected);
      os.close();
    }

    if (null != actual) {
      final File f = new File(failedDir, testNameName + "-actual.png");

      // support testNames with slashes
      f.getParentFile().mkdirs();

      final MemoryCacheImageOutputStream os = new MemoryCacheImageOutputStream(new FileOutputStream(f));
      pngWriter.setOutput(os);
      pngWriter.write(actual);
      os.close();
    }
  }

}
