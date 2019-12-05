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
package com.levigo.jadice.format.jpeg2000;

import com.levigo.jadice.format.jpeg2000.internal.marker.Marker;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

/**
 * This class provides methods to determine if the given input is a JPEG2000 codestream or a JP2/JPX file format. This
 * indicates if this module can handle given input.
 */
public class JPEG2000Matcher {

  private static final int[] CODESTREAM_HEADER = {
      (0xFF4F >> 8) & 0xFF, (0xFF4F & 0xFF),
      (0xFF51 >> 8) & 0xFF, (0xFF51 & 0xFF)
  };

  private static final int[] FILEFORMAT_HEADER = {
      0x00, 0x00, 0x00, 0x0C, 0x6A, 0x50, 0x20, 0x20, 0x0D, 0x0A, 0x87, 0x0A
  };

  /**
   * This function determines whether the source is valid input and is JPEG2000-conform. If this function returns
   * <code>true</code> the decoder should be able to process the source if it's valid.
   *
   * @param source the input object to be examined.
   * @return <code>false</code> if the source is not kind of {@link ImageInputStream} or is identified as non-JPEG2000
   * data; <code>true</code> if source was identified as a valid type, and JPEG2000 codestream or JP2/JPX file format.
   * @throws IOException if something went wrong while reading the source.
   * @see #isCodestream(ImageInputStream)
   * @see #isFileFormat(ImageInputStream)
   */
  public static boolean matches(final Object source) throws IOException {
    if (!(source instanceof ImageInputStream)) {
      return false;
    }

    final ImageInputStream input = (ImageInputStream) source;

    return isFileFormat(input) || isCodestream(input);
  }

  /**
   * This function determines if the given stream starts with a valid codestream header. In particular, the first two
   * bytes are compared to {@link Marker#SOC} marker's code, the second two bytes are compared to {@link Marker#SIZ}.
   * After all, the function resets the position to the initial one.
   *
   * @param input the input to be examined.
   * @return <code>false</code> if the input is not a JPEG2000 codestream;<br>
   * <code>true</code> if input was identified
   * as a JPEG2000 codestream.
   * @throws IOException if something went wrong while reading the source.
   */
  public static boolean isCodestream(final ImageInputStream input) throws IOException {
    return matches(input, CODESTREAM_HEADER);
  }

  /**
   * This function determines if the given stream starts with a valid JP2 or JPX file format header. In particular, the
   * first twelve bytes to the file format header defined in <i>ITU-T.800, I.5.1, JPEG 2000 Signature box</i>. After
   * all, the function resets the position to the initial one.
   *
   * @param input the input to be examined.
   * @return <code>false</code> if the input is not JP2 or JPX file format.<br>
   * <code>true</code> if input was identified as a JP2 or JPX file format.
   * @throws IOException
   */
  public static boolean isFileFormat(final ImageInputStream input) throws IOException {
    return matches(input, FILEFORMAT_HEADER);
  }

  /**
   * Internal function which is able to compare the very beginning of an {@link ImageInputStream} with a given byte
   * array. After all, the function resets the position to the initial one.
   *
   * @param input      the input to be examined.
   * @param magicBytes the byte array which contains the values which should be compared to the input bytes
   *                   sequentially.
   * @return <code>false</code> if the input bytes differ from the given byte array;<br>
   * <code>true</code> if the input bytes are equal to the given byte array.
   * @throws IOException if something went wrong while reading the given {@link ImageInputStream}.
   */
  private static boolean matches(final ImageInputStream input, final int[] magicBytes) throws IOException {
    synchronized (input) {
      final long streamPosition = input.getStreamPosition();

      try {
        input.seek(0);
        for (int magicByte : magicBytes) {
          if (magicByte != input.read()) {
            return false;
          }
        }
      } finally {
        input.seek(streamPosition);
      }

      return true;
    }
  }


}
