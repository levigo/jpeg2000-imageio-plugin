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
package com.levigo.jadice.format.jpeg2000.internal.pgx;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jadice.document.io.IOUtils;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.ImageFrame;
import org.jadice.util.base.Numbers;

public class PGXImageReader {

  private static final String SEPARATOR = new String(new byte[]{0x20});

  private final ImageInputStream source;
  
  private final ByteOrder byteOrder;
  private final boolean signed;
  private final int bitDepth;
  private final int width;
  private final int height;
  
  private final long payloadStartPosition;

  public PGXImageReader(ImageInputStream source) throws IOException {
    this.source = source;

    synchronized (source) {
      source.seek(0);

      final String header = source.readLine();
      final String[] headerParts = header.split(SEPARATOR);

      if (headerParts.length < 5 || headerParts.length > 6) {
        throw new IOException("Illegal header size " + headerParts.length);
      }

      if (!"PG".equalsIgnoreCase(headerParts[0])) {
        throw new IOException("Illegal PG marker " + headerParts[0]);
      }

      byteOrder = getByteOrder(headerParts);
      signed = isSigned(headerParts);
      bitDepth = getBitDepth(headerParts);
      width = getWidth(headerParts);
      height = getHeight(headerParts);

      payloadStartPosition = source.getStreamPosition();
    }
  }
  
  public ColorModel getColorModel() {
    final byte[] c = new byte[256];
    for (int i = 0; i < c.length; i++) {
      c[i] = (byte) i;
    }
    return new IndexColorModel(8, 256, c, c, c);
  }

  public WritableRaster getRaster() throws IOException {
    synchronized (source) {
      source.seek(payloadStartPosition);

      final SampleReader sampleReader = createSampleReader(source, bitDepth, signed);

      final WritableRaster raster = Raster.createPackedRaster(DataBuffer.TYPE_BYTE, width, height, 1, bitDepth, null);
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          raster.setSample(x, y, 0, sampleReader.readNextSample());
        }
      }

      return raster;
    }
  }

  private ByteOrder getByteOrder(String[] headerParts) throws IOException {
    final String byteOrderString = headerParts[1];
    if ("ML".equalsIgnoreCase(byteOrderString)) {
      return ByteOrder.BIG_ENDIAN;
    } else if ("LM".equalsIgnoreCase(byteOrderString)) {
      return ByteOrder.LITTLE_ENDIAN;
    } else {
      throw new IOException("Illegal byte order " + byteOrderString);
    }
  }

  private boolean isSigned(String[] headerParts) {
    switch (headerParts.length){
      case 5:
        return headerParts[2].startsWith("-");
      case 6:
        return "-".equalsIgnoreCase(headerParts[2]);
      default:
        return false;
    }
  }

  private int getBitDepth(String[] headerParts) throws IOException {
    switch (headerParts.length){
      case 5:
        return Math.abs(Integer.parseInt(headerParts[2].replace("+", "").replace("-", "")));
      case 6:
        return Math.abs(Integer.parseInt(headerParts[3]));
    }

    throw new IOException("Illegal header format detected while bit depth interpretation");
  }

  private int getWidth(String[] headerParts) throws IOException {
    switch (headerParts.length){
      case 5:
        return Integer.parseInt(headerParts[3]);
      case 6:
        return Integer.parseInt(headerParts[4]);
    }

    throw new IOException("Illegal header format detected while width interpretation");
  }

  private int getHeight(String[] headerParts) throws IOException {
    switch (headerParts.length){
      case 5:
        return Integer.parseInt(headerParts[4]);
      case 6:
        return Integer.parseInt(headerParts[5]);
    }

    throw new IOException("Illegal header format detected while height interpretation");
  }

  private SampleReader createSampleReader(ImageInputStream source, int bitDepth, boolean signed) {
    if (Numbers.isWithin(bitDepth, 1, 8)) {
      return new ByteReader(source, signed);
    } else if (Numbers.isWithin(bitDepth, 9, 16)) {
      return new ShortReader(source, signed);
    } else if (Numbers.isWithin(bitDepth, 17, 32)) {
      return new IntReader(source, signed);
    } else {
      throw new IllegalStateException("Illegal bit depth " + bitDepth);
    }
  }

  private interface SampleReader {

    int readNextSample() throws IOException;
  }

  private static abstract class SampleReaderBase implements SampleReader {

    protected final ImageInputStream source;
    protected final boolean signed;

    protected SampleReaderBase(ImageInputStream source, boolean signed) {
      this.source = source;
      this.signed = signed;
    }

    @Override
    public int readNextSample() throws IOException {
      if (signed) {
        return readSigned();
      } else {
        return readUnsigned();
      }
    }

    protected abstract int readSigned() throws IOException;

    protected abstract int readUnsigned() throws IOException;

  }

  private class ByteReader extends SampleReaderBase {

    public ByteReader(ImageInputStream source, boolean signed) {
      super(source, signed);
    }

    @Override
    protected int readSigned() throws IOException {
      return source.readByte();
    }

    @Override
    protected int readUnsigned() throws IOException {
      return source.readUnsignedByte();
    }
  }

  private class ShortReader extends SampleReaderBase {

    public ShortReader(ImageInputStream source, boolean signed) {
      super(source, signed);
    }

    @Override
    protected int readSigned() throws IOException {
      return source.readShort();
    }

    @Override
    protected int readUnsigned() throws IOException {
      return source.readUnsignedShort();
    }
  }

  private class IntReader extends SampleReaderBase {

    public IntReader(ImageInputStream source, boolean signed) {
      super(source, signed);
    }

    @Override
    protected int readSigned() throws IOException {
      return source.readInt();
    }

    @Override
    protected int readUnsigned() throws IOException {
      return (int) (source.readUnsignedInt() & 0xFFFFFFFF);
    }
  }

  public static void main(String[] args) throws IOException {
    String resourcePath = "/codestreams/profile1/c0p1_07.pgx";
    final Class<PGXImageReader> cls = PGXImageReader.class;
    final InputStream inputStream = cls.getResourceAsStream(resourcePath);
    final SeekableInputStream source = IOUtils.wrap(inputStream);
    final PGXImageReader pgxImageReader = new PGXImageReader(source);
    final ColorModel colorModel = pgxImageReader.getColorModel();
    final WritableRaster raster = pgxImageReader.getRaster();
    final BufferedImage bufferedImage = new BufferedImage(colorModel, raster, false, null);
    
    new ImageFrame(bufferedImage, 2);
  }
}
