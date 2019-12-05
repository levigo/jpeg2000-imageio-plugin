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
package com.levigo.jadice.format.jpeg2000.internal.codestream;

import java.io.IOException;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.image.Canvas;
import com.levigo.jadice.format.jpeg2000.internal.image.Pair;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.marker.SOT;


public class TilePart {

  public boolean isFirst;

  public int idx;

  public Tile tile;

  public long length;

  public int lastPacketIndex;

  public MarkerSegmentContainer markerSegments;

  public TilePart() {
    isFirst = false;
    markerSegments = new MarkerSegmentContainer();
  }

  public void initialize(SOT sot, Codestream codestream, long tilePartStart) throws JPEG2000Exception, IOException {
    final Pair numTiles = codestream.numTiles;
    
    if (codestream.tiles == null) {
      codestream.tiles = new Tile[numTiles.y* numTiles.x];
    }

    final Pair tileIndices = Canvas.tileIndices(sot.Isot, numTiles.x);
    final int tileIndex = tileIndices.y * numTiles.x + tileIndices.x;
    Tile tile = codestream.tiles[(tileIndex)];
    if (tile == null) {
      codestream.tiles[tileIndex] = tile = new Tile(tileIndices, codestream);
    }
/*
    if (tile.parts == null) {
      tile.parts = new LinkedList<TilePart>();
    }
    if (tile.parts.isEmpty()) {
      isFirst = true;
    }
    tile.parts.add(this);

    idx = sot.TPsot;

    if (idx < tile.lastPartIndex) {
      throw new CodestreamException(CodestreamMessages.INCONSISTENT_TILE_PART_ORDER);
    }

    tile.lastPartIndex = idx;

    codestream.tilePartPointerProvider.registerSOT(sot, tilePartStart);

    final int numParts = sot.TNsot;

    if (numParts > 0) {
      tile.numTileParts = numParts;
    }

    length = sot.Psot;
*/
    this.tile = tile;
  }
}
