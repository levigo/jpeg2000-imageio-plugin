package com.levigo.jadice.format.jpeg2000.internal.debug;

import static com.levigo.jadice.format.jpeg2000.internal.debug.BooleanParameter.bool;

public class BooleanParameterFactory implements ParameterFactory<Boolean> {
  @Override
  public Parameter<Boolean> create() {
    return bool();
  }
}
