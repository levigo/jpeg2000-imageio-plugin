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
package com.levigo.jadice.format.jpeg2000.internal.color;

import static org.jadice.util.base.Numbers.clamp;

import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponent;

public class InverseIrreversibleMCTTask implements DecodeTask<Tile> {

  private static final float CR_R_WEIGHT = 1.402f;
  private static final float CR_G_WEIGHT = 0.71414f;
  private static final float CB_G_WEIGHT = 0.34413f;
  private static final float CB_B_WEIGHT = 1.771f;

  private final Tile tile;

  public InverseIrreversibleMCTTask(Tile tile) {
    this.tile = tile;
  }

  @Override
  public Tile call() throws Exception {
    if (tile.codestream.numComps < 3) {
      throw new IllegalArgumentException(
          "The inverse irreversible multiple component transformation expects at least three components");
    }

    // Component 0: Y becomes R
    final TileComponent c0 = tile.accessTileComp(0);
    final float c0Samples[] = c0.state.sampleBuffer.floatSamples;
    final float levelShiftR = c0.comp.isSigned ? 0 : (1 << c0.comp.precision - 1) - 1;
    final float minR = 0 - (c0.comp.isSigned ? 1 << c0.comp.precision - 1 : 0);
    final float maxR = (1 << c0.comp.precision - (c0.comp.isSigned ? 1 : 0)) - 1;

    // Component 1: Cb becomes G
    final TileComponent c1 = tile.accessTileComp(1);
    final float c1samples[] = c1.state.sampleBuffer.floatSamples;
    final float levelShiftG = c1.comp.isSigned ? 0 : (1 << c1.comp.precision - 1) - 1;
    final float minG = 0 - (c1.comp.isSigned ? 1 << c1.comp.precision - 1 : 0);
    final float maxG = (1 << c1.comp.precision - (c1.comp.isSigned ? 1 : 0)) - 1;

    // Component 2: Cr becomes B
    final TileComponent c2 = tile.accessTileComp(2);
    final float c2samples[] = c2.state.sampleBuffer.floatSamples;
    final float levelShiftB = c2.comp.isSigned ? 0 : (1 << c2.comp.precision - 1) - 1;
    final float minB = 0 - (c2.comp.isSigned ? 1 << c2.comp.precision - 1 : 0);
    final float maxB = (1 << c2.comp.precision - (c2.comp.isSigned ? 1 : 0)) - 1;

    // FIXME: naive implementation without any support for subsampling
    for (int i = 0; i < c0Samples.length; i++) {
      final float y = c0Samples[i];
      final float cb = c1samples[i];
      final float cr = c2samples[i];

      // R
      c0Samples[i] = clamp(y + CR_R_WEIGHT * cr + levelShiftR, minR, maxR);
      // G
      c1samples[i] = clamp(y - CB_G_WEIGHT * cb - CR_G_WEIGHT * cr + levelShiftG, minG, maxG);
      // B
      c2samples[i] = clamp(y + CB_B_WEIGHT * cb + levelShiftB, minB, maxB);
    }

    // for the remaining components we need to perform a simple level shifting
    for (int c = 3; c < tile.codestream.numComps; c++) {
      InverseLevelShiftTask.performLevelShift(tile.accessTileComp(c));
    }

    return tile;
  }
}
