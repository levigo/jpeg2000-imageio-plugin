package com.levigo.jadice.format.jpeg2000.internal.debug.dwt;


import static com.levigo.jadice.format.jpeg2000.internal.debug.Protocol.DEFAULT_TOKEN_LENGTH;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.levigo.jadice.format.jpeg2000.internal.debug.IntegerParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.Parameter;
import com.levigo.jadice.format.jpeg2000.internal.debug.ParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolToken;

public enum DWTToken implements ProtocolToken, Parameter {


  Finish(0xF, new IntegerParameterFactory(), "Finished with element counter"), ;

  private final int symbol;
  private final ParameterFactory parameterFactory;
  private final String description;

  DWTToken(int symbol, ParameterFactory parameterFactory, String description) {
    this.symbol = symbol;
    this.parameterFactory = parameterFactory;
    this.description = description;
  }

  public String description() {
    return description;
  }

  @Override
  public Object value() {
    return symbol;
  }

  @Override
  public Object read(ImageInputStream source) throws IOException {
    return source.readBits(DEFAULT_TOKEN_LENGTH);
  }

  @Override
  public void write(ImageOutputStream sink) throws IOException {
    sink.writeBits(symbol, DEFAULT_TOKEN_LENGTH);
  }

  @Override
  public boolean matches(Object o) {
    return this == o || (o instanceof Long && symbol == (Long) o);
  }

  @Override
  public ParameterFactory getParameterFactory() {
    return parameterFactory;
  }

  public Parameter parameter() {
    return parameterFactory.create();
  }
}
