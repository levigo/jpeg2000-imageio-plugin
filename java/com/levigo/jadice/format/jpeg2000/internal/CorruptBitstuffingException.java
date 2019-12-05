package com.levigo.jadice.format.jpeg2000.internal;

import javax.imageio.stream.ImageInputStream;

public class CorruptBitstuffingException extends Exception {

  private static final long serialVersionUID = 1L;

  private final ImageInputStream source;
  private final long startPosition;

  public CorruptBitstuffingException(ImageInputStream source, long startPosition) {
    this.source = source;
    this.startPosition = startPosition;
  }

  public ImageInputStream getSource() {
    return source;
  }

  public long getStartPosition() {
    return startPosition;
  }
}
