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

public class NullParameter implements Parameter<Void> {

  private static final NullParameter INSTANCE = new NullParameter();

  public static NullParameter noParam() {
    return INSTANCE;
  }

  @Override
  public Void value() {
    return null;
  }

  @Override
  public Void read(ImageInputStream source) throws IOException {
    return null; // nothing to read
  }

  @Override
  public void write(ImageOutputStream sink) throws IOException {
    // nothing to write
  }

  @Override
  public boolean matches(Object o) {
    return this == o;
  }

  @Override
  public String toString() {
    return "-";
  }
}
