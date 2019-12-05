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
package org.jadice.jpeg2000.internal.io;

import javax.imageio.stream.ImageInputStream;

import org.jadice.jpeg2000.internal.marker.Marker;

import java.io.IOException;

public class MarkerReader {

  /**
   * Reads the given input until a valid marker code was found.
   *
   * @param source the input providing codestream bytes
   * @return a {@link Marker} if found.
   * @throws java.io.IOException if general I/O error occured.
   * @throws java.io.EOFException
   */
  public Marker next(ImageInputStream source) throws IOException {
    int markerCode;

    int read = (source.readByte() & 0xFF);
    do {
      while (read != 0xFF) {
        read = (source.readByte() & 0xFF);
      }

      markerCode = (read << 8) | (read = ((source.readByte() & 0xFF)));

    } while (!Marker.isValid(markerCode));

    return Marker.byCode(markerCode);
  }

  /**
   * This method has two different behaviours. If search is allowed, {@link org.jadice.jpeg2000.internal.io.MarkerReader#next(javax.imageio.stream.ImageInputStream)}
   * is called. If search is not allowed, two bytes will be read. If the result is a valid marker code a {@link
   * org.jadice.jpeg2000.internal.marker.Marker} will be created and returned. If the result is not a
   * valid marker code <code>null</code> is returned.
   *
   * @param source the input providing codestream bytes
   * @param search flag if the reading operation should search for the next marker code and therefore read until one is
   *               recognized.
   * @return A {@link Marker} if present or found. Otherwise <code>null</code>.
   * @throws java.io.IOException
   */
  public Marker next(ImageInputStream source, boolean search) throws IOException {
    if (search) {
      return next(source);
    }

    final int markerCode = source.readUnsignedShort();
    if (Marker.isValid(markerCode)) {
      return Marker.byCode(markerCode);
    }

    return null;
  }
}
