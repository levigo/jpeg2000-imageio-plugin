package com.levigo.jadice.format.jpeg2000.internal.tier2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Comparator;

import org.junit.Test;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;

public class PacketJigsawCreationTest {
  
  @Test
  public void progressionIdOnly() throws JPEG2000Exception {
    final int[] progressionOrderIds = new int[]{
        ProgressionOrders.VALUE_PROGRESSION_LRCP,
        ProgressionOrders.VALUE_PROGRESSION_RLCP,
        ProgressionOrders.VALUE_PROGRESSION_RPCL,
        ProgressionOrders.VALUE_PROGRESSION_PCRL,
        ProgressionOrders.VALUE_PROGRESSION_CPRL
    };

    final ProgressionOrder[] progressionOrders = new ProgressionOrder[]{
        ProgressionOrder.LRCP,
        ProgressionOrder.RLCP,
        ProgressionOrder.RPCL,
        ProgressionOrder.PCRL,
        ProgressionOrder.CPRL
    };
    
    for(int i = 0; i < progressionOrderIds.length;i++) {
      final int expectedId = progressionOrderIds[i];
      final ProgressionOrder expectedOrder = progressionOrders[i];
      
      final PacketJigsaw packetJigsaw = ProgressionOrders.create(expectedId);
      final Comparator<PacketHeader> actualOrder = packetJigsaw.comparator;
      assertEquals(expectedOrder, actualOrder);
    }
    
  }
  
  @Test(expected = JPEG2000Exception.class)
  public void progressionIdOutOfRangeMin() throws JPEG2000Exception {
    try {
      ProgressionOrders.create(-1);
    } catch (JPEG2000Exception e) {
      assertEquals(CodestreamMessages.ILLEGAL_PROGRESSION_ORDER, e.getMessageID());
      throw e;
    }

    fail("Expected exception not thrown");
  }
  
  @Test(expected = JPEG2000Exception.class)
  public void progressionIdOutOfRangeMax() throws JPEG2000Exception {
    try {
      ProgressionOrders.create(5);
    } catch (JPEG2000Exception e) {
      assertEquals(CodestreamMessages.ILLEGAL_PROGRESSION_ORDER, e.getMessageID());
      throw e;
    }

    fail("Expected exception not thrown");
  }
  
}
