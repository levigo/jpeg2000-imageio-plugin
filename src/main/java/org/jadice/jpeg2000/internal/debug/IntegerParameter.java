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
package org.jadice.jpeg2000.internal.debug;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public class IntegerParameter implements Parameter<Integer> {

  private Integer value;

  public static IntegerParameter integer() {
    return new IntegerParameter(0);
  }

  public static IntegerParameter integer(int value) {
    return new IntegerParameter(value);
  }

  public IntegerParameter(int value) {
    this.value = value;
  }

  @Override
  public Integer value() {
    return value;
  }

  @Override
  public Integer read(ImageInputStream source) throws IOException {
    return value = (int) source.readBits(32);
  }

  @Override
  public void write(ImageOutputStream sink) throws IOException {
    sink.writeBits(value, 32);
  }

  @Override
  public boolean matches(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof IntegerParameter))
      return false;

    IntegerParameter that = (IntegerParameter) o;

    return value != null ? value.equals(that.value) : that.value == null;
  }

  @Override
  public String toString() {
    return "int " + value;
  }

}
