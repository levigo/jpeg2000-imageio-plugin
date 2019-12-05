package com.levigo.jadice.format.jpeg2000.internal.io;

import com.levigo.jadice.document.io.MemoryInputStream;
import com.levigo.jadice.format.jpeg2000.internal.CorruptBitstuffingException;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.stream.ImageInputStream;
import java.io.EOFException;
import java.io.IOException;

public class PacketHeaderInputTest {

  @Test
  public void testBitwiseRead() throws IOException, CorruptBitstuffingException {
    final int[] expecteds = new int[]{
        0, 0, 0, 0, 0, 0, 0, 0, // Byte 0 (0x0)
        0, 0, 0, 0, 0, 0, 0, 1, // Byte 1 (0x1)
        1, 1, 1, 1, 1, 1, 1, 1, // Byte 2 (0xFF)
      /*0*/1, 1, 1, 1, 1, 1, 1, // Byte 3 (0x7F)
        1, 0, 0, 1, 0, 0, 0, 0, // Byte 4 (0x90)
        0, 1, 0, 1, 0, 0, 1, 1  // Byte 5 (0x53)
    };

    final byte[] sourceBytes = new byte[]{
        0x0, 0x1, //
        (byte) 0xFF, // FF-byte leads to a stuff bit at the beginning of next byte
        (byte) 0x7F, // stuff bit (0) at beginning, will be skipped while reading
        (byte) 0x90, 0x53
    };

    final MemoryInputStream source = new MemoryInputStream(sourceBytes);
    final PacketHeaderInput packetHeaderInput = new PacketHeaderInput(source, 0);

    int asserts = 0;
    try {
      for (int i = 0; i < expecteds.length; i++, asserts++) {
        Assert.assertEquals(expecteds[i], packetHeaderInput.readBit());
      }

    } finally {
      Assert.assertEquals(47, asserts);
      Assert.assertEquals(6, source.getStreamPosition());

      source.close();
    }
  }

  @Test(expected = EOFException.class)
  public void testBitwiseReadEOF() throws IOException, CorruptBitstuffingException {
    final byte[] sourceBytes = new byte[]{
        0x0, (byte) 0xFF, 0x7F, (byte) 0x90
    };

    final MemoryInputStream source = new MemoryInputStream(sourceBytes);
    final PacketHeaderInput packetHeaderInput = new PacketHeaderInput(source, 0);

    int maxBits = (sourceBytes.length * 8) + 1;
    while (maxBits > 0) {
      packetHeaderInput.readBit();
      maxBits--;
    }
  }

  @Test(expected = EOFException.class)
  public void testBlockwiseReadEOF() throws IOException, CorruptBitstuffingException {
    final byte[] sourceBytes = new byte[]{
        0x0, (byte) 0xFF, 0x7F, (byte) 0x90
    };

    final MemoryInputStream source = new MemoryInputStream(sourceBytes);
    final PacketHeaderInput packetHeaderInput = new PacketHeaderInput(source, 0);

    int maxBits = (sourceBytes.length * 4) + 1;
    while (maxBits > 0) {
      packetHeaderInput.readBits(2);
      maxBits--;
    }
  }

  @Test(expected = EOFException.class)
  public void testFinish() throws IOException, CorruptBitstuffingException {
    final int[] expecteds = new int[]{
        0, 0, 0, 0, 0, 0, 0, 0, // Byte 0 (0x0)
        0, 0, 0, 0, 0, 0, 0, 1, // Byte 1 (0x1)
        1, 1, 1, 1, 1, 1, 1, 1, // Byte 2 (0xFF)
      /*0*/1, 1, 1, 1, 1, 1, 1, // Byte 3 (0x7F)
        1, 0, 0, 1, 0, 0, 0, 0, // Byte 4 (0x90)
        0, 1, 0, 1/*,0,0,1,1 */ // Byte 5 (0x53)
    };

    final byte[] sourceBytes = new byte[]{
        0x0, 0x1, (byte) 0xFF, 0x7F, (byte) 0x90, 0x53, 0x35
    };

    final MemoryInputStream source = new MemoryInputStream(sourceBytes);
    final PacketHeaderInput packetHeaderInput = new PacketHeaderInput(source, 0);

    // Read until the half of the last byte was read
    int i = 0;
    for (; i < expecteds.length; i++) {
      Assert.assertEquals(expecteds[i], packetHeaderInput.readBit());
    }

    Assert.assertEquals(expecteds.length, i);
    Assert.assertEquals(4, packetHeaderInput.getBitsLeft());
    Assert.assertEquals(0x53, packetHeaderInput.getByteBuffer());

    final ImageInputStream releasedSource = packetHeaderInput.finish();
    Assert.assertSame(source, releasedSource);

    // Test stream position alignment
    Assert.assertEquals(6, releasedSource.getStreamPosition());
    Assert.assertEquals(0x35, releasedSource.readByte());
    Assert.assertEquals(7, releasedSource.length());
    Assert.assertEquals(releasedSource.length(), releasedSource.getStreamPosition());

    // throws the expected EOFException
    releasedSource.readByte();
  }

