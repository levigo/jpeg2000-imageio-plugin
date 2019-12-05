package com.levigo.jadice.format.jpeg2000.internal.debug;

public class BitsParameterFactory implements ParameterFactory {
  private final int numBits;

  public BitsParameterFactory(int numBits) {
    this.numBits = numBits;
  }

  @Override
  public Parameter create() {
    return new BitsParameter(numBits);
  }
}
