package com.levigo.jadice.format.jpeg2000.internal.debug;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public class IntegerParameter implements Parameter<Integer> {

  private Integer value;

  public static IntegerParameter integer() {
    return new IntegerParameter(0);
  }

  public static IntegerParameter integer(int value) {
    return new IntegerParameter(value);
  }

  public IntegerParameter(int value) {
    this.value = value;
  }

  @Override
  public Integer value() {
    return value;
  }

  @Override
  public Integer read(ImageInputStream source) throws IOException {
    return value = (int) source.readBits(32);
  }

  @Override
  public void write(ImageOutputStream sink) throws IOException {
    sink.writeBits(value, 32);
  }

  @Override
  public boolean matches(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof IntegerParameter))
      return false;

    IntegerParameter that = (IntegerParameter) o;

    return value != null ? value.equals(that.value) : that.value == null;
  }

  @Override
  public String toString() {
    return "int " + value;
  }

}
