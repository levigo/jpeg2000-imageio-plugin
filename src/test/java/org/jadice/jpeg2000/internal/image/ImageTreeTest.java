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
package org.jadice.jpeg2000.internal.image;

import static org.jadice.jpeg2000.internal.image.Resolution.subbandArrayIndex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.Tests;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.codestream.CodestreamMock;
import org.jadice.jpeg2000.internal.codestream.Codestreams;
import org.jadice.jpeg2000.internal.codestream.TilePartPointer;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.Receiver;
import org.jadice.jpeg2000.internal.decode.push.stage.ForwardTilePartReading;
import org.jadice.jpeg2000.internal.image.BandPrecinct;
import org.jadice.jpeg2000.internal.image.CodeBlock;
import org.jadice.jpeg2000.internal.image.Component;
import org.jadice.jpeg2000.internal.image.GridRegion;
import org.jadice.jpeg2000.internal.image.Pair;
import org.jadice.jpeg2000.internal.image.Precinct;
import org.jadice.jpeg2000.internal.image.Region;
import org.jadice.jpeg2000.internal.image.Resolution;
import org.jadice.jpeg2000.internal.image.Subband;
import org.jadice.jpeg2000.internal.image.SubbandType;
import org.jadice.jpeg2000.internal.image.Tile;
import org.jadice.jpeg2000.internal.image.TileComponent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.levigo.jadice.document.io.SeekableInputStream;

@RunWith(Parameterized.class)
public class ImageTreeTest {

  private static final Gson GSON;
  static {
    final GsonBuilder gson = new GsonBuilder();
    gson.registerTypeAdapter(Pair.class, new PairAdapter());
    gson.registerTypeAdapter(Region.class, new RegionAdapter());
    gson.registerTypeAdapter(GridRegion.class, new GridRegionAdapter());
    GSON = gson.create();
  }

  @Parameters(name = "test[{index}]: {1}")
  public static Object[][] parameters() {
    return new Object[][] {
        {"/com/levigo/jadice/format/jpeg2000/internal/image/p0_01.j2k.image.json","/codestreams/profile0/p0_01.j2k"},
        {"/com/levigo/jadice/format/jpeg2000/internal/image/p0_02.j2k.image.json","/codestreams/profile0/p0_02.j2k"},
        {"/com/levigo/jadice/format/jpeg2000/internal/image/multi-tiles.image.json", "/files/AntragPK363.Im0-53.jp2"}
    };
  }

  @Parameter(0)
  public String traceDataFilePath;

  @Parameter(1)
  public String sourceDataFilePath;

  private Codestream expectedCodestream;
  private Codestream actualCodestream;

  @Before
  public void setup() throws IOException, JPEG2000Exception {
    final SeekableInputStream tracaDataStream = Tests.openResource(traceDataFilePath);
    final Reader json = new BufferedReader(new InputStreamReader(tracaDataStream));
    expectedCodestream = GSON.fromJson(json, CodestreamMock.class);

    final SeekableInputStream source = Tests.openResource(sourceDataFilePath);
    final SeekableInputStream codestreamSource = Codestreams.createCodestreamSource(source);
    actualCodestream = new Codestream(codestreamSource);
    actualCodestream.init();
    
    final ForwardTilePartReading reader = new ForwardTilePartReading(new Receiver<TilePartPointer>() {
      @Override
      public void receive(TilePartPointer tilePartPointer, DecoderParameters parameters) throws JPEG2000Exception {
        try {
          codestreamSource.seek(tilePartPointer.tilePartStart + tilePartPointer.tilePartLength);
        } catch (IOException e) {
          e.printStackTrace();
          Assert.fail(e.getMessage());
        }
      }
    });

    final DecoderParameters parameters = new DecoderParameters();
    parameters.region = actualCodestream.region().absolute();
    parameters.validate = true;

    reader.receive(actualCodestream, parameters);
  }

