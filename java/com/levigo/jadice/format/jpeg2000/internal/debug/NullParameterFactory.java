package com.levigo.jadice.format.jpeg2000.internal.debug;

import static com.levigo.jadice.format.jpeg2000.internal.debug.NullParameter.noParam;

public class NullParameterFactory implements ParameterFactory<Void> {
  @Override
  public Parameter<Void> create() {
    return noParam();
  }

}
