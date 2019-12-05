package com.levigo.jadice.format.jpeg2000.internal.dwt;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import com.levigo.jadice.document.internal.render.debug.PartialResultsDebugger;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.buffer.Buffers;
import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.image.Canvas;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;

public class Inverse9x7FilterTask implements DecodeTask<Resolution> {

  private final static float ALPHA = -1.586134342059924f;
  private final static float BETA = -0.052980118572961f;
  private final static float GAMMA = 0.882911075530934f;
  private final static float DELTA = 0.443506852043971f;
  private final static float KL = 1.230174104914001f;
  private final static float KH = 1 / KL;

  private final Resolution resolution;

  private final float[] samples;
  private final int scanline;

  private final Region resolutionRegion;
  private final Region subbandPartition;

  public Inverse9x7FilterTask(DummyDataBuffer sampleBuffer, Resolution resolution) {
    this.resolution = resolution;

    samples = sampleBuffer.floatSamples;
    scanline = sampleBuffer.scanline;

    resolutionRegion = resolution.region().relativeTo(resolution.region());
    subbandPartition = resolution.subbandPartition;
    subbandPartition.displaceBy(subbandPartition.pos.inverse());
  }

  @Override
  public Resolution call() throws Exception {
    final int x0 = resolutionRegion.x0();
    final int x1 = subbandPartition.x1();
    final int x2 = resolutionRegion.x1();

    final int y0 = resolutionRegion.y0();
    final int y1 = subbandPartition.y1();
    final int y2 = resolutionRegion.y1();

    boolean even = Canvas.isEven(x2 - x0);

    int j1 = x1;
    int jn = even ? j1 : j1 - 1;

    int k1 = x2 - x1;
    int kn = even ? k1 - 1 : k1;

    final int initialOffset = y0 * scanline + x0;

    // Horizontal filtering for each row
    int offset = initialOffset;
    for (int y = y0; y < y2; y++, offset += scanline) {
      final float[] low = Buffers.copyHorizontal(samples, offset, offset + x1, KL);
      final float[] high = Buffers.copyHorizontal(samples, offset + x1, offset + x2, KH);
      liftLow(low, high, even, jn, DELTA); // STEP 3
      liftHigh(low, high, even, kn, GAMMA); // STEP 4
      liftLow(low, high, even, jn, BETA); // STEP 5
      liftHigh(low, high, even, kn, ALPHA); // STEP 6
      
      // TODO
      // Save back low and high in samples with applied float to int conversion and interleaving. Currently we simply 
      // use rounding and casting until the extraction and the re-integration 
      
      for(int i = offset, j = 0; j < low.length; i += 2, j++) {
        samples[i] = low[j]; // Math.round(low[j]);
      }

      for (int i = offset + 1, k = 0; k < high.length; i += 2, k++) {
        samples[i] = high[k]; // Math.round(high[k]);
      }

      if(Debug.VISUALIZE_DWT) {
        final Raster raster = Debug.floatSamplesToRaster(samples, scanline);
        PartialResultsDebugger.getInstance().addDebugResult(raster, BufferedImage.TYPE_BYTE_GRAY, "9x7 row " + y);
      }
    }

    even = Canvas.isEven(y2 - y0);

    j1 = y1;
    jn = even ? j1 : j1 - 1;

    k1 = y2 - y1;
    kn = even ? k1 - 1 : k1;

    // TODO
    // Vertical filtering for each column
    int offsetL = initialOffset;
    int offsetH = y1 * scanline + x0;
    final int scanlineBy2 = scanline + scanline;

    for(int x = x0; x < x2; x++, offsetL++, offsetH++) {
      final float[] low = Buffers.copyVertical(samples, offsetL, j1, scanline, KL);
      final float[] high = Buffers.copyVertical(samples, offsetH, k1, scanline, KH);

      liftLow(low, high, even, jn, DELTA); // STEP 3
      liftHigh(low, high, even, kn, GAMMA); // STEP 4
      liftLow(low, high, even, jn, BETA); // STEP 5
      liftHigh(low, high, even, kn, ALPHA); // STEP 6

      // TODO
      // Save back low and high in samples with applied float to int conversion and interleaving. Currently we simply use
      // rounding and casting until the extraction and the re-integration 

      for(int i = offsetL, j = 0; j < low.length; i += scanlineBy2, j++) {
        samples[i] = low[j]; // Math.round(low[j]);
      }

      for (int i = offsetL + scanline, k = 0; k < high.length; i += scanlineBy2, k++) {
        samples[i] = high[k]; // Math.round(high[k]);
      }
      
      if(Debug.VISUALIZE_DWT) {
        final Raster raster = Debug.floatSamplesToRaster(samples, scanline);
        PartialResultsDebugger.getInstance().addDebugResult(raster, BufferedImage.TYPE_BYTE_GRAY, "9x7 column " + x);
      }
    }

    
    return resolution;
  }

  private void liftLow(float[] low, float[] high, boolean even, int jn, float liftingFactor) {
    if (high.length == 0) {
      return; // No high-pass signal for lifting low-pass signal
    }

    float h0 = high[0];

    low[0] -= liftingFactor * (h0 + h0);

    float h1;
    for (int j = 1; j < jn; j++) {
      h1 = high[j];
      low[j] -= liftingFactor * (h0 + h1);
      h0 = h1;
    }

    if (!even) {
      low[jn] -= liftingFactor * (h0 + h0);
    }
  }

  private void liftHigh(float[] low, float[] high, boolean even, int kn, float liftingFactor) {
    if (low.length == 0) {
      return; // No low-pass signal for lifting high-pass signal
    }

    float l0 = low[0];
    float l1;
    for (int k = 0; k < kn; k++) {
      l1 = low[k + 1];
      high[k] -= liftingFactor * (l0 + l1);
      l0 = l1;
    }

    if (even) {
      high[kn] -= liftingFactor * (l0 + l0);
    }
  }

}
