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
package com.levigo.jadice.format.jpeg2000.internal.debug.tier1;

import static com.levigo.jadice.format.jpeg2000.internal.debug.Protocol.DEFAULT_TOKEN_LENGTH;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.levigo.jadice.format.jpeg2000.internal.debug.BitsParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.BooleanParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.IntegerParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.Parameter;
import com.levigo.jadice.format.jpeg2000.internal.debug.ParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolToken;

public enum BlockCodingToken implements ProtocolToken, Parameter {

  RunMode(0x0, new BooleanParameterFactory(), "Run mode"),
  RunLength(0x1, new BitsParameterFactory(2), "Run length"),
  ContextWord(0x2, new IntegerParameterFactory(), "Context Word"),
  Significance(0x3, new BooleanParameterFactory(), "Significance"),
  Sign(0x4, new BitsParameterFactory(1), "Sign"),
  Sample(0x5, new IntegerParameterFactory(), "Sample Update"),
  SegmentationMark(0x6, new BitsParameterFactory(4), "Segmentation Marks"),
  Refinement(0x7, new BooleanParameterFactory(), "Refinement"),
  Finish(0xF, new IntegerParameterFactory(), "Finish");

  private final int symbol;
  private final ParameterFactory parameterFactory;
  private final String description;

  BlockCodingToken(int symbol, ParameterFactory parameterFactory, String description) {
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
