package com.levigo.jadice.format.jpeg2000.internal.tier2;

import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;

public class DefaultPacketSequencer implements PacketSequencer {

  private PacketHeader head;
  private PacketHeader next;
  private PacketHeader tail;

  @Override
  public void append(PacketHeader packetHeader) {
    if (tail == null) {
      head = next = packetHeader;
    } else {
      tail.next = packetHeader;
    }

    tail = packetHeader;
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public PacketHeader next() {
    final PacketHeader ret = next;

    if (ret != null) {
      next = ret.next;
    }

    return ret;
  }

  @Override
  public void reset() {
    next = head;
  }

  PacketHeader getTail() {
    return tail;
  }

}
