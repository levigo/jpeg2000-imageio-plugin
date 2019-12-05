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

import org.jadice.jpeg2000.internal.buffer.DummyDataBuffer;
import org.jadice.jpeg2000.internal.dwt.Inverse9x7FilterTask;
import org.jadice.jpeg2000.internal.image.DefaultGridRegion;
import org.jadice.jpeg2000.internal.image.Region;
import org.jadice.jpeg2000.internal.image.ResolutionMock;
import org.jadice.jpeg2000.internal.image.TileComponentMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Inverse9x7FilterTaskTest {

  /**
   * Tests the 9-tap/7-tap convolutional filter with the example shown in ITU-T.800, J.4.1
   */
  @Test
  public void example_Part1() throws Exception {
    // @formatter:off
    final int[] original = new int[] {
       0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12,
       1,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12,
       2,  2,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12,
       3,  3,  3,  4,  5,  5,  6,  7,  8,  9, 10, 11, 12,
       4,  4,  4,  5,  5,  6,  7,  8,  8,  9, 10, 11, 12,
       5,  5,  5,  5,  6,  7,  7,  8,  9, 10, 11, 12, 13,
       6,  6,  6,  6,  7,  7,  8,  9, 10, 10, 11, 12, 13,
       7,  7,  7,  7,  8,  8,  9,  9, 10, 11, 12, 13, 13,
       8,  8,  8,  8,  8,  9, 10, 10, 11, 12, 12, 13, 14,
       9,  9,  9,  9,  9, 10, 10, 11, 12, 12, 13, 14, 15,
      10, 10, 10, 10, 10, 11, 11, 12, 12, 13, 14, 14, 15,
      11, 11, 11, 11, 11, 12, 12, 13, 13, 14, 14, 15, 16,
      12, 12, 12, 12, 12, 13, 13, 13, 14, 15, 15, 16, 16,
      13, 13, 13, 13, 13, 13, 14, 14, 15, 15, 16, 17, 17,
      14, 14, 14, 14, 14, 14, 15, 15, 16, 16, 17, 17, 18,
      15, 15, 15, 15, 15, 15, 16, 16, 17, 17, 18, 18, 19,
      16, 16, 16, 16, 16, 16, 17, 17, 17, 18, 18, 19, 20
    };
    // @formatter:on

    // @formatter:off
    final int[] samples = new int[] {
         1,  4,  8, 11,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         4,  5,  8, 11,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         8,  9, 11, 13,  0,  0,  0,  0,  0,  0,  0,  0,  0,
        12, 12, 14, 16,  0,  0,  0,  0, -1,  0,  0,  0,  0,
        15, 15, 17, 18,  0,  0,  0,  0,  0,  0, -1,  0,  0,
         0,  0,  0,  0, -1,  0,  0,  0,  0,  1,  1,  0,  0,
         0,  0,  0,  0,  0,  1,  0,  0,  0,  0,  0,  0, -1,
         0,  0,  0,  0,  0,  0,  0,  0,  0, -1, -1, -1,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0, -1,  0,  0,  0,  0,  0, 
         0,  0,  0, -1,  0,  0,  0,  0,  0, -1,  0,  0,  0,
         0,  0,  0,  0,  0,  1,  1,  0, -1,  1,  0,  0,  0,
         0,  0,  0,  0, -1,  1,  0,  0,  0,  0,  0,  0,  1,
         0,  0,  0,  0,  0,  0,  1,  0,  0,  0,  0, -1,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
         0,  0,  0,  0,  0,  0,  0,  0,  0, -1,  0, -1,  1,
         0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, -1,  0
    };
    // @formatter:on

    // @formatter:off
    final int[] expecteds = new int[] {
        -1,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12,
         1,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 11,
         2,  2,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 11,
         3,  3,  3,  4,  4,  4,  5,  6,  8,  9, 10, 11, 11,
         4,  4,  4,  5,  4,  5,  6,  7,  8,  9, 10, 11, 11,
         5,  5,  5,  4,  6,  7,  7,  8,  8, 10, 11, 12, 12,
         6,  6,  6,  6,  7,  7,  8,  9,  9, 10, 10, 11, 12,
         7,  7,  7,  7,  8,  8,  9,  9,  9, 11, 12, 13, 12,
         8,  8,  8,  9,  9,  9, 10, 10, 12, 12, 12, 12, 13,
         9,  9,  9, 10, 10, 11, 10, 11, 12, 12, 13, 14, 15,
        10, 10, 10, 10, 10, 11, 11, 12, 12, 13, 14, 14, 14,
        11, 11, 11, 11, 11, 12, 12, 13, 13, 14, 15, 15, 16,
        12, 12, 12, 12, 12, 12, 13, 13, 14, 15, 16, 15, 17,
        13, 13, 13, 13, 13, 13, 14, 14, 16, 15, 17, 17, 17,
        14, 14, 14, 14, 14, 14, 15, 15, 17, 16, 17, 17, 18,
        15, 15, 15, 15, 15, 15, 16, 16, 17, 17, 18, 18, 18,
        16, 15, 15, 15, 15, 16, 16, 17, 17, 18, 18, 18, 18
    };
    // @formatter:on

    final DummyDataBuffer dataBuffer = new DummyDataBuffer();
    dataBuffer.floatSamples = new float[samples.length];
    dataBuffer.scanline = 13;

    // Inverse9x7FilterTask wants floats
    for (int i = 0; i < samples.length; i++) {
      dataBuffer.floatSamples[i] = samples[i];
    }

    TileComponentMock tileComp = new TileComponentMock();
    tileComp.setGridRegion(new DefaultGridRegion(new Region(13,17)));

    ResolutionMock r1 = new ResolutionMock();
    r1.tileComp = tileComp;
    r1.dwtLevel = 1;
    r1.resLevel = 1;
    r1.setGridRegion(new DefaultGridRegion(new Region(7, 9)));
    r1.subbandPartition = new Region(4, 5);

    ResolutionMock r2 = new ResolutionMock();
    r2.tileComp = tileComp;
    r2.dwtLevel = 0;
    r2.resLevel = 2;
    r2.setGridRegion(new DefaultGridRegion(new Region(13, 17)));
    r2.subbandPartition = new Region(7, 9);

    Inverse9x7FilterTask task = new Inverse9x7FilterTask(dataBuffer, r1);
    task.call();

    task = new Inverse9x7FilterTask(dataBuffer, r2);
    task.call();

    for (int i = 0; i < samples.length; i++) {
      samples[i] = Math.round(dataBuffer.floatSamples[i]);
    }

    /*
     * dumpSamples("EXPECTED", expecteds, samples.length / dataBuffer.scanline,
     * dataBuffer.scanline);
     * 
     * dumpSamples("RESULT", dataBuffer.floatSamples, samples.length / dataBuffer.scanline,
     * dataBuffer.scanline);
     * 
     * dumpSamples("RESULT (int)", samples, samples.length / dataBuffer.scanline,
     * dataBuffer.scanline);
     * 
     * dumpSamples("DIFF - ORIGINAL", diff(original, samples), samples.length / dataBuffer.scanline,
     * dataBuffer.scanline);
     * 
     * dumpSamples("DIFF - EXPECTED", diff(expecteds, samples), samples.length /
     * dataBuffer.scanline, dataBuffer.scanline);
     */

    Assertions.assertArrayEquals(expecteds, samples);
  }

  protected int[] diff(final int[] a, final int[] b) {
    final int diff[] = new int[b.length];
    for (int j = 0; j < diff.length; j++) {
      diff[j] = a[j] - b[j];
    }
    return diff;
  }

  protected void dumpSamples(String title, int[] sampleBuffer, int rows, int columns) {
    System.out.println(title);
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        System.out.printf("%5d ", sampleBuffer[r * columns + c]);
      }
      System.out.println();
    }
  }
  
  protected void dumpSamples(String title, float[] sampleBuffer, int rows, int columns) {
    System.out.println(title);
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        System.out.printf("%5.1f ", sampleBuffer[r * columns + c]);
      }
      System.out.println();
    }
  } 
}
