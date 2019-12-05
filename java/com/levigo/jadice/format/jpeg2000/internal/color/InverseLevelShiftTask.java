package com.levigo.jadice.format.jpeg2000.internal.color;

import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponent;
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
