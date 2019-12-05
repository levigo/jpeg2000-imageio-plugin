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
package com.levigo.jadice.format.jpeg2000.internal.io;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.format.jpeg2000.internal.CorruptBitstuffingException;

/**
 * Manages the reading of packet header bytes from the codestream along with bit stuffing and unpacking.
 */
@Refer(to = Spec.J2K_CORE, page = 54, section = "B.10.1", called = "Bit-stuffing Routine")
public class PacketHeaderInput {

  private long startPosition;

  private ImageInputStream source;

  private byte byteBuffer;

  private int bitsLeft;

  public PacketHeaderInput(ImageInputStream source, long headerStartPosition) {
    this.source = source;
    this.startPosition = headerStartPosition;
  }

  private void fillBuffer() throws IOException, CorruptBitstuffingException {
    if (((byteBuffer & 0xFF) == 0xFF)) {
      bitsLeft = 7;
      byteBuffer = source.readByte();

      if (((byteBuffer & 0xFF) == 0xFF)) {
        throw new CorruptBitstuffingException(source, startPosition);
      }

    } else {
      bitsLeft = 8;
      byteBuffer = source.readByte();
    }
  }

  /**
   * Reads exactly one bit from the source.
   *
   * @return an integer representing one bit with the values <code>0</code> or <code>1</code>.
   *
   * @throws java.io.EOFException                if the source stream is at its end
   * @throws java.io.IOException                 if an I/O error occurs
   * @throws CorruptBitstuffingException if a stuff-bit is missing after a <code>0xFF</code> byte.
   */
  public int readBit() throws IOException, CorruptBitstuffingException {
    if (bitsLeft == 0) {
      fillBuffer();
    }

    bitsLeft--;

    return (byteBuffer >> bitsLeft) & 1;
  }

  /**
   * Reads exactly the given <code>numBits</code> bits. The maximum number of bits are 64 as the returned value hast to
   * fit into a long which is able to hold 64 bits.
   *
   * @param numBits amount of bits to read
   *
   * @return the value of the read bits
   *
   * @throws java.io.EOFException                if the source stream is at its end
   * @throws java.io.IOException                 if an I/O error occurs
   * @throws CorruptBitstuffingException if a stuff-bit is missing after a <code>0xFF</code> byte.
   */
  public long readBits(int numBits) throws IOException, CorruptBitstuffingException {
    long readValue = 0;

    while (numBits > 0) {
      if (bitsLeft == 0) {
        fillBuffer();
      }

      final int transferredBits = (numBits < bitsLeft) ? numBits : bitsLeft;
      bitsLeft -= transferredBits;
      numBits -= transferredBits;
      readValue <<= transferredBits;
      readValue |= ((byteBuffer >> bitsLeft) & (0xFF >> (8 - transferredBits)));
    }

    return readValue;
  }

  /**
   * This method should be called if reading header is expected to be finished. Remaining stuffing bytes or unused will
   * be consumed and the source is released.
   *
   * @return the released source
   *
   * @throws java.io.IOException if an I/O error occurs
   */
  public ImageInputStream finish() throws IOException {
    final ImageInputStream releasedSource = source;

    if (bitsLeft == 0 && (byteBuffer & 0xFF) == 0xFF) {
      bitsLeft = 7;
      source.readByte();
    }

    source = null;
    return releasedSource;
  }

  public byte getByteBuffer() {
    return byteBuffer;
  }

  public int getBitsLeft() {
    return bitsLeft;
  }
}
