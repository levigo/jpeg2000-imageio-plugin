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
package com.levigo.jadice.format.jpeg2000.internal.tcq;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.buffer.Buffers;
import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.codestream.CodestreamMock;
import com.levigo.jadice.format.jpeg2000.internal.image.BandPrecinctMock;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlockMock;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlockState;
import com.levigo.jadice.format.jpeg2000.internal.image.Component;
import com.levigo.jadice.format.jpeg2000.internal.image.DefaultGridRegion;
import com.levigo.jadice.format.jpeg2000.internal.image.Pair;
import com.levigo.jadice.format.jpeg2000.internal.image.PrecinctMock;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.ResolutionMock;
import com.levigo.jadice.format.jpeg2000.internal.image.Subband;
import com.levigo.jadice.format.jpeg2000.internal.image.SubbandMock;
import com.levigo.jadice.format.jpeg2000.internal.image.SubbandType;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponentMock;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponentState;

public class InverseIrreversibleQuantizationTest {

  @Test
  public void testExponent8() throws JPEG2000Exception {
    final int precision = 8;

    final CodeBlock codeBlock = createMockCodeBlock(precision, 4, 4, SubbandType.LL);

    final Subband subband = codeBlock.bandPrecinct.subband;

    subband.mantissa = 0;
    subband.exponent = 8; // 2^(8 - 8) -> step size 1

    // @formatter:off
    setCoefficients(codeBlock, new int[]{
           0,    1,    2,    3,
           0,   -1,   -2,   -3,
         125,  126,  127,   128,
        -125, -126, -127,  -128,
    });
    // @formatter:on

    Quantizations.inverseIrreversible(codeBlock, subband.resolution.tileComp.state.sampleBuffer);
    final float[] result = subband.resolution.tileComp.state.sampleBuffer.floatSamples;

    // @formatter:off
    final float[] expectedSamples = new float[]{
           0.0f,    1.5f,    2.5f,    3.5f,
           0.0f,   -1.5f,   -2.5f,   -3.5f,
         125.5f,  126.5f,  127.5f,  128.5f,
        -125.5f, -126.5f, -127.5f, -128.5f,
    };
    // @formatter:on

    assertArrayEquals(expectedSamples, result, 0f);
  }

  @Test
  public void testExponent6() throws JPEG2000Exception {
    final int precision = 8;

    final CodeBlock codeBlock = createMockCodeBlock(precision, 4, 4, SubbandType.LL);

    final Subband subband = codeBlock.bandPrecinct.subband;

    subband.mantissa = 0;
    subband.exponent = 6;

    // @formatter:off
    setCoefficients(codeBlock, new int[]{
        0,    1,    2,    3,
        0,   -1,   -2,   -3,
        60,  61,   62,   63,
       -60, -61,  -62,  -63,
    });
    // @formatter:on

    Quantizations.inverseIrreversible(codeBlock, subband.resolution.tileComp.state.sampleBuffer);
    final float[] result = subband.resolution.tileComp.state.sampleBuffer.floatSamples;

    // @formatter:off
    final float[] expectedSamples = new float[]{
        0.0f,    6.0f,   10.0f,   14.0f,
        0.0f,   -6.0f,  -10.0f,  -14.0f,
      242.0f,  246.0f,  250.0f,  254.0f,
     -242.0f, -246.0f, -250.0f, -254.0f,
    };
    // @formatter:on

    assertArrayEquals(expectedSamples, result, 0f);
  }

  @Test
  public void testExponent6mantissa1024() throws JPEG2000Exception {
    final int precision = 8;

    final CodeBlock codeBlock = createMockCodeBlock(precision, 4, 4, SubbandType.LL);

    final Subband subband = codeBlock.bandPrecinct.subband;

    subband.exponent = 6; // 2^(8 - 6) -> step size 4
    subband.mantissa = 1024; // 1 + 1024/2048 -> 4 * 1.5 -> step size 6

    // @formatter:off
    setCoefficients(codeBlock, new int[]{
        0,    1,    2,    3,
        0,   -1,   -2,   -3,
       38,   39,   40,   41,
      -38,  -39,  -40,  -41,
    });
    // @formatter:on

    Quantizations.inverseIrreversible(codeBlock, subband.resolution.tileComp.state.sampleBuffer);
    final float[] result = subband.resolution.tileComp.state.sampleBuffer.floatSamples;

    // @formatter:off
    final float[] expectedSamples = new float[]{
        0.0f,    9.0f,   15.0f,   21.0f,
        0.0f,   -9.0f,  -15.0f,  -21.0f,
      231.0f,  237.0f,  243.0f,  249.0f,
     -231.0f, -237.0f, -243.0f, -249.0f,
    };
    // @formatter:on

    assertArrayEquals(expectedSamples, result, 0f);
  }

