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
package org.jadice.jpeg2000.internal.dwt;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import org.jadice.jpeg2000.internal.Debug;
import org.jadice.jpeg2000.internal.buffer.Buffers;
import org.jadice.jpeg2000.internal.buffer.DummyDataBuffer;
import org.jadice.jpeg2000.internal.decode.push.DecodeTask;
import org.jadice.jpeg2000.internal.image.Canvas;
import org.jadice.jpeg2000.internal.image.Region;
import org.jadice.jpeg2000.internal.image.Resolution;

import com.levigo.jadice.document.internal.render.debug.PartialResultsDebugger;

public class Inverse5x3FilterTask implements DecodeTask<Resolution> {

  private final Resolution resolution;

  private final int[] samples;
  private final int scanline;

  private final Region resolutionRegion;
  private final Region subbandPartition;

  public Inverse5x3FilterTask(DummyDataBuffer sampleBuffer, Resolution resolution) {
    this.resolution = resolution;

    samples = sampleBuffer.intSamples;
    scanline = sampleBuffer.scanline;

    resolutionRegion = resolution.region().absolute();
    subbandPartition = resolution.subbandPartition;
  }

  @Override
  public Resolution call() throws Exception {
    final int x0 = subbandPartition.x0();
    final int x1 = subbandPartition.x1();
    final int x2 = resolutionRegion.x1();

    final int y0 = subbandPartition.y0();
    final int y1 = subbandPartition.y1();
    final int y2 = resolutionRegion.y1();

    boolean even = Canvas.isEven(x2 - x0);

    int j0 = 0;
    int j1 = x1 - x0;
    int k0 = 0;
    int k1 = x2 - x1;

    int jn = even ? j1 : j1 - 1;
    int kn = even ? k1 - 1 : k1;

    // Horizontal filtering for each row
    if (x1 != x2) {
      int offset = 0;
      for (int y = y0; y < y2; y++, offset += scanline) {
        final int[] l = Buffers.copyHorizontal(samples, offset, offset + j1);
        final int[] h = Buffers.copyHorizontal(samples, offset + j1, offset + j1 + k1);

        int x = offset;
        int j = j0;

        /* STEP 1 begin */
        samples[x] = l[j] - (((h[k0] << 1) + 2) >> 2);

        for (j++, x += 2; j < jn; j++, x += 2) {
          samples[x] = l[j] - ((h[j + k0 - 1] + h[j + k0] + 2) >> 2);
        }

        if (!even) {
          samples[x] = l[j] - (((h[k1 - 1] << 1) + 2) >> 2);
        }
        /* STEP 1 end */

        /* STEP 2 begin */
        int k;
        for (k = k0, x = offset + 1; k < kn; k++, x += 2) {
          samples[x] = h[k] + ((samples[x - 1] + samples[x + 1]) >> 1);
        }

        if (even) {
          samples[x] = h[k] + samples[x - 1];
        }
        /* STEP 2 end */

        if (Debug.VISUALIZE_DWT) {
          final Raster raster = Debug.intSamplesToRaster(samples, scanline);
          // Debug.dump(raster, BufferedImage.TYPE_BYTE_GRAY, "5x3 row " + y);
          PartialResultsDebugger.getInstance().addDebugResult(raster, BufferedImage.TYPE_BYTE_GRAY, "5x3 row " + y);
        }
      }
    }

    even = Canvas.isEven(y2 - y0);

    j1 = y1 - y0;
    k1 = y2 - y1;

    jn = even ? j1 : j1 - 1;
    kn = even ? k1 - 1 : k1;

    // Vertical filtering for each column
    if (y1 != y2) {
      int offsetL = 0;
      int offsetH = j1 * scanline;
      final int scanlineBy2 = scanline + scanline;

      for (int x = x0; x < x2; x++, offsetL++, offsetH++) {
        final int[] l = Buffers.copyVertical(samples, offsetL, j1, scanline);
        final int[] h = Buffers.copyVertical(samples, offsetH, k1, scanline);

        int y = offsetL;
        int j = j0;

        samples[y] = l[j] - (((h[k0] << 1) + 2) >> 2);

        for (j++, y += scanlineBy2; j < jn; j++, y += scanlineBy2) {
          samples[y] = l[j] - ((h[j + k0 - 1] + h[j + k0] + 2) >> 2);
        }

        if (!even) {
          samples[y] = l[j] - (((h[k1 - 1] << 1) + 2) >> 2);
        }

        int k;
        for (k = k0, y = offsetL + scanline; k < kn; k++, y += scanlineBy2) {
          samples[y] = h[k] + ((samples[y - scanline] + samples[y + scanline]) >> 1);
        }

        if (even) {
          samples[y] = h[k] + samples[y - scanline];
        }

        if (Debug.VISUALIZE_DWT) {
          final Raster raster = Debug.intSamplesToRaster(samples, scanline);
          // Debug.dump(raster, BufferedImage.TYPE_BYTE_GRAY, "5x3 column " + x);
          PartialResultsDebugger.getInstance().addDebugResult(raster, BufferedImage.TYPE_BYTE_GRAY, "5x3 column " + x);
        }
      }
    }

    return resolution;
  }

}