  @Test
  public void compareTreeElements() throws JPEG2000Exception {
    inspectCodestream();

    inspectComponents(actualCodestream.comps, expectedCodestream.comps);

    for (int c = 0; c < expectedCodestream.comps.length; c++) {
      final Component expectedComp = expectedCodestream.comps[c];
      final Component actualComp = actualCodestream.comps[c];
      inspectComponent(c, expectedComp, actualComp);
    }

    final Tile[] expectedTiles = expectedCodestream.tiles;
    final Tile[] actualTiles = actualCodestream.tiles;

    inspectTileArray(expectedTiles, actualTiles);

    for (int t = 0; t < expectedTiles.length; t++) {
      final Tile expectedTile = expectedTiles[t];
      expectedTile.codestream = expectedCodestream;
      
      final Tile actualTile = actualTiles[t];

      final String tileIdentifier = "t[" + t + "]";

      inspectTile(expectedTile, actualTile, tileIdentifier);

      for (int tc = 0; tc < expectedCodestream.numComps; tc++) {
        final TileComponent expectedTileComp = expectedTile.accessTileComp(tc);
        final TileComponent actualTileComp = actualTile.accessTileComp(tc);

        final String tileCompIdentifier = tileIdentifier + " tc[" + tc + "]";

        inspectTileComponent(expectedTileComp, actualTileComp, tileCompIdentifier);

        final int numResolutions = expectedTileComp.numResolutions();
        for (int r = 0; r < numResolutions; r++) {
          final Resolution expectedResolution = expectedTileComp.accessResolution(r);
          final Resolution actualResolution = actualTileComp.accessResolution(r);

          final String resIdentifier = tileCompIdentifier + " r[" + r + "]";

          final int numPrecinctsTotal = expectedResolution.numPrecinctsTotal;

          for (int p = 0; p < numPrecinctsTotal; p++) {
            final String precIdentifier = resIdentifier + " p[" + p + "]";
            final Precinct expectedPrecinct = expectedResolution.accessPrecinct(p);
            final Precinct actualPrecinct = actualResolution.accessPrecinct(p);
            inspectPrecinct(expectedPrecinct, actualPrecinct, precIdentifier);
          }

          final SubbandType[] subbandTypes = expectedResolution.subbandTypes();
          for (SubbandType type : subbandTypes) {

            final Subband expectedSubband = expectedResolution.accessSubband(type);
            final Subband actualSubband = actualResolution.accessSubband(type);

            final String bandIdentifier = resIdentifier + " b[" + subbandArrayIndex(type) + "]";

            inspectSubband(expectedSubband, actualSubband, bandIdentifier);
            
            for (int bp = 0; bp < expectedResolution.numPrecinctsTotal; bp++) {
              final BandPrecinct expectedBandPrecinct = expectedSubband.accessBandPrecinct(bp);
              final BandPrecinct actualBandPrecinct = actualSubband.accessBandPrecinct(bp);

              final String bandPrecIdentifier = bandIdentifier + " bp[" + bp + "]";
              
              inspectBandPrecinct(expectedBandPrecinct, actualBandPrecinct, bandPrecIdentifier);

              for (int y = 0; y < expectedBandPrecinct.numBlocks.y; y++) {
                for (int x = 0; x < expectedBandPrecinct.numBlocks.x; x++) {
                  final Pair indices = new Pair(x, y);
                  final String cbIdentifier = bandPrecIdentifier + " cb" + indices;
                  final CodeBlock expectedBlock = expectedBandPrecinct.accessCodeBlock(indices);
                  final CodeBlock actualBlock = actualBandPrecinct.accessCodeBlock(indices);
                  inspectCodeBlock(expectedBlock, actualBlock, cbIdentifier);
                }
              }
            }
          }
          inspectResolution(expectedResolution, actualResolution, resIdentifier);
        }
      }
    }
  }

  private void inspectCodestream() {
    final Region expectedCanvas = expectedCodestream.region().absolute();
    final Region actualCanvas = actualCodestream.region().absolute();
    assertEquals("canvas", expectedCanvas, actualCanvas);
    assertEquals("tilePartition", expectedCodestream.tilePartition, actualCodestream.tilePartition);
    assertEquals("numTiles", expectedCodestream.numTiles, actualCodestream.numTiles);
    assertEquals("numComps", expectedCodestream.numComps, actualCodestream.numComps);
  }

  private void inspectComponents(Component[] actualComps, Component[] expectedComps) {
    assertNotNull("expected comps array existence", expectedComps);
    assertNotNull("actual comps array existence", actualComps);
    assertEquals("comps array length", expectedComps.length, actualComps.length);
  }

