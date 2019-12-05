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

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.CorruptBitstuffingException;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.codestream.PacketHeader;
import org.jadice.jpeg2000.internal.codestream.TilePartPointerProvider;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.Receiver;
import org.jadice.jpeg2000.internal.image.Canvas;
import org.jadice.jpeg2000.internal.image.Pair;
import org.jadice.jpeg2000.internal.image.Region;
import org.jadice.jpeg2000.internal.image.Regions;
import org.jadice.jpeg2000.internal.image.Tile;
import org.jadice.jpeg2000.internal.io.MarkerReader;
import org.jadice.jpeg2000.internal.marker.Marker;
import org.jadice.jpeg2000.internal.marker.SOT;
import org.jadice.jpeg2000.internal.tier2.PacketHeaderReader;
import org.jadice.jpeg2000.internal.tier2.PacketSequencer;
import org.jadice.jpeg2000.msg.CodestreamMessages;
import org.jadice.jpeg2000.msg.CompressionMessages;
import org.jadice.jpeg2000.msg.GeneralMessages;
import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

public class ForwardReading implements Receiver<Codestream> {

  private final Receiver<PacketHeader> nextStage;

  public ForwardReading(Receiver<PacketHeader> nextStage) {
    this.nextStage = nextStage;
  }

  @Override
  public void receive(Codestream codestream, DecoderParameters parameters) throws JPEG2000Exception {
    final ImageInputStream source = codestream.source;
    synchronized (source) {
      try {
        final long savedPosition = source.getStreamPosition();
        long nextTilePartPosition = codestream.firstTilePartAddress;
        source.seek(nextTilePartPosition);

        final MarkerReader markerReader = new MarkerReader();
        Marker marker = markerReader.next(source);

        while (marker != Marker.EOC) {
          if (marker != Marker.SOT) {
            throw new JPEG2000Exception(CodestreamMessages.EXPECTED_SOT_MARKER, marker);
          }

          nextTilePartPosition = readTilePart(codestream, parameters, nextTilePartPosition);

          if (source.getStreamPosition() != nextTilePartPosition) {
            source.seek(nextTilePartPosition);
          }
          marker = markerReader.next(source);
        }

        source.seek(savedPosition);
      } catch (IOException e) {
        throw new JPEG2000Exception(GeneralMessages.IO_ERROR, e);
      }
    }
  }

  private long readTilePart(Codestream codestream, DecoderParameters parameters, long tilePartStart)
      throws IOException, JPEG2000Exception {
    final SOT sot = new SOT();
    sot.read(codestream.source, codestream, parameters.validate);
    
    final TilePartPointerProvider pointerProvider = codestream.accessTilePartPointerProvider();
    pointerProvider.registerSOT(sot, tilePartStart);

    final long tilePartEnd = tilePartStart + sot.Psot;

    final Pair numTiles = codestream.numTiles;
    final Pair tileIndices = Canvas.tileIndices(sot.Isot, numTiles.x);

    final Region canvas = codestream.region().absolute();
    final Region tileRegion = Canvas.tileRegion(canvas, codestream.tilePartition, tileIndices.x, tileIndices.y);

    if (Regions.intersect(tileRegion, parameters.region)) {
      final Tile tile = codestream.accessTile(tileIndices);

      readPackets(codestream, parameters, tile, tilePartEnd);
    }

    return tilePartEnd;
  }

  private void readPackets(Codestream codestream, DecoderParameters parameters, Tile tile, long tilePartEnd)
      throws IOException, JPEG2000Exception {
    final PacketHeaderReader packetHeaderReader = new PacketHeaderReader(tile);
    final ImageInputStream source = codestream.source;
    
    final PacketSequencer packetSequencer = tile.packetSequencer;
    
    while (packetSequencer.hasNext() && source.getStreamPosition() < tilePartEnd) {
      final PacketHeader packetHeader = packetSequencer.next();
      packetHeader.source = source;
      try {
        packetHeaderReader.read(source, packetHeader);
      } catch (CorruptBitstuffingException e) {
        final QualifiedLogger log = LoggerFactory.getQualifiedLogger(this.getClass());
        log.error(CompressionMessages.CORRUPT_BITSTUFFING, e);
        // TODO resync
      }
      nextStage.receive(packetHeader, parameters);
    }
  }
}
