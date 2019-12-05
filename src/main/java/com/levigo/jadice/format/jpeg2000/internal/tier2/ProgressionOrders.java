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
package com.levigo.jadice.format.jpeg2000.internal.tier2;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.marker.COD;
import com.levigo.jadice.format.jpeg2000.internal.marker.COx;
import com.levigo.jadice.format.jpeg2000.internal.marker.POC;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;

public class ProgressionOrders {

  /** Applies <b>layer -> resolution level -> component -> position</b> progression (LRCP). */
  public static final int VALUE_PROGRESSION_LRCP = 0;

  /** Applies <b>resolution level -> layer -> component -> position</b> progression (RLCP). */
  public static final int VALUE_PROGRESSION_RLCP = 1;

  /** Applies <b>resolution level -> position -> component -> layer </b> progression (RPCL). */
  public static final int VALUE_PROGRESSION_RPCL = 2;

  /** Applies <b>position -> component -> resolution level -> layer </b> progression (PCRL). */
  public static final int VALUE_PROGRESSION_PCRL = 3;

  /** Applies <b>component -> position -> resolution level -> layer </b> progression (CPRL). */
  public static final int VALUE_PROGRESSION_CPRL = 4;

  /**
   * Creates a new instance of {@link PacketJigsaw} according to the given identifier. If the requested identifier
   * is unknown and therefore currently not supported, the function will return <code>null</code>.
   * <p>
   * Following integer-based identifiers are supported as defined in <i>ITU-T.800, Table A.16</i>:
   * <ul>
   * <li><b>Value <code>0</code>:</b> Layer -> Resolution level -> Component -> Precinct</li>
   * <li><b>Value <code>1</code>:</b> Resolution level -> Layer -> Component -> Precinct</li>
   * <li><b>Value <code>2</code>:</b> Resolution level -> Precinct -> Component -> Layer</li>
   * <li><b>Value <code>3</code>:</b> Precinct -> Component -> Resolution level -> Layer</li>
   * <li><b>Value <code>4</code>:</b> Component -> Precinct -> Resolution level -> Layer</li>
   * </ul>
   *
   * @param progressionOrder an integer identifying the progression order.
   *
   * @return a new instance of {@link PacketJigsaw} or <code>null</code>.
   */
  public static PacketJigsaw create(final int progressionOrder) throws JPEG2000Exception {
    final ProgressionOrder[] values = ProgressionOrder.values();

    if (progressionOrder < values.length && progressionOrder >= 0) {
      final ProgressionOrder order = values[progressionOrder];
      if (order != null) {
        return new PacketJigsaw(order);
      }
    }

    throw new JPEG2000Exception(CodestreamMessages.ILLEGAL_PROGRESSION_ORDER, progressionOrder);
  }

  private static PacketJigsaw create(int p, int lMax, int cMin, int cMax, int rMin, int rMax) throws JPEG2000Exception {
    final PacketJigsaw order = create(p);
    order.setNumLayers(lMax);
    order.setCompIdxBounds(cMin, cMax);
    order.setResIdxBounds(rMin, rMax);
    return order;
  }

  public static PacketJigsaw create(final Codestream codestream, final Tile tile) throws JPEG2000Exception {
    final COD cod = COx.accessCOD(codestream, tile);
    return create(cod.SGcod_order, cod.SGcod_layers, 0, codestream.numComps - 1, 0, cod.SP_NL);
  }

  public static PacketJigsaw[] create(final POC poc) throws JPEG2000Exception {
    final int numEntries = poc.Ppoc.length;
    final PacketJigsaw[] orders = new PacketJigsaw[numEntries];
    for (int i = 0; i < numEntries; i++) {
      orders[i] = create(poc.Ppoc[i], poc.LYEpoc[i], poc.CSpoc[i], poc.CEpoc[i], poc.RSpoc[i], poc.REpoc[i]);
    }
    return orders;
  }
}
