package com.levigo.jadice.format.jpeg2000.internal.tcq;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.levigo.jadice.format.jpeg2000.internal.buffer.Buffers;
import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.image.BandPrecinctMock;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlockMock;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlockState;
import com.levigo.jadice.format.jpeg2000.internal.image.DefaultGridRegion;
import com.levigo.jadice.format.jpeg2000.internal.image.Pair;
import com.levigo.jadice.format.jpeg2000.internal.image.PrecinctMock;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.ResolutionMock;
import com.levigo.jadice.format.jpeg2000.internal.image.SubbandMock;
import com.levigo.jadice.format.jpeg2000.internal.image.SubbandType;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponentMock;

public class InverseReversibleQuantizationTest {

  @Test
  public void testUpperLeft() {
    final Region region4x4 = new Region(4, 4);
    final Region region8x8 = new Region(8, 8);
    
    final TileComponentMock tileComp = new TileComponentMock();
    tileComp.setGridRegion(new DefaultGridRegion(region8x8));
    tileComp.reversible = true;

    final ResolutionMock resolution = new ResolutionMock();
    resolution.dwtLevel = 1;
    resolution.resLevel = 0;
    resolution.tileComp = tileComp;
    resolution.setGridRegion(new DefaultGridRegion(region4x4));

    final PrecinctMock precinct = new PrecinctMock();
    precinct.resolution = resolution;
    precinct.setGridRegion(new DefaultGridRegion(region4x4));
    
    final SubbandMock subband = new SubbandMock();
    subband.idx = 0;
    subband.type = SubbandType.LL;
    subband.resolution = resolution;
    subband.setGridRegion(new DefaultGridRegion(region4x4));
    
    final BandPrecinctMock bandPrecinct = new BandPrecinctMock();
    bandPrecinct.precinct = precinct;
    bandPrecinct.subband = subband;
    bandPrecinct.setGridRegion(new DefaultGridRegion(region4x4));

    final CodeBlockMock codeBlock = new CodeBlockMock();
    codeBlock.indices = new Pair(0, 0);
    codeBlock.bandPrecinct = bandPrecinct;
    codeBlock.setGridRegion(new DefaultGridRegion(region4x4));

    // Prepare source samples
    final int precision = 8;
    subband.Kmax = 31 - precision;

    codeBlock.state = new CodeBlockState();
    codeBlock.numPassesTotal = 1;
    
    // @formatter:off
    codeBlock.state.sampleBuffer = new int[]{
        0, 1, 2, 3,
        1, 2, 3, 4,
        2, 3, 4, 5,
        3, 4, 5, 6,
    };
    // @formatter:on

    for(int i = 0; i < codeBlock.state.sampleBuffer.length; i++) {
      codeBlock.state.sampleBuffer[i] <<= precision;
    }

    final DummyDataBuffer sampleBuffer = Buffers.createTileComponentBuffer(tileComp);
    assertNotNull(sampleBuffer);
    assertNotNull(sampleBuffer.intSamples);
    assertEquals(region8x8.size.x, sampleBuffer.scanline);
    assertEquals(region8x8.area(), sampleBuffer.intSamples.length);

    Quantizations.inverseReversible(codeBlock, sampleBuffer);

    // @formatter:off
    final int[] expectedSamples = new int[]{
        0, 1, 2, 3, 0, 0, 0, 0,
        1, 2, 3, 4, 0, 0, 0, 0,
        2, 3, 4, 5, 0, 0, 0, 0,
        3, 4, 5, 6, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0
    };
    // @formatter:off

    assertArrayEquals(expectedSamples, sampleBuffer.intSamples);
  }

  @Test
  public void testIntermediate() {
    final Region region16x16 = new Region(16, 16);
    
    final TileComponentMock tileComp = new TileComponentMock();
    tileComp.reversible = true;
    tileComp.setGridRegion(new DefaultGridRegion(region16x16));
    
    final ResolutionMock resolution = new ResolutionMock();
    resolution.tileComp = tileComp;
    resolution.dwtLevel = 1;
    resolution.resLevel = 0;
    resolution.setGridRegion(new DefaultGridRegion(region16x16));

    final PrecinctMock precinct = new PrecinctMock();
    precinct.resolution = resolution;
    precinct.setGridRegion(new DefaultGridRegion(new Region(8, 0, 8, 8)));

    final SubbandMock subband = new SubbandMock();
    subband.type = SubbandType.HL;
    subband.resolution = resolution;
    subband.setGridRegion(new DefaultGridRegion(new Region(8, 0, 8, 8)));

    final BandPrecinctMock bandPrecinct = new BandPrecinctMock();
    bandPrecinct.precinct = precinct;
    bandPrecinct.subband = subband;
    bandPrecinct.setGridRegion(new DefaultGridRegion(new Region(9, 3, 4, 4)));

    final CodeBlockMock codeBlock = new CodeBlockMock();
    codeBlock.indices = new Pair(0, 0);
    codeBlock.bandPrecinct = bandPrecinct;
    codeBlock.setGridRegion(new DefaultGridRegion(new Region(9, 3, 4, 4)));

    // Prepare source samples
    final int precision = 8;
    subband.Kmax = 31 - precision;

    codeBlock.state = new CodeBlockState();
    codeBlock.numPassesTotal = 1;
    
    // @formatter:off
    codeBlock.state.sampleBuffer = new int[]{
        0, 1, 2, 3,
        1, 2, 3, 4,
        2, 3, 4, 5,
        3, 4, 5, 6,
    };
    // @formatter:on

    for (int i = 0; i < codeBlock.state.sampleBuffer.length; i++) {
      codeBlock.state.sampleBuffer[i] <<= precision;
    }

    final DummyDataBuffer sampleBuffer = Buffers.createTileComponentBuffer(tileComp);
    assertNotNull(sampleBuffer);
    assertNotNull(sampleBuffer.intSamples);
    assertEquals(region16x16.size.x, sampleBuffer.scanline);
    assertEquals(region16x16.area(), sampleBuffer.intSamples.length);

    Quantizations.inverseReversible(codeBlock, sampleBuffer);

    // @formatter:off
    final int[] expectedSamples = new int[]{
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 1, 2, 3, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0,   0, 1, 2, 3, 4, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 2, 3, 4, 5, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 3, 4, 5, 6, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,

        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 0
    };
    // @formatter:on

    assertArrayEquals(expectedSamples, sampleBuffer.intSamples);
  }

}
