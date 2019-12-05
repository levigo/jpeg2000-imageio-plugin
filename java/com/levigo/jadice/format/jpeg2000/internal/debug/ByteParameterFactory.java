package com.levigo.jadice.format.jpeg2000.internal.debug;

import static com.levigo.jadice.format.jpeg2000.internal.debug.ByteParameter.byteParam;

public class ByteParameterFactory implements ParameterFactory<Byte> {
  @Override
  public Parameter<Byte> create() {
    return byteParam();
  }
}
