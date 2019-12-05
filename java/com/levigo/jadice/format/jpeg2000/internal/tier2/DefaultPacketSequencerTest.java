package com.levigo.jadice.format.jpeg2000.internal.tier2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;

public class DefaultPacketSequencerTest {

  @Test
  public void testSimpleUsage() {
    final DefaultPacketSequencer underTest = new DefaultPacketSequencer();
    Assertions.assertFalse(underTest.hasNext());

    final PacketHeader[] packets = new PacketHeader[]{
        new PacketHeader(0, 0, 0, 1),
        new PacketHeader(0, 0, 0, 2),
        new PacketHeader(0, 0, 0, 3),
        new PacketHeader(0, 0, 0, 4)
    };

    for (PacketHeader packet : packets) {
      underTest.append(packet);
      Assertions.assertTrue(underTest.hasNext());
      Assertions.assertEquals(packet, underTest.getTail());
    }

    for (PacketHeader packet : packets) {
      PacketHeader actual = underTest.next();
      Assertions.assertNotNull(actual);
      Assertions.assertEquals(packet, actual);
    }

    Assertions.assertFalse(underTest.hasNext());
    Assertions.assertNull(underTest.next());
    Assertions.assertEquals(packets[packets.length - 1], underTest.getTail());

  }
}
