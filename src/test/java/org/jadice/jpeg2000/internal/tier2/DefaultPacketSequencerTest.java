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

import org.jadice.jpeg2000.internal.codestream.PacketHeader;
import org.jadice.jpeg2000.internal.tier2.DefaultPacketSequencer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
