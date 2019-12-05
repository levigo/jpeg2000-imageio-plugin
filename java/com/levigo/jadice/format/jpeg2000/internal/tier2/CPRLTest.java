package com.levigo.jadice.format.jpeg2000.internal.tier2;

import static com.levigo.jadice.format.jpeg2000.internal.tier2.ProgressionOrder.CPRL;

import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;

public class CPRLTest extends ProgressionOrderTestBase {

  @Override
  protected PacketJigsaw createPacketJigsaw() {
    final PacketJigsaw order = new PacketJigsaw(CPRL);
    order.setCompIdxBounds(0, 2);
    order.setResIdxBounds(0, 2);
    order.setNumLayers(2);
    return order;
  }

  @Override
  protected PacketHeader[] createExpecteds() {
    return new PacketHeader[]{
        new PacketHeader(0, 0, 0, 0), new PacketHeader(0, 0, 1, 0),
        new PacketHeader(0, 1, 0, 0), new PacketHeader(0, 1, 1, 0),
        new PacketHeader(0, 2, 0, 0), new PacketHeader(0, 2, 1, 0),
        new PacketHeader(0, 1, 0, 1), new PacketHeader(0, 1, 1, 1),
        new PacketHeader(0, 2, 0, 1), new PacketHeader(0, 2, 1, 1),
        new PacketHeader(0, 2, 0, 2), new PacketHeader(0, 2, 1, 2),
        
        new PacketHeader(1, 0, 0, 0), new PacketHeader(1, 0, 1, 0),
        new PacketHeader(1, 1, 0, 0), new PacketHeader(1, 1, 1, 0),
        new PacketHeader(1, 2, 0, 0), new PacketHeader(1, 2, 1, 0),
        new PacketHeader(1, 1, 0, 1), new PacketHeader(1, 1, 1, 1),
        new PacketHeader(1, 2, 0, 1), new PacketHeader(1, 2, 1, 1),
        new PacketHeader(1, 2, 0, 2), new PacketHeader(1, 2, 1, 2),
        
        new PacketHeader(2, 0, 0, 0), new PacketHeader(2, 0, 1, 0),
        new PacketHeader(2, 1, 0, 0), new PacketHeader(2, 1, 1, 0),
        new PacketHeader(2, 2, 0, 0), new PacketHeader(2, 2, 1, 0),
        new PacketHeader(2, 1, 0, 1), new PacketHeader(2, 1, 1, 1),
        new PacketHeader(2, 2, 0, 1), new PacketHeader(2, 2, 1, 1),
        new PacketHeader(2, 2, 0, 2), new PacketHeader(2, 2, 1, 2),
    };
  }
}
