package com.levigo.jadice.format.jpeg2000.internal.debug;

import static com.levigo.jadice.format.jpeg2000.internal.debug.IntegerParameter.integer;

public class IntegerParameterFactory implements ParameterFactory {
  @Override
  public Parameter create() {
    return integer();
  }
}
