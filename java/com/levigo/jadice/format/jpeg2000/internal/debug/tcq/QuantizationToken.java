package com.levigo.jadice.format.jpeg2000.internal.debug.tcq;

import static com.levigo.jadice.format.jpeg2000.internal.debug.Protocol.DEFAULT_TOKEN_LENGTH;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.levigo.jadice.format.jpeg2000.internal.debug.IntegerParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.NullParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.Parameter;
import com.levigo.jadice.format.jpeg2000.internal.debug.ParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolToken;

public enum QuantizationToken implements ProtocolToken, Parameter {

  Downshift(0x0, new IntegerParameterFactory(), "Downshift value"),
  ValueBefore(0x1, new IntegerParameterFactory(), "Value before quantization"),
  ValueAfter(0x2, new IntegerParameterFactory(), "Value after quantization"),
  FixedPoint(0x3, new NullParameterFactory(), "Fixed point"),
  FillWithZeros(0x4, new NullParameterFactory(), "Fill with zeros"),
  
  
  Finish(0xF, new IntegerParameterFactory(), "Finished with element counter"),
  ;
  
  private final int symbol;
  private final ParameterFactory parameterFactory;
  private final String description;

  QuantizationToken(int symbol, ParameterFactory parameterFactory, String description) {
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
