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
package org.jadice.jpeg2000.internal.color;

import static org.jadice.util.base.Numbers.clamp;

import org.jadice.jpeg2000.internal.decode.push.DecodeTask;
import org.jadice.jpeg2000.internal.image.Tile;
import org.jadice.jpeg2000.internal.image.TileComponent;

public class InverseReversibleMCTTask implements DecodeTask<Tile> {

  private final Tile tile;

  public InverseReversibleMCTTask(Tile tile) {
    this.tile = tile;
  }

  @Override
  public Tile call() throws Exception {
    final TileComponent tileComp0 = tile.accessTileComp(0);
    final TileComponent tileComp1 = tile.accessTileComp(1);
    final TileComponent tileComp2 = tile.accessTileComp(2);

    final boolean rSigned = tileComp0.comp.isSigned;
    final boolean gSigned = tileComp1.comp.isSigned;
    final boolean bSigned = tileComp2.comp.isSigned;

    final int rLift = rSigned ? 0 : (1 << (tileComp0.comp.precision - 1));
    final int gLift = gSigned ? 0 : (1 << (tileComp1.comp.precision - 1));
    final int bLift = bSigned ? 0 : (1 << (tileComp2.comp.precision - 1));

    final int[] y2r = tileComp0.state.sampleBuffer.intSamples;
    final int[] cb2g = tileComp1.state.sampleBuffer.intSamples;
    final int[] cr2b = tileComp2.state.sampleBuffer.intSamples;

    // TODO Implement support for subsampled components
    // TODO Implement level shifting

    for (int i = 0; i < y2r.length; i++) {
      final int cb = cb2g[i];
      final int cr = cr2b[i];
      final int g = y2r[i] - ((cb + cr) >> 2);
      y2r[i] = clamp(g + cr + rLift, 0, 255);
      cr2b[i] = clamp(g + cb + bLift, 0, 255);
      cb2g[i] = clamp(g + gLift, 0, 255);
    }

    // for the remaining components we need to perform a simple level shifting
    for (int c = 3; c < tile.codestream.numComps; c++) {
      InverseLevelShiftTask.performLevelShift(tile.accessTileComp(c));
    }

    return tile;
  }

}