  @Test
  public void testExponent10() throws JPEG2000Exception {
    final int precision = 8;

    final CodeBlock codeBlock = createMockCodeBlock(precision, 4, 4, SubbandType.LL);

    final Subband subband = codeBlock.bandPrecinct.subband;

    subband.mantissa = 0;
    subband.exponent = 10; // 2^(8 - 10) -> step size .25

    // @formatter:off
    setCoefficients(codeBlock, new int[]{
        0,    1,    2,    3,
        0,   -1,   -2,   -3,
        60,  61,   62,   63,
       -60, -61,  -62,  -63,
    });
    // @formatter:on

    Quantizations.inverseIrreversible(codeBlock, subband.resolution.tileComp.state.sampleBuffer);
    final float[] result = subband.resolution.tileComp.state.sampleBuffer.floatSamples;

    // @formatter:off
    final float[] expectedSamples = new float[]{
        0.0f,    0.375f,   0.625f,   0.875f,
        0.0f,   -0.375f,  -0.625f,  -0.875f,
     15.125f,   15.375f,  15.625f,  15.875f,
     -15.125f, -15.375f, -15.625f, -15.875f,
    };
    // @formatter:on

    assertArrayEquals(expectedSamples, result, 0f);
  }

  @Test
  public void testExponent6SubbandLH() throws JPEG2000Exception {
    final int precision = 8;

    final CodeBlock codeBlock = createMockCodeBlock(precision, 4, 4, SubbandType.LH); // gain is 2

    final Subband subband = codeBlock.bandPrecinct.subband;

    subband.mantissa = 0;
    subband.exponent = 6;

    // @formatter:off
    setCoefficients(codeBlock, new int[]{
        0,    1,    2,    3,
        0,   -1,   -2,   -3,
        28,  29,   30,   31,
       -28, -29,  -30,  -31,
    });
    // @formatter:on

    Quantizations.inverseIrreversible(codeBlock, subband.resolution.tileComp.state.sampleBuffer);
    final float[] result = subband.resolution.tileComp.state.sampleBuffer.floatSamples;

    // @formatter:off
    final float[] expectedSamples = new float[]{
        0.0f,   12.0f,   20.0f,   28.0f,
        0.0f,  -12.0f,  -20.0f,  -28.0f,
      228.0f,  236.0f,  244.0f,  252.0f,
     -228.0f, -236.0f, -244.0f, -252.0f,
    };
    // @formatter:on

    assertArrayEquals(expectedSamples, result, 0f);
  }

  @Test
  public void testExponent6SubbandHL() throws JPEG2000Exception {
    final int precision = 8;

    final CodeBlock codeBlock = createMockCodeBlock(precision, 4, 4, SubbandType.LH); // gain is 2

    final Subband subband = codeBlock.bandPrecinct.subband;

    subband.mantissa = 0;
    subband.exponent = 6;

    // @formatter:off
    setCoefficients(codeBlock, new int[]{
        0,    1,    2,    3,
        0,   -1,   -2,   -3,
        28,  29,   30,   31,
       -28, -29,  -30,  -31,
    });
    // @formatter:on

    Quantizations.inverseIrreversible(codeBlock, subband.resolution.tileComp.state.sampleBuffer);
    final float[] result = subband.resolution.tileComp.state.sampleBuffer.floatSamples;

    // @formatter:off
    final float[] expectedSamples = new float[]{
        0.0f,   12.0f,   20.0f,   28.0f,
        0.0f,  -12.0f,  -20.0f,  -28.0f,
      228.0f,  236.0f,  244.0f,  252.0f,
     -228.0f, -236.0f, -244.0f, -252.0f,
    };
    // @formatter:on

    assertArrayEquals(expectedSamples, result, 0f);
  }

  @Test
  public void testExponent6SubbandHH() throws JPEG2000Exception {
    final int precision = 8;

    final CodeBlock codeBlock = createMockCodeBlock(precision, 4, 4, SubbandType.HH); // gain is 4

    final Subband subband = codeBlock.bandPrecinct.subband;

    subband.mantissa = 0;
    subband.exponent = 6;

    // @formatter:off
    setCoefficients(codeBlock, new int[]{
        0,    1,    2,    3,
        0,   -1,   -2,   -3,
        12,  13,   14,   15,
       -12, -13,  -14,  -15,
    });
    // @formatter:on

    Quantizations.inverseIrreversible(codeBlock, subband.resolution.tileComp.state.sampleBuffer);
    final float[] result = subband.resolution.tileComp.state.sampleBuffer.floatSamples;

    // @formatter:off
    final float[] expectedSamples = new float[]{
        0.0f,   24.0f,   40.0f,   56.0f,
        0.0f,  -24.0f,  -40.0f,  -56.0f,
      200.0f,  216.0f,  232.0f,  248.0f,
     -200.0f, -216.0f, -232.0f, -248.0f,
    };
    // @formatter:on

    assertArrayEquals(expectedSamples, result, 0f);
  }


