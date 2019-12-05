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
