package com.levigo.jadice.format.jpeg2000.internal.debug.tier2;

import static com.levigo.jadice.format.jpeg2000.internal.debug.Protocol.DEFAULT_TOKEN_LENGTH;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.levigo.jadice.format.jpeg2000.internal.debug.BooleanParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.ByteParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.IntegerParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.NullParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.Parameter;
import com.levigo.jadice.format.jpeg2000.internal.debug.ParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolToken;

enum PacketHeaderToken implements ProtocolToken, Parameter {
  PacketHeaderStart(0x0, new NullParameterFactory(), "start of packet"),
  Empty(0x1, new BooleanParameterFactory(), "bit for zero or non-zero length packet"),
  CodeBlockStart(0x2, new NullParameterFactory(), "start of code-block"),
  //CodeBlockStart(0x2, new CodeBlockParameterFactory(), "start of code-block"),
  BetaChange(0x3, new ByteParameterFactory(), "beta change"),
  NotIncludedFirstTime(0x4, new NullParameterFactory(), "not included for the first time"),
  NotIncluded(0x5, new NullParameterFactory(), "not included"),
  IncludedFirstTime(0x6, new NullParameterFactory(), "included for the first time"),
  Included(0x7, new NullParameterFactory(), "included"),
  NewPasses(0x8, new IntegerParameterFactory(), "number of new passes"),
  LBlock(0x9, new ByteParameterFactory(), "LBlock"),
  SegmentBytes(0xA, new IntegerParameterFactory(), "segment bytes"),
  IncludedEmpty(0xB, new NullParameterFactory(), "included empty contribution"),
  Finish(0xF, new IntegerParameterFactory(), "Finish")
  ;

  private final int symbol;
  private final ParameterFactory parameterFactory;
  private final String description;

  PacketHeaderToken(int symbol, ParameterFactory parameterFactory, String description) {
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
