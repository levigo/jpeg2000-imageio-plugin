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

import org.jadice.jpeg2000.internal.buffer.DummyDataBuffer;
import org.jadice.jpeg2000.internal.decode.push.DecodeTask;
import org.jadice.jpeg2000.internal.image.Tile;
import org.jadice.jpeg2000.internal.image.TileComponent;
import org.jadice.util.base.Numbers;

public class InverseLevelShiftTask implements DecodeTask<Tile> {

  private final Tile tile;

  public InverseLevelShiftTask(Tile tile) {
    this.tile = tile;
  }

  @Override
  public Tile call() throws Exception {
    final int numComps = tile.codestream.numComps;
    for (int c = 0; c < numComps; c++) {
      final TileComponent tileComp = tile.accessTileComp(c);
      performLevelShift(tileComp);
    }

    return tile;
  }

  // FIXME: this method is default visible as the MCT tasks must be able to perform level shifting on
  // extra components. Refactor the tasks so that several tasks can be combined and the controlling logic 
  // can be placed in ColorPreparation.
  static void performLevelShift(final TileComponent tileComp) {
    if (!tileComp.comp.isSigned) {
      final DummyDataBuffer sampleBuffer = tileComp.state.sampleBuffer;
      final int bitDepth = tileComp.comp.precision - 1;
      final int shift = 1 << bitDepth;
      if (sampleBuffer.intSamples != null) {
        final int[] samples = sampleBuffer.intSamples;
        for (int i = 0; i < samples.length; i++) {
          samples[i] = Numbers.clamp(samples[i] + shift, 0, 255);
        }
      } else if (sampleBuffer.floatSamples != null) {
        final float[] samples = sampleBuffer.floatSamples;
        for (int i = 0; i < samples.length; i++) {
          samples[i] = Numbers.clamp(samples[i] + shift, 0, 255);
        }
      }
    }
  }
}
