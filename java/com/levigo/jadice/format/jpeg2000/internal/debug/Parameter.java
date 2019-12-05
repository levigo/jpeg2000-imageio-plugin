package com.levigo.jadice.format.jpeg2000.internal.debug;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public interface Parameter<V> {
  
  V value();

  V read(ImageInputStream source) throws IOException;

  void write(ImageOutputStream sink) throws IOException;

  boolean matches(Object o);
  
}
