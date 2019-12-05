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

import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

import java.util.Comparator;
import java.util.TreeSet;

import org.jadice.util.log.qualified.QualifiedLogger;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponent;

/**
 * FIXME 
 * 
 * Mitschrieb aus Kurz-Übergabe:
 * encapsulates relevant information for packet sequence, and is able to solve ???
 */
public class PacketJigsaw {
  
  private static final QualifiedLogger LOGGER = getQualifiedLogger(PacketJigsaw.class);

  final Comparator<PacketHeader> comparator;

  private int minCompIdx;
  private int maxCompIdx;
  
  private int minResIdx;
  private int maxResIdx;
  
  private int numLayers;
  
  public void setCompIdxBounds(int min, int max) {
    this.minCompIdx = min;
    this.maxCompIdx = max;
  }

  public void setResIdxBounds(int min, int max) {
    this.minResIdx = min;
    this.maxResIdx = max;
  }

  public void setNumLayers(int max) {
    this.numLayers = max;
  }
  
  public PacketJigsaw(Comparator<PacketHeader> comparator) {
    this.comparator = comparator;
  }
  
  public void solve(Tile tile, PacketSequencer sequencer) throws JPEG2000Exception {
    final TreeSet<PacketHeader> set = new TreeSet<>(comparator);

    final int minCompIdx = Math.max(this.minCompIdx, 0);
    final int maxCompIdx = Math.min(this.maxCompIdx, tile.codestream.numComps - 1);

    for (int c = minCompIdx; c <= maxCompIdx; c++) {
      final TileComponent tileComp = tile.accessTileComp(c);

      final int minResIdx = Math.max(this.minResIdx, 0);
      final int maxResIdx = Math.min(this.maxResIdx, tileComp.numResolutions() - 1);

      for (int r = minResIdx; r <= maxResIdx; r++) {
        final Resolution resolution = tileComp.accessResolution(r);

        for (int p = 0; p < resolution.numPrecinctsTotal; p++) {
          for (int l = 0; l < numLayers; l++) {
            set.add(new PacketHeader(c, r, l, p));
          }
        }
      }
    }

    for (PacketHeader packet : set) {
      if (Debug.LOG_PROGRESSION_ORDER) {
        LOGGER.info(packet.toString());
      }
      sequencer.append(packet);
    }
  }

  @Override
  public PacketJigsaw clone() {
    final PacketJigsaw clone = new PacketJigsaw(comparator);
    clone.setCompIdxBounds(minCompIdx, maxCompIdx);
    clone.setResIdxBounds(minResIdx, maxResIdx);
    clone.setNumLayers(numLayers);
    return clone;
  }
}
