package com.levigo.jadice.format.jpeg2000.internal.debug;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public class BooleanParameter implements Parameter<Boolean> {

  private boolean value;

  public static BooleanParameter bool() {
    return new BooleanParameter(true);
  }

  public static BooleanParameter bool(boolean value) {
    return new BooleanParameter(value);
  }

  private BooleanParameter(boolean value) {
    this.value = value;
  }

  @Override
  public Boolean value() {
    return value;
  }

  @Override
  public Boolean read(ImageInputStream source) throws IOException {
    return value = source.readBit() == 1;
  }

  @Override
  public void write(ImageOutputStream sink) throws IOException {
    sink.writeBit(value ? 1 : 0);
  }

  @Override
  public boolean matches(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof BooleanParameter))
      return false;

    BooleanParameter that = (BooleanParameter) o;

    return value == that.value;
  }

  @Override
  public String toString() {
    return "" + value;
  }
}