  @Test(expected = EOFException.class)
  public void testFinishWithStuffByte() throws IOException, CorruptBitstuffingException {
    final int[] expecteds = new int[]{
        0, 0, 0, 0, 0, 0, 0, 0, // Byte 0 (0x0)
        0, 0, 0, 0, 0, 0, 0, 1, // Byte 1 (0x1)
        1, 1, 1, 1, 1, 1, 1, 1, // Byte 2 (0xFF)
    };

    final byte[] sourceBytes = new byte[]{
        0x0, 0x1, (byte) 0xFF, 0x7F,
        /* rest is arbitrary padding */
        (byte) 0x90, 0x53, 0x35
    };

    final MemoryInputStream source = new MemoryInputStream(sourceBytes);
    final PacketHeaderInput packetHeaderInput = new PacketHeaderInput(source, 0);

    // Read until the 0xFF byte was read
    int i = 0;
    for (; i < expecteds.length; i++) {
      Assert.assertEquals(expecteds[i], packetHeaderInput.readBit());
    }

    Assert.assertEquals(expecteds.length, i);
    Assert.assertEquals(0, packetHeaderInput.getBitsLeft());
    Assert.assertEquals((byte) 0xFF, packetHeaderInput.getByteBuffer());

    final ImageInputStream releasedSource = packetHeaderInput.finish();
    Assert.assertSame(source, releasedSource);

    // Test stream position alignment
    Assert.assertEquals(4, releasedSource.getStreamPosition());
    Assert.assertEquals((byte) 0x90, releasedSource.readByte());

    Assert.assertEquals(5, releasedSource.getStreamPosition());
    Assert.assertEquals(0x53, releasedSource.readByte());

    Assert.assertEquals(6, releasedSource.getStreamPosition());
    Assert.assertEquals(0x35, releasedSource.readByte());

    Assert.assertEquals(7, releasedSource.getStreamPosition());
    Assert.assertEquals(7, releasedSource.length());
    Assert.assertEquals(releasedSource.length(), releasedSource.getStreamPosition());

    // throws the expected EOFException
    releasedSource.readByte();
  }

  @Test(expected = CorruptBitstuffingException.class)
  public void testCorruptBitstuffingDeterminationBitwise() throws IOException, CorruptBitstuffingException {
    final int curruptByteIdx = 1;
    final byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xFF};

    final MemoryInputStream source = new MemoryInputStream(bytes);
    final PacketHeaderInput packetHeaderInput = new PacketHeaderInput(source, 0);

    int i = 0;
    try {
      while (true) {
        Assert.assertEquals(1, packetHeaderInput.readBit());
        i++;
        if (i > (bytes.length * 8)) {
          Assert.fail("readBit() didn't throw " + CorruptBitstuffingException.class.getSimpleName() + " at expected position");
        }
      }
    } finally {
      Assert.assertEquals(curruptByteIdx + 1, source.getStreamPosition());
      Assert.assertEquals(curruptByteIdx * 8, i);
    }
  }

  @Test(expected = CorruptBitstuffingException.class)
  public void testCorruptBitstuffingDeterminationBytewise() throws IOException, CorruptBitstuffingException {
    final int curruptByteIdx = 1;
    final byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    final MemoryInputStream source = new MemoryInputStream(bytes);
    final PacketHeaderInput packetHeaderInput = new PacketHeaderInput(source, 0);

    long actual = Long.MAX_VALUE;
    try {
      actual = packetHeaderInput.readBits(12);
    } finally {
      Assert.assertEquals(Long.MAX_VALUE, actual); // 'actual' must be left unchanged
      Assert.assertEquals(curruptByteIdx + 1, source.getStreamPosition());
    }
  }


}