  @Test
  public void testExponent8Guardbits2() throws JPEG2000Exception {
    final int precision = 8;

    final CodeBlock codeBlock = createMockCodeBlock(precision, 4, 4, SubbandType.LL);
    codeBlock.bandPrecinct.subband.guardBits = 2;

    final Subband subband = codeBlock.bandPrecinct.subband;

    subband.mantissa = 0;
    subband.exponent = 8; // 2^(8 - 8) -> step size 1

    // @formatter:off
    setCoefficients(codeBlock, new int[]{
           0,    1,    2,    3,
           0,   -1,   -2,   -3,
         125,  126,  127,   128,
        -125, -126, -127,  -128,
    });
    // @formatter:on

    Quantizations.inverseIrreversible(codeBlock, subband.resolution.tileComp.state.sampleBuffer);
    final float[] result = subband.resolution.tileComp.state.sampleBuffer.floatSamples;

    // @formatter:off
    final float[] expectedSamples = new float[]{
           0.0f,    1.5f,    2.5f,    3.5f,
           0.0f,   -1.5f,   -2.5f,   -3.5f,
         125.5f,  126.5f,  127.5f,  128.5f,
        -125.5f, -126.5f, -127.5f, -128.5f,
    };
    // @formatter:on

    assertArrayEquals(expectedSamples, result, 0f);
  }


  protected void setCoefficients(final CodeBlock codeBlock, final int[] coefficients) {
    final int p = codeBlock.bandPrecinct.subband.resolution.tileComp.comp.precision;
    for (int i = 0; i < coefficients.length; i++) {
      // convert from 2s complement to isolated sign bit
      if (coefficients[i] < 0)
        coefficients[i] = -coefficients[i] << p | 0x80000000;
      else
        coefficients[i] <<= p;
    }

    codeBlock.state.sampleBuffer = coefficients;
  }

  protected CodeBlock createMockCodeBlock(final int precision, int tileSize, int blockRegionSize, SubbandType subbandType)
      throws JPEG2000Exception {
    
    final Component comp = new Component();
    comp.isSigned = false;
    comp.precision = precision;

    final Region tileRegion = new Region(tileSize, tileSize);
    final Region blockRegion = new Region(blockRegionSize, blockRegionSize);
    
    final CodestreamMock codestream = new CodestreamMock();
    codestream.setGridElement(new DefaultGridRegion(tileRegion));

    final TileComponentMock tileComp = new TileComponentMock();
    tileComp.setGridRegion(new DefaultGridRegion(tileRegion));
    tileComp.state = new TileComponentState();
    tileComp.reversible = false;
    tileComp.comp = comp;

    final ResolutionMock resolution = new ResolutionMock();
    resolution.tileComp = tileComp;
    resolution.setGridRegion(new DefaultGridRegion(blockRegion));

    final PrecinctMock precinct = new PrecinctMock();
    precinct.resolution = resolution;
    precinct.setGridRegion(new DefaultGridRegion(blockRegion));

    final SubbandMock subband = new SubbandMock();
    subband.type = subbandType;
    subband.resolution = resolution;
    subband.Kmax = 31 - precision;
    subband.setGridRegion(new DefaultGridRegion(blockRegion));

    final BandPrecinctMock bandPrecinct = new BandPrecinctMock();
    bandPrecinct.subband = subband;
    bandPrecinct.setGridRegion(new DefaultGridRegion(blockRegion));

    final CodeBlockMock codeBlock = new CodeBlockMock();
    codeBlock.indices = new Pair(0, 0);
    codeBlock.setGridRegion(new DefaultGridRegion(blockRegion));
    codeBlock.state = new CodeBlockState();
    codeBlock.bandPrecinct = bandPrecinct;

    final DummyDataBuffer sampleBuffer = Buffers.createTileComponentBuffer(tileComp);
    assertNotNull(sampleBuffer);
    assertNotNull(sampleBuffer.floatSamples);

    tileComp.state.sampleBuffer = sampleBuffer;

    assertEquals(tileRegion.size.x, sampleBuffer.scanline);
    assertEquals(tileRegion.area(), sampleBuffer.floatSamples.length);
    
    return codeBlock;
  }
}
