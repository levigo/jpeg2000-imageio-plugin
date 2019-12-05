package com.levigo.jadice.format.jpeg2000.internal.debug;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public class BitsParameter implements Parameter<Long> {

  private Long value;
  private int numBits;

  public static BitsParameter bits(long value, int numBits) {
    final BitsParameter bitsParameter = new BitsParameter(numBits);
    bitsParameter.value = value;
    return bitsParameter;
  }

  BitsParameter(int numBits) {
    this.numBits = numBits;
  }

  @Override
  public Long value() {
    return value;
  }

  @Override
  public Long read(ImageInputStream source) throws IOException {
    return value = source.readBits(numBits);
  }

  @Override
  public void write(ImageOutputStream sink) throws IOException {
    sink.writeBits(value, numBits);
  }

  @Override
  public boolean matches(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof BitsParameter))
      return false;

    BitsParameter that = (BitsParameter) o;

    return value != null ? value.equals(that.value) : that.value == null;
  }

  @Override
  public String toString() {
    return "bits " + value;
  }
}
