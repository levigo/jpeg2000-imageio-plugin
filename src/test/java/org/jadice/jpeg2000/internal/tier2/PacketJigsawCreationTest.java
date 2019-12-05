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
package org.jadice.jpeg2000.internal.tier2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Comparator;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.codestream.PacketHeader;
import org.jadice.jpeg2000.internal.tier2.PacketJigsaw;
import org.jadice.jpeg2000.internal.tier2.ProgressionOrder;
import org.jadice.jpeg2000.internal.tier2.ProgressionOrders;
import org.jadice.jpeg2000.msg.CodestreamMessages;
import org.junit.Test;

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
