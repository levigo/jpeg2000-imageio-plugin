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
package org.jadice.jpeg2000.internal.decode.seda;

import java.io.IOException;
import java.util.List;

import javax.imageio.stream.ImageInputStream;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.CorruptBitstuffingException;
import org.jadice.jpeg2000.internal.codestream.BlockContribution;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.codestream.Codeword;
import org.jadice.jpeg2000.internal.codestream.PacketHeader;
import org.jadice.jpeg2000.internal.codestream.TilePartPointerProvider;
import org.jadice.jpeg2000.internal.image.Canvas;
import org.jadice.jpeg2000.internal.image.CodeBlock;
import org.jadice.jpeg2000.internal.image.Pair;
import org.jadice.jpeg2000.internal.image.Region;
import org.jadice.jpeg2000.internal.image.Regions;
import org.jadice.jpeg2000.internal.image.Tile;
import org.jadice.jpeg2000.internal.io.MarkerReader;
import org.jadice.jpeg2000.internal.io.SectorInputStream;
import org.jadice.jpeg2000.internal.marker.Marker;
import org.jadice.jpeg2000.internal.marker.SOT;
import org.jadice.jpeg2000.internal.tier2.PacketHeaderReader;
import org.jadice.jpeg2000.internal.tier2.PacketSequencer;
import org.jadice.jpeg2000.msg.CodestreamMessages;
import org.jadice.jpeg2000.msg.CompressionMessages;
import org.jadice.jpeg2000.msg.GeneralMessages;
import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

// part of an experiment. Currently not in use.
public class ReadCodestreamStage extends ConfigurableStage
    implements
      Transformer<Codestream, BlockContribution, JPEG2000Exception> {

  private long readTilePart(Codestream codestream, long tilePartStart,
      Consumer<? super BlockContribution, ? extends JPEG2000Exception> next) throws IOException, JPEG2000Exception {
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

      readPackets(codestream, tile, tilePartEnd, next);
    }

    return tilePartEnd;
  }

  private void readPackets(Codestream codestream, Tile tile, long tilePartEnd,
      Consumer<? super BlockContribution, ? extends JPEG2000Exception> next) throws IOException, JPEG2000Exception {
    final ImageInputStream source = codestream.source;
    final PacketHeaderReader packetHeaderReader = new PacketHeaderReader(tile);
    final PacketSequencer packetSequencer = tile.packetSequencer;
    while (packetSequencer.hasNext() && source.getStreamPosition() < tilePartEnd) {
      final PacketHeader packetHeader = packetSequencer.next();
      try {
        packetHeaderReader.read(source, packetHeader);
      } catch (final CorruptBitstuffingException e) {
        final QualifiedLogger log = LoggerFactory.getQualifiedLogger(this.getClass());
        log.error(CompressionMessages.CORRUPT_BITSTUFFING, e);
        // TODO resync
      }

      final List<BlockContribution> blockContributions = packetHeader.blockContributions;
      for (BlockContribution contribution : blockContributions) {
        final CodeBlock block = contribution.block;
        if (block.restart) {
          push(contribution, source, next);
        } else if (contribution.isLastLayer) {
          mergeAndPush(contribution, source, next);
        } else {
          merge(contribution, source);
        }
      }
    }
  }

  private void push(BlockContribution contribution, ImageInputStream source,
      Consumer<? super BlockContribution, ? extends JPEG2000Exception> next)
      throws IOException, JPEG2000Exception {
    for (Codeword segment : contribution.codewords) {
      segment.input = new SectorInputStream(source, source.getStreamPosition(), segment.numBytes);
      source.skipBytes(segment.numBytes);
    }

    next.consume(contribution);
  }

  private void mergeAndPush(BlockContribution contribution, ImageInputStream source,
      Consumer<? super BlockContribution, ? extends JPEG2000Exception> next)
      throws JPEG2000Exception, IOException {
    merge(contribution, source);

    final CodeBlock block = contribution.block;
    final Codeword codeword = block.codeword;

    final BlockContribution blockContribution = new BlockContribution(block);
    blockContribution.isLastLayer = true;
    blockContribution.passes = codeword.passes;
    blockContribution.codewords.add(codeword);

    block.codeword = null;

    next.consume(blockContribution);
  }

  private void merge(BlockContribution contribution, ImageInputStream source) throws IOException {
    final CodeBlock block = contribution.block;

    if (block.codeword == null) {
      block.codeword = new Codeword();
    }

    for (Codeword segment : contribution.codewords) {
      segment.input = new SectorInputStream(source, source.getStreamPosition(), segment.numBytes);
      block.codeword.merge(segment);
      source.skipBytes(segment.numBytes);
    }
  }

  @Override
  public void transform(Codestream codestream, Consumer<? super BlockContribution, ? extends JPEG2000Exception> next)
      throws JPEG2000Exception {
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

          nextTilePartPosition = readTilePart(codestream, nextTilePartPosition, next);

          if (source.getStreamPosition() != nextTilePartPosition) {
            source.seek(nextTilePartPosition);
          }
          marker = markerReader.next(source);
        }

        source.seek(savedPosition);
      } catch (final IOException e) {
        throw new JPEG2000Exception(GeneralMessages.IO_ERROR, e);
      } catch (final Exception e) {
        throw new JPEG2000Exception(GeneralMessages.WRAPPED_EXCEPTION, e);
      }
    }
  }
}