  private void inspectComponent(int c, Component expectedComp, Component actualComp) {
    assertEquals("c[" + c + "] idx", expectedComp.idx, actualComp.idx);
    assertEquals("c[" + c + "] subsampling", expectedComp.subsampling, actualComp.subsampling);
  }

  private void inspectTileArray(Tile[] expectedTiles, Tile[] actualTiles) {
    assertEquals("tiles array length", expectedTiles.length, actualTiles.length);
  }

  private void inspectTile(Tile expectedTile, Tile actualTile, String tileIdentifier) {
    assertEquals(tileIdentifier + " idx", expectedTile.idx, actualTile.idx);

    assertEquals(tileIdentifier + " region",
        expectedTile.region().absolute(),
        actualTile.region().absolute());
  }

  private void inspectTileComponent(TileComponent expectedTileComp, TileComponent actualTileComp,
      String tileCompIdentifier) {
    assertEquals(tileCompIdentifier + " region",
        expectedTileComp.region().absolute(),
        actualTileComp.region().absolute());

    assertEquals(tileCompIdentifier + " block size ",
        expectedTileComp.blockSize,
        actualTileComp.blockSize);

    assertEquals(tileCompIdentifier + " numResolutions",
        expectedTileComp.numResolutions(),
        actualTileComp.numResolutions());
  }

  private void inspectResolution(Resolution expectedResolution, Resolution actualResolution, String resIdentifier) {
    assertEquals(resIdentifier + " region",
        expectedResolution.region().absolute(),
        actualResolution.region().absolute());

    assertEquals(resIdentifier + " numBlocks",
        expectedResolution.numBlocks,
        actualResolution.numBlocks);

    assertEquals(resIdentifier + " numPrecinctsX",
        expectedResolution.numPrecinctsX,
        actualResolution.numPrecinctsX);

    assertEquals(resIdentifier + " numPrecinctsY",
        expectedResolution.numPrecinctsY,
        actualResolution.numPrecinctsY);

    assertEquals(resIdentifier + " numPrecinctsTotal",
        expectedResolution.numPrecinctsTotal,
        actualResolution.numPrecinctsTotal);

    assertEquals(resIdentifier + " subbandPartition",
        expectedResolution.subbandPartition,
        actualResolution.subbandPartition);

    assertEquals(resIdentifier + " precinctPartition",
        expectedResolution.precinctPartition,
        actualResolution.precinctPartition);

    assertArrayEquals(resIdentifier + " subbandTypes",
        expectedResolution.subbandTypes(),
        actualResolution.subbandTypes());
  }

  private void inspectPrecinct(Precinct expectedPrecinct, Precinct actualPrecinct, String precIdentifier) {
    assertEquals(precIdentifier + " idx", expectedPrecinct.idx, actualPrecinct.idx);
    assertEquals(precIdentifier + " x", expectedPrecinct.x, actualPrecinct.x);
    assertEquals(precIdentifier + " y", expectedPrecinct.y, actualPrecinct.y);
  }

  private void inspectSubband(Subband expectedSubband, Subband actualSubband, String bandIdentifier) {
    assertEquals(bandIdentifier + " idx", expectedSubband.idx, actualSubband.idx);
    assertEquals(bandIdentifier + " type", expectedSubband.type, actualSubband.type);

    assertEquals(bandIdentifier + " region",
        expectedSubband.region().absolute(),
        actualSubband.region().absolute());

    assertEquals(bandIdentifier + " blockPartition",
        expectedSubband.blockPartition,
        expectedSubband.blockPartition);
  }

  private void inspectBandPrecinct(BandPrecinct expectedBandPrecinct, BandPrecinct actualBandPrecinct,
      String bandPrecinctIdentifier) {

    assertEquals(bandPrecinctIdentifier + " region",
        expectedBandPrecinct.region().absolute(),
        actualBandPrecinct.region().absolute());

    assertEquals(bandPrecinctIdentifier + " numBlocks",
        expectedBandPrecinct.numBlocks,
        actualBandPrecinct.numBlocks);
  }

  private void inspectCodeBlock(CodeBlock expectedBlock, CodeBlock actualBlock, String cbIdentifier) {
    assertEquals(cbIdentifier + " indices", expectedBlock.indices, actualBlock.indices);

    assertEquals(cbIdentifier + " region",
        expectedBlock.region().absolute(),
        actualBlock.region().absolute());
  }

}
