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

import static org.jadice.jpeg2000.internal.tier2.ProgressionOrder.LRCP;

import org.jadice.jpeg2000.internal.codestream.PacketHeader;
import org.jadice.jpeg2000.internal.tier2.PacketJigsaw;

public class LRCPTest extends ProgressionOrderTestBase {

  @Override
  protected PacketJigsaw createPacketJigsaw() {
    final PacketJigsaw order = new PacketJigsaw(LRCP);
    order.setCompIdxBounds(0, 2);
    order.setResIdxBounds(0, 2);
    order.setNumLayers(2);
    return order;
  }

  @Override
  protected PacketHeader[] createExpecteds() {
    return new PacketHeader[]{
        new PacketHeader(0, 0, 0, 0),
        new PacketHeader(1, 0, 0, 0),
        new PacketHeader(2, 0, 0, 0),
        
        new PacketHeader(0, 1, 0, 0), new PacketHeader(0, 1, 0, 1), 
        new PacketHeader(1, 1, 0, 0), new PacketHeader(1, 1, 0, 1), 
        new PacketHeader(2, 1, 0, 0), new PacketHeader(2, 1, 0, 1),

        new PacketHeader(0, 2, 0, 0), new PacketHeader(0, 2, 0, 1), new PacketHeader(0, 2, 0, 2),
        new PacketHeader(1, 2, 0, 0), new PacketHeader(1, 2, 0, 1), new PacketHeader(1, 2, 0, 2),
        new PacketHeader(2, 2, 0, 0), new PacketHeader(2, 2, 0, 1), new PacketHeader(2, 2, 0, 2),

        new PacketHeader(0, 0, 1, 0),
        new PacketHeader(1, 0, 1, 0),
        new PacketHeader(2, 0, 1, 0),

        new PacketHeader(0, 1, 1, 0), new PacketHeader(0, 1, 1, 1),
        new PacketHeader(1, 1, 1, 0), new PacketHeader(1, 1, 1, 1),
        new PacketHeader(2, 1, 1, 0), new PacketHeader(2, 1, 1, 1),

        new PacketHeader(0, 2, 1, 0), new PacketHeader(0, 2, 1, 1), new PacketHeader(0, 2, 1, 2),
        new PacketHeader(1, 2, 1, 0), new PacketHeader(1, 2, 1, 1), new PacketHeader(1, 2, 1, 2),
        new PacketHeader(2, 2, 1, 0), new PacketHeader(2, 2, 1, 1), new PacketHeader(2, 2, 1, 2),
    };
  }
}
