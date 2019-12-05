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
package com.levigo.jadice.format.jpeg2000.internal.buffer;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class BuffersTest {
  
  @Test
  public void fullVerticalCopy() {
    final int width = 4;
    final int height = 8;

    // @formatter:off
    final int[] samples = new int[]{
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3
    };
    // @formatter:on

    final int[] expecteds = new int[height];

    for (int i = 0; i < width; i++) {
      Arrays.fill(expecteds, i);
      final int[] actuals = Buffers.copyVertical(samples, i, height, width);
      Assert.assertArrayEquals(expecteds, actuals);
    }
  }
  @Test
  public void fullVerticalScaledCopy() {
    final int width = 4;
    final int height = 8;

    // @formatter:off
    final int[] samples = new int[]{
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3
    };
    // @formatter:on

    final float scaleFactor = 2;
    final float[] expecteds = new float[height];

    for (int i = 0; i < width; i++) {
      Arrays.fill(expecteds, i * scaleFactor);
      final float[] actuals = Buffers.copyVertical(samples, i, height, width, scaleFactor);
      Assert.assertArrayEquals(expecteds, actuals, 0);
    }
  }
  
  @Test
  public void partialVerticalCopy() {
    final int width = 4;
    final int height = 8;

    // @formatter:off
    final int[] samples = new int[]{
        0, 1, 2, 3,
        1, 2, 3, 4,
        2, 3, 4, 5,
        3, 4, 5, 6,
        4, 5, 6, 7,
        5, 6, 7, 8,
        6, 7, 8, 9,
        7, 8, 9, 10
    };

    final int[][] expecteds = new int[][] {
        {1, 2, 3, 4},
        {2, 3, 4, 5},
        {3, 4, 5, 6},
        {4, 5, 6, 7}
    };
    // @formatter:on

    for (int i = 0; i < width; i++) {
      Assert.assertArrayEquals(expecteds[i], Buffers.copyVertical(samples, width + i, height / 2, width));
    }
  }
  
  @Test
  public void partialVerticalScaledCopy() {
    final int width = 4;
    final int height = 8;

    // @formatter:off
    final int[] samples = new int[]{
        0, 1, 2, 3,
        1, 2, 3, 4,
        2, 3, 4, 5,
        3, 4, 5, 6,
        4, 5, 6, 7,
        5, 6, 7, 8,
        6, 7, 8, 9,
        7, 8, 9, 10
    };
    
    final float scaleFactor = 0.5f;

    final float[][] expecteds = new float[][] {
        {0.5f, 1, 1.5f, 2},
        {1, 1.5f, 2, 2.5f},
        {1.5f, 2, 2.5f, 3},
        {2, 2.5f, 3, 3.5f}
    };
    // @formatter:on

    for (int i = 0; i < width; i++) {
      final int from = width + i;
      final int numSamples = height / 2;
      final float[] actuals = Buffers.copyVertical(samples, from, numSamples, width, scaleFactor);
      Assert.assertArrayEquals(expecteds[i], actuals, 0);
    }
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIllegalNumSamplesForVerticalCopy() {
    Buffers.copyVertical(new int[]{0, 0}, 0, -1, 0);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIllegalNumSamplesForVerticalScaledCopy() {
    Buffers.copyVertical(new int[]{0, 0}, 0, -1, 0, 2);
  }
  
  @Test
  public void fullHorizontalCopy() {
    final int width = 4;
    final int height = 8;

    // @formatter:off
    final int[] samples = new int[]{
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3
    };
    // @formatter:on

    final int[] expecteds = {0, 1, 2, 3};

    for (int i = 0; i < height; i++) {
      final int from = i * width;
      final int to = from + width;
      final int[] actuals = Buffers.copyHorizontal(samples, from, to);
      Assert.assertArrayEquals(expecteds, actuals);
    }
  }

  @Test
  public void partialHorizontalCopy() {
    final int width = 8;
    final int height = 4;

    // @formatter:off
    final int[] samples = new int[]{
        0, 1, 2, 3, 4, 5, 6, 7,
        1, 2, 3, 4, 5, 6, 7, 8,
        2, 3, 4, 5, 6, 7, 8, 9,
        3, 4, 5, 6, 7, 8, 9, 10
    };

    final int[][] expecteds = new int[][] {
        {0, 1, 2, 3},
        {2, 3, 4, 5},
        {4, 5, 6, 7},
        {6, 7, 8, 9}
    };
    // @formatter:on

    for (int i = 0; i < height; i++) {
      final int from = i * width + i;
      final int to = from + 4;
      final int[] actuals = Buffers.copyHorizontal(samples, from, to);
      Assert.assertArrayEquals(expecteds[i], actuals);
    }
  }
  
  @Test
  public void fullHorizontalScaledCopy() {

    final int width = 4;
    final int height = 8;

    // @formatter:off
    final int[] samples = new int[]{
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3,
        0, 1, 2, 3
    };
    // @formatter:on

    final float scaleFactor = 2;
    
    final float[] expecteds = {0, 2, 4, 6};

    for (int i = 0; i < height; i++) {
      final int from = i * width;
      final int to = from + width;
      final float[] actuals = Buffers.copyHorizontal(samples, from, to, scaleFactor);
      Assert.assertArrayEquals(expecteds, actuals, 0);
    }
  }

  @Test
  public void partialHorizontalScaledCopy() {
    final int width = 8;
    final int height = 4;

    // @formatter:off
    final int[] samples = new int[]{
        0, 1, 2, 3, 4, 5, 6, 7,
        1, 2, 3, 4, 5, 6, 7, 8,
        2, 3, 4, 5, 6, 7, 8, 9,
        3, 4, 5, 6, 7, 8, 9, 10
    };
    
    final float scaleFactor = 0.5f;

    final float[][] expecteds = new float[][] {
        {0, 0.5f, 1, 1.5f},
        {1, 1.5f, 2, 2.5f},
        {2, 2.5f, 3, 3.5f},
        {3, 3.5f, 4, 4.5f}
    };
    // @formatter:on

    for (int i = 0; i < height; i++) {
      final int from = i * width + i;
      final int to = from + 4;
      final float[] actuals = Buffers.copyHorizontal(samples, from, to, scaleFactor);
      Assert.assertArrayEquals(expecteds[i], actuals, 0);
    }
  }
  
  @Test
  public void testThat_cropInt_atLowerRight_returnsExpectedInts() {
    final int[] samples = new int[]{
        0, 1, 2, 3, 4, 5, 6, 7,
        1, 2, 3, 4, 5, 6, 7, 8,
        2, 3, 4, 5, 6, 7, 8, 9,
        3, 4, 5, 6, 7, 8, 9, 10
    };

    final int[] expecteds = new int[]{
        4, 5, 6, 7, 8,
        5, 6, 7, 8, 9,
        6, 7, 8, 9, 10
    };

    final int[] actuals = Buffers.crop(samples, 3, 1, 5, 3, 8);

    Assert.assertArrayEquals(expecteds, actuals);
  }
  
  @Test
  public void testThat_cropInt_atVerticalOffset_returnsExpectedInts() {
    final int[] samples = new int[]{
        0, 1, 2, 3, 4, 5, 6, 7,
        1, 2, 3, 4, 5, 6, 7, 8,
        2, 3, 4, 5, 6, 7, 8, 9,
        3, 4, 5, 6, 7, 8, 9, 10
    };

    final int[] expecteds = new int[]{
        2, 3, 4, 5, 6, 7, 8, 9,
        3, 4, 5, 6, 7, 8, 9, 10
    };

    final int[] actuals = Buffers.crop(samples, 0, 2, 8, 2, 8);

    Assert.assertArrayEquals(expecteds, actuals);
  }
  
  @Test
  public void testThat_cropFloat_atLowerRight_returnsExpectedInts() {
    final float[] samples = new float[]{
        0, 1, 2, 3, 4, 5, 6, 7,
        1, 2, 3, 4, 5, 6, 7, 8,
        2, 3, 4, 5, 6, 7, 8, 9,
        3, 4, 5, 6, 7, 8, 9, 10
    };

    final float[] expecteds = new float[]{
        4, 5, 6, 7, 8,
        5, 6, 7, 8, 9,
        6, 7, 8, 9, 10
    };

    final float[] actuals = Buffers.crop(samples, 3, 1, 5, 3, 8);

    Assert.assertArrayEquals(expecteds, actuals, 0.00001f);
  }
  
  @Test
  public void testThat_cropFloat_atVerticalOffset_returnsExpectedInts() {
    final float[] samples = new float[]{
        0, 1, 2, 3, 4, 5, 6, 7,
        1, 2, 3, 4, 5, 6, 7, 8,
        2, 3, 4, 5, 6, 7, 8, 9,
        3, 4, 5, 6, 7, 8, 9, 10
    };

    final float[] expecteds = new float[]{
        2, 3, 4, 5, 6, 7, 8, 9,
        3, 4, 5, 6, 7, 8, 9, 10
    };

    final float[] actuals = Buffers.crop(samples, 0, 2, 8, 2, 8);

    Assert.assertArrayEquals(expecteds, actuals, 0.00001f);
  }

}
