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
package org.jadice.jpeg2000.internal.decode.push.stage;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.buffer.Buffers;
import org.jadice.jpeg2000.internal.buffer.DummyDataBuffer;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.Receiver;
import org.jadice.jpeg2000.internal.image.DecodedRaster;
import org.jadice.jpeg2000.internal.image.GridRegion;
import org.jadice.jpeg2000.internal.image.Pair;
import org.jadice.jpeg2000.internal.image.Region;
import org.jadice.jpeg2000.internal.image.Regions;
import org.jadice.jpeg2000.internal.image.Tile;
import org.jadice.jpeg2000.internal.image.TileComponent;
import org.jadice.jpeg2000.msg.GeneralMessages;

public class TileComposition implements Receiver<Tile> {
  private final Receiver<DecodedRaster> nextStage;

  public TileComposition(Receiver<DecodedRaster> nextStage) {
    this.nextStage = nextStage;
  }

  @Override
  public void receive(Tile tile, DecoderParameters parameters) throws JPEG2000Exception {
    // FIXME: who, exactly is responsible for the state initialization?
    final Codestream codestream = tile.codestream;

    if (null == codestream.state) {
      codestream.start(parameters);
    }

    if (codestream.state.remainingTiles.decrementAndGet() == 0) {
      // TODO implement short-cuts for directly creatable rasters

      final WritableRaster raster = composeRaster(codestream, parameters);
      codestream.free();
      nextStage.receive(new DecodedRaster(raster, codestream), parameters);
    }
  }

  protected WritableRaster composeRaster(final Codestream codestream, DecoderParameters parameters)
      throws JPEG2000Exception {

    final Region requestedRegion = getRequestedRegion(codestream, parameters);

    final WritableRaster raster = createWritableRaster(codestream, requestedRegion);

    if (requestedRegion.equals(codestream.region().absolute())) {
      composeFull(raster, codestream);
    } else {
      composeRegion(raster, codestream, requestedRegion);
    }

    return raster;
  }

  private void composeFull(WritableRaster raster, Codestream codestream) throws JPEG2000Exception {
    for (int tileIndex = 0; tileIndex < codestream.tiles.length; tileIndex++) {
      final Tile tile = codestream.tiles[tileIndex];
      for (int compIdx = 0; compIdx < codestream.numComps; compIdx++) {
        final TileComponent tileComp = tile.accessTileComp(compIdx);
        composeTileComp(tileComp, raster, null);
        tileComp.free();
      }
    }
  }

  private void composeRegion(WritableRaster raster, Codestream codestream, Region roi) throws JPEG2000Exception {
    final Pair firstTileIdx = Regions.getFirstChildIdx(roi, codestream.tilePartition);
    final Pair lastTileIdx = Regions.getLastChildIdx(roi, codestream.tilePartition);

    for (int tileY = firstTileIdx.y; tileY <= lastTileIdx.y; tileY++) {
      for (int tileX = firstTileIdx.x; tileX <= lastTileIdx.x; tileX++) {

        final Tile tile = getTile(codestream, tileY, tileX);

        if (tile != null) {
          for (int compIdx = 0; compIdx < codestream.numComps; compIdx++) {
            final TileComponent tileComp = tile.accessTileComp(compIdx);
            composeTileComp(tileComp, raster, roi);
            tileComp.free();
          }
        }
      }
    }
  }

  private void composeTileComp(TileComponent tileComp, WritableRaster raster, Region requestedRegion) {
    final GridRegion gridRegion = tileComp.region();
    final Region region = gridRegion.absolute();
    final DummyDataBuffer sb = tileComp.state.sampleBuffer;

    if (requestedRegion == null || requestedRegion.covers(region)) {
      composeTileCompFull(tileComp, raster, region, sb);
    } else {
      composeTileCompRegion(raster, requestedRegion, region, tileComp.comp.idx, sb);
    }
  }

  private void composeTileCompFull(TileComponent tileComp, WritableRaster raster, Region region, DummyDataBuffer sb) {
    if (sb.floatSamples != null) {
      raster.setSamples(region.pos.x, region.pos.y, region.size.x, region.size.y, tileComp.comp.idx, sb.floatSamples);
    } else if (sb.intSamples != null) {
      raster.setSamples(region.pos.x, region.pos.y, region.size.x, region.size.y, tileComp.comp.idx, sb.intSamples);
    }
  }

  private void composeTileCompRegion(WritableRaster raster, Region requestedRegion, Region tileCompRegion, int compIdx,
      DummyDataBuffer sb) {
    final int dstX = Math.max(tileCompRegion.pos.x, requestedRegion.pos.x);
    final int dstY = Math.max(tileCompRegion.pos.y, requestedRegion.pos.y);

    final int srcX = dstX - tileCompRegion.pos.x;
    final int srcY = dstY - tileCompRegion.pos.y;

    final int dstW = Math.min(tileCompRegion.x1(), requestedRegion.x1()) - dstX;
    final int dstH = Math.min(tileCompRegion.y1(), requestedRegion.y1()) - dstY;

    final int scanline = tileCompRegion.width();
    if (sb.floatSamples != null) {
      final float[] croppedSamples = Buffers.crop(sb.floatSamples, srcX, srcY, dstW, dstH, scanline);
      raster.setSamples(dstX, dstY, dstW, dstH, compIdx, croppedSamples);
    } else if (sb.intSamples != null) {
      final int[] croppedSamples = Buffers.crop(sb.intSamples, srcX, srcY, dstW, dstH, scanline);
      raster.setSamples(dstX, dstY, dstW, dstH, compIdx, croppedSamples);
    }
  }

  private WritableRaster createWritableRaster(Codestream codestream, Region requestedRegion) {
    return Raster.createInterleavedRaster( //
        DataBuffer.TYPE_BYTE, //
        requestedRegion.size.x, // 
        requestedRegion.size.y, // 
        codestream.numComps, //
        requestedRegion.pos.toPoint());
  }

  private Region getRequestedRegion(Codestream codestream, DecoderParameters parameters) {
    if (parameters.region != null) {
      return parameters.region;
    }

    return codestream.region().absolute();
  }

  private Tile getTile(Codestream codestream, int tileY, int tileX) throws JPEG2000Exception {
    try {
      return codestream.accessTile(new Pair(tileX, tileY));
    } catch (IOException e) {
      throw new JPEG2000Exception(GeneralMessages.IO_ERROR, e);
    }
  }

}
