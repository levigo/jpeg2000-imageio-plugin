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

public class BooleanParameter implements Parameter<Boolean> {

  private boolean value;

  public static BooleanParameter bool() {
    return new BooleanParameter(true);
  }

  public static BooleanParameter bool(boolean value) {
    return new BooleanParameter(value);
  }

  private BooleanParameter(boolean value) {
    this.value = value;
  }

  @Override
  public Boolean value() {
    return value;
  }

  @Override
  public Boolean read(ImageInputStream source) throws IOException {
    return value = source.readBit() == 1;
  }

  @Override
  public void write(ImageOutputStream sink) throws IOException {
    sink.writeBit(value ? 1 : 0);
  }

  @Override
  public boolean matches(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof BooleanParameter))
      return false;

    BooleanParameter that = (BooleanParameter) o;

    return value == that.value;
  }

  @Override
  public String toString() {
    return "" + value;
  }
}
