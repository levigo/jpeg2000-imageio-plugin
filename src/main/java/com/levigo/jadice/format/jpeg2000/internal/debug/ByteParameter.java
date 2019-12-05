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
package com.levigo.jadice.format.jpeg2000.internal.debug;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public class ByteParameter implements Parameter<Byte> {
  private byte value;

  public static ByteParameter byteParam() {
    return new ByteParameter((byte) 0);
  }

  public static ByteParameter byteParam(byte value) {
    return new ByteParameter(value);
  }

  private ByteParameter(byte value) {
    this.value = value;
  }

  @Override
  public Byte value() {
    return value;
  }

  @Override
  public Byte read(ImageInputStream source) throws IOException {
    return value = (byte) (source.readBits(8) & 0xFF);
  }

  @Override
  public void write(ImageOutputStream sink) throws IOException {
    sink.writeBits(value, 8);
  }

  @Override
  public boolean matches(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ByteParameter))
      return false;

    ByteParameter that = (ByteParameter) o;

    return value == that.value;
  }

  @Override
  public String toString() {
    return "byte " + value;
  }
}
