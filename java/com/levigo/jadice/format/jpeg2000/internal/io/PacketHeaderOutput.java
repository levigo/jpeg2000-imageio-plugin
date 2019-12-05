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
