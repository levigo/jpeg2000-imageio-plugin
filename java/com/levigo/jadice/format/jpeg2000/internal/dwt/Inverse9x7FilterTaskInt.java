package com.levigo.jadice.format.jpeg2000.internal.dwt;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import com.levigo.jadice.document.internal.render.debug.PartialResultsDebugger;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.buffer.Buffers;
import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.image.Canvas;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;

public class Inverse9x7FilterTaskInt implements DecodeTask<Resolution> {

  private final static float ALPHA = -1.586134342059924f;
  private final static float BETA = -0.052980118572961f;
  private final static float GAMMA = 0.882911075530934f;
  private final static float DELTA = 0.443506852043971f;
  private final static float KL = 1.230174104914001f;
  private final static float KH = 1 / KL;

  private final Resolution resolution;

  private final int[] samples;
  private final int scanline;

  private final Region resolutionRegion;
  private final Region subbandPartition;

  public Inverse9x7FilterTaskInt(DummyDataBuffer sampleBuffer, Resolution resolution) {
    this.resolution = resolution;

    samples = sampleBuffer.intSamples;
    scanline = sampleBuffer.scanline;

    resolutionRegion = resolution.region().relativeTo(resolution.tileComp.region());
    subbandPartition = resolution.subbandPartition;
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

    final int kl = upshift(KL, 16);
    final int kh = upshift(KH, 16);
    final int delta = upshift(DELTA, 16);
    final int gamma = upshift(GAMMA, 16);
    final int beta = upshift(BETA, 16);
    final int alpha = upshift(ALPHA, 16);

    // Horizontal filtering for each row
    int offset = initialOffset;
    for (int y = y0; y < y2; y++, offset += scanline) {
      final int[] low = Buffers.copyHorizontal(samples, offset, offset + x1, kl);
      final int[] high = Buffers.copyHorizontal(samples, offset + x1, offset + x2, kh);
      liftLow(low, high, even, jn, delta); // STEP 3
      liftHigh(low, high, even, kn, gamma); // STEP 4
      liftLow(low, high, even, jn, beta); // STEP 5
      liftHigh(low, high, even, kn, alpha); // STEP 6
      
      // TODO
      // Save back low and high in samples with applied float to int conversion and interleaving. Currently we simply 
      // use rounding and casting until the extraction and the re-integration 
      
      for(int i = offset, j = 0; j < low.length; i += 2, j++) {
        samples[i] = low[j] >> 16;
      }

      for (int i = offset + 1, k = 0; k < high.length; i += 2, k++) {
        samples[i] = high[k] >> 16;
      }

      if(Debug.VISUALIZE_DWT) {
        final Raster raster = samplesToRaster();
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
      final int[] low = Buffers.copyVertical(samples, offsetL, j1, scanline, kl);
      final int[] high = Buffers.copyVertical(samples, offsetH, k1, scanline, kh);

      liftLow(low, high, even, jn, delta); // STEP 3
      liftHigh(low, high, even, kn, gamma); // STEP 4
      liftLow(low, high, even, jn, beta); // STEP 5
      liftHigh(low, high, even, kn, alpha); // STEP 6

      // TODO
      // Save back low and high in samples with applied float to int conversion and interleaving. Currently we simply use
      // rounding and casting until the extraction and the re-integration 

      for(int i = offsetL, j = 0; j < low.length; i += scanlineBy2, j++) {
        samples[i] = low[j] >> 16;
      }

      for (int i = offsetL + scanline, k = 0; k < high.length; i += scanlineBy2, k++) {
        samples[i] = high[k] >> 16;
      }
      
      if(Debug.VISUALIZE_DWT) {
        final Raster raster = samplesToRaster();
        PartialResultsDebugger.getInstance().addDebugResult(raster, BufferedImage.TYPE_BYTE_GRAY, "9x7 column " + x);
      }
    }

    
    return resolution;
  }

  protected int upshift(float f, int bits) {
    return Math.round(f * (1 << bits));
  }

  private void liftLow(int[] low, int[] high, boolean even, int jn, int liftingFactor) {
    int h0 = high[0];

    low[0] -= liftingFactor * (h0 + h0);

    int h1;
    for(int j = 1; j < jn; j++) {
      h1 = high[j];
      low[j] -= liftingFactor * (h0 + h1);
      h0 = h1;
    }

    if(!even) {
      low[jn] -= liftingFactor * (h0 + h0);
    }
  }
  
  private void liftHigh(int[] low, int[] high, boolean even, int kn, int liftingFactor) {
    int l0 = low[0];
    int l1;
    for (int k = 0; k < kn; k++) {
      l1 = low[k + 1];
      high[k] -= liftingFactor * (l0 + l1);
      l0 = l1;
    }

    if (even) {
      high[kn] -= liftingFactor * (l0 + l0);
    }
  }

  private Raster samplesToRaster() {
    final int length = samples.length;
    final int w = scanline;
    final int h = length / scanline;
    final WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, w, h, 1, new Point(0, 0));

    for (int y = 0, i = 0; y < h; y++) {
      for (int x = 0; x < w; x++, i++) {
        raster.setSample(x, y, 0, samples[i] + 128); // +128 for level shifting
      }
    }

    return raster;
  }

}
