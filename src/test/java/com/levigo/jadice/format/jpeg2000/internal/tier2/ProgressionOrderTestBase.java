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

import static org.hamcrest.CoreMatchers.is;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;
import com.levigo.jadice.format.jpeg2000.internal.image.Component;
import com.levigo.jadice.format.jpeg2000.internal.image.DefaultGridRegion;
import com.levigo.jadice.format.jpeg2000.internal.image.Pair;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;
import com.levigo.jadice.format.jpeg2000.internal.image.ResolutionMock;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponent;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponentMock;
import com.levigo.jadice.format.jpeg2000.internal.image.TileMock;

public abstract class ProgressionOrderTestBase {

  protected Tile createTile() {
    final int numComps = 3;
    final int numRes = 3;
    final int numLayers = 2;
    
    final Codestream codestream = new Codestream();
    codestream.numComps = numComps;
    
    final TileMock tile = new TileMock();
    tile.codestream = codestream;
    tile.tileIndices = new Pair(0, 0);
    tile.numLayers = numLayers;
    
    final TileComponent[] tileComps = new TileComponent[numComps];
    tile.setTileComps(tileComps);
    
    for (int c = 0; c < numComps; c++) {
      final TileComponentMock tileComponent = new TileComponentMock();
      tileComponent.codestream = codestream;
      tileComponent.tile = tile;
      tileComponent.comp = new Component();
      tileComponent.setNumResolutions(numRes);
      
      final Resolution[] resolutions = new Resolution[numRes];
      tileComponent.setResolutions(resolutions);
      
      for (int r = 0, nl = 2; r < numRes; r++, nl--) {
        final ResolutionMock resolution = new ResolutionMock();
        resolution.codestream = codestream;
        resolution.tileComp = tileComponent;
        resolution.dwtLevel = 1;
        resolution.resLevel = 1;
        resolution.numPrecinctsX = r + 1;
        resolution.numPrecinctsY = 1;
        resolution.numPrecinctsTotal = resolution.numPrecinctsX * resolution.numPrecinctsY;
        resolution.setGridRegion(new DefaultGridRegion(new Region(64, 64)));
        resolutions[r] = resolution;
      }
      tileComps[c] = tileComponent;
    }

    tile.packetSequencer = new DefaultPacketSequencer();

    return tile;
  }

  protected abstract PacketJigsaw createPacketJigsaw();

  protected abstract PacketHeader[] createExpecteds();

  @Test
  public void simplePacketCreation() throws JPEG2000Exception {
    final PacketHeader[] expecteds = createExpecteds();
    final Tile tile = createTile();
    final PacketJigsaw order = createPacketJigsaw();
    final PacketSequencer packetSequencer = tile.packetSequencer;
    order.solve(tile, packetSequencer);

    for (int i = 0; i < expecteds.length; i++) {
      final PacketHeader expected = expecteds[i];
      Assertions.assertTrue(packetSequencer.hasNext(), "i=" + i + ": hasNext() returned false instead of true");

      final PacketHeader actual = packetSequencer.next();
      Assertions.assertNotNull(actual);
      MatcherAssert.assertThat(actual, is(expected));
    }
    
    Assertions.assertFalse(packetSequencer.hasNext(), "Final hasNext() returned true instead of false");
  }
}
