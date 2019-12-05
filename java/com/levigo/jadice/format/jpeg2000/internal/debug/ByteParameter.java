package com.levigo.jadice.format.jpeg2000.internal.debug;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public class ByteParameter implements Parameter<Byte> {
  private byte value;

  public static ByteParameter byteParam() {
    return new ByteParameter((byte) 0);
  }

  public static ByteParameter byteParam(byte value) {
    return new ByteParameter(value);
  }

  private ByteParameter(byte value) {
    this.value = value;
  }

  @Override
  public Byte value() {
    return value;
  }

  @Override
  public Byte read(ImageInputStream source) throws IOException {
    return value = (byte) (source.readBits(8) & 0xFF);
  }

  @Override
  public void write(ImageOutputStream sink) throws IOException {
    sink.writeBits(value, 8);
  }

  @Override
  public boolean matches(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ByteParameter))
      return false;

    ByteParameter that = (ByteParameter) o;

    return value == that.value;
  }

  @Override
  public String toString() {
    return "byte " + value;
  }
}
