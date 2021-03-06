/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jadice.jpeg2000.internal.debug.tier2;

import static org.jadice.jpeg2000.internal.debug.Protocol.DEFAULT_TOKEN_LENGTH;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.jadice.jpeg2000.internal.debug.BooleanParameterFactory;
import org.jadice.jpeg2000.internal.debug.ByteParameterFactory;
import org.jadice.jpeg2000.internal.debug.IntegerParameterFactory;
import org.jadice.jpeg2000.internal.debug.NullParameterFactory;
import org.jadice.jpeg2000.internal.debug.Parameter;
import org.jadice.jpeg2000.internal.debug.ParameterFactory;
import org.jadice.jpeg2000.internal.debug.ProtocolToken;

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
