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

import javax.imageio.stream.ImageOutputStream;

public class PacketHeaderOutput {

  private int bitsLeft;
  private ImageOutputStream dst;
  private int byteBuffer;

  public PacketHeaderOutput(ImageOutputStream dst) {
    this.dst = dst;
    this.byteBuffer = 0;
    this.bitsLeft = 8;
  }

  public void writeBit(int bit) throws IOException {
    assert (bit == (bit & 0x1));

    if (bitsLeft == 0) {
      dst.write(byteBuffer);
      bitsLeft = (byteBuffer == 0xFF) ? 7 : 8;
      byteBuffer = 0;
    }

    byteBuffer = (byteBuffer << 1) | bit;
    bitsLeft--;
  }

  public void writeBits(int bits, int numBits) throws IOException {
    while (numBits > 0) {
      writeBit((bits >> numBits) & 0x1);
      numBits--;
    }
  }

  public ImageOutputStream finish() throws IOException {
    final ImageOutputStream releasedDst = dst;

    if (bitsLeft < 8) {
      byteBuffer <<= bitsLeft;

      dst.write(byteBuffer);

      // Check if we need a stuffing bit?
      if (byteBuffer == 0xFF) {
        // Write a padding byte containing stuff bit and padding bits.
        dst.write(0);
      }
    }

    dst = null;
    return releasedDst;
  }
}
