package com.levigo.jadice.format.jpeg2000.internal.debug;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public class NullParameter implements Parameter<Void> {

  private static final NullParameter INSTANCE = new NullParameter();

  public static NullParameter noParam() {
    return INSTANCE;
  }

  @Override
  public Void value() {
    return null;
  }

  @Override
  public Void read(ImageInputStream source) throws IOException {
    return null; // nothing to read
  }

  @Override
  public void write(ImageOutputStream sink) throws IOException {
    // nothing to write
  }

  @Override
  public boolean matches(Object o) {
    return this == o;
  }

  @Override
  public String toString() {
    return "-";
  }
}
