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
package org.jadice.jpeg2000.internal.tier2;

import static org.jadice.jpeg2000.internal.Debug.packetHeaderProtocol;
import static org.jadice.jpeg2000.internal.image.Pair.p;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.Constants;
import org.jadice.jpeg2000.internal.CorruptBitstuffingException;
import org.jadice.jpeg2000.internal.Debug;
import org.jadice.jpeg2000.internal.codestream.BlockContribution;
import org.jadice.jpeg2000.internal.codestream.Codeword;
import org.jadice.jpeg2000.internal.codestream.PacketHeader;
import org.jadice.jpeg2000.internal.image.BandPrecinct;
import org.jadice.jpeg2000.internal.image.CodeBlock;
import org.jadice.jpeg2000.internal.image.Pair;
import org.jadice.jpeg2000.internal.image.Resolution;
import org.jadice.jpeg2000.internal.image.Subband;
import org.jadice.jpeg2000.internal.image.SubbandType;
import org.jadice.jpeg2000.internal.image.Tile;
import org.jadice.jpeg2000.internal.image.TileComponent;
import org.jadice.jpeg2000.internal.io.MarkerReader;
import org.jadice.jpeg2000.internal.io.PacketHeaderInput;
import org.jadice.jpeg2000.internal.marker.COx;
import org.jadice.jpeg2000.internal.marker.Marker;
import org.jadice.jpeg2000.internal.marker.MarkerSegment;
import org.jadice.jpeg2000.internal.param.Parameters;
import org.jadice.jpeg2000.msg.CodestreamMessages;
import org.jadice.jpeg2000.msg.CompressionMessages;
import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;

@Refer(to = Spec.J2K_CORE, page = 54, section = "B.10", called = "Packet Header Information Coding")
public class PacketHeaderReader {

  private static final QualifiedLogger LOGGER = LoggerFactory.getQualifiedLogger(PacketHeaderReader.class);

  private final Tile tile;

  public PacketHeaderReader(Tile tile) {
    this.tile = tile;
  }

  /**
   * @param source
   * @param packetHeader
   * @return <code>false</code> if the first packet header bit signals an empty packet;
   *         <code>true</code> if packet isn't empty and the packet header was parsed.
   * @throws IOException
   * @throws JPEG2000Exception
   */
  public boolean read(ImageInputStream source, PacketHeader packetHeader) throws IOException, JPEG2000Exception,
      CorruptBitstuffingException {

    /*
    if (Debug.PROTOCOL_PACKET_HEADER) {
      packetHeaderProtocol().packetHeaderStart(packetHeader);
    }
    */

    if (tile.useSOP) {
      final long savedStreamPosition = source.getStreamPosition();
      final MarkerReader markerReader = new MarkerReader();
      final Marker sopMarker = markerReader.next(source, false);
      if (sopMarker == Marker.SOP) {
        final MarkerSegment markerSegment = sopMarker.createMarkerSegment();
        markerSegment.read(source, false);
      } else {
        LOGGER.warn(CodestreamMessages.INCONSISTENT_USE_OF_SOP);
        source.seek(savedStreamPosition);
      }
    }

    /*
     * TODO
     * 
     * At this point the PPT and PPM marker segments should be taken into account if present. The
     * pointer into source should be retrieved from codestream (main header, PPM) or the current
     * active tile-part (PPT).
     */

    final long headerStartPosition = source.getStreamPosition();
    final PacketHeaderInput input = new PacketHeaderInput(source, headerStartPosition);

    if (input.readBit() == 0) {
      packetHeader.empty = true;

      if (Debug.PROTOCOL_PACKET_HEADER) {
        packetHeaderProtocol().empty(packetHeader.empty);
      }

      handleEmptyPacket(packetHeader, input);
    } else {
      packetHeader.empty = false;

      if (Debug.PROTOCOL_PACKET_HEADER) {
        packetHeaderProtocol().empty(packetHeader.empty);
      }

      parsePacketHeader(packetHeader, input);
    }

    if (tile.useEPH) {
      final MarkerReader markerReader = new MarkerReader();
      final Marker ephMarker = markerReader.next(source, false);
      if (ephMarker != Marker.EPH) {
        LOGGER.warn(CodestreamMessages.INCONSISTENT_USE_OF_EPH);
      }
    }

    return !packetHeader.empty;
  }

  protected void parsePacketHeader(PacketHeader packetHeader, PacketHeaderInput input) throws JPEG2000Exception,
      IOException, CorruptBitstuffingException {

    final TileComponent tileComp = tile.accessTileComp(packetHeader.comp);
    final Resolution resolution = tileComp.accessResolution(packetHeader.res);

    final int layer = packetHeader.layer;
    final boolean isLastLayer = layer == tileComp.layers - 1;

    final SubbandType[] subbandTypes = resolution.subbandTypes();
    for (SubbandType subbandType : subbandTypes) {
      final Subband subband = resolution.accessSubband(subbandType);

      if (Debug.LOG_PACKET_HEADER_READ) {
        final int r = resolution.resLevel;
        final SubbandType type = subband.type;
        LOGGER.info("Resolution " + r + ", Subband " + type + " packet header info decoding started.");
      }

      final BandPrecinct bandPrecinct = subband.accessBandPrecinct(packetHeader.precinct);
      for (int y = 0; y < bandPrecinct.numBlocks.y; y++) {
        for (int x = 0; x < bandPrecinct.numBlocks.x; x++) {
          final CodeBlock block = bandPrecinct.accessCodeBlock(p(x, y));

          if (Debug.PROTOCOL_PACKET_HEADER) {
            packetHeaderProtocol().codeBlockStart(block);
          }

          if (block.beta == 0) {
            if (Debug.PROTOCOL_PACKET_HEADER) {
              packetHeaderProtocol().beta(block.beta);

            }

            final TagTreeDecoder inclusionDecoder = bandPrecinct.inclusionDecoder;
            final int inclusion = inclusionDecoder.update(input, block.indices.x, block.indices.y, layer + 1);
            if (inclusion > layer) {

              if (Debug.PROTOCOL_PACKET_HEADER) {
                packetHeaderProtocol().notIncludedFirstTime();
              }

              // Add an empty block contribution to be able to push not included code-block
              packetHeader.blockContributions.add(emptyBlockContribution(block, layer, isLastLayer));

              // Continue with next code-block
              continue;
            }

          } else {
            if (input.readBit() == 0) {
              if (Debug.PROTOCOL_PACKET_HEADER) {
                packetHeaderProtocol().notIncluded();
              }

              // Add an empty block contribution to be able to push not included code-block
              packetHeader.blockContributions.add(emptyBlockContribution(block, layer, isLastLayer));

              // Continue with next code-block
              continue;
            }
          }

          if (Debug.PROTOCOL_PACKET_HEADER) {
            packetHeaderProtocol().included();
          }

          // At this point there will be at least one data segment contributing to the current
          // code-block. We create a
          // BlockContribution object containing all information and data for subsequent decoding
          // passes.
          final BlockContribution blockContribution = new BlockContribution(block);
          packetHeader.blockContributions.add(blockContribution);
          blockContribution.layer = layer;
          blockContribution.isLastLayer = isLastLayer;

          if (block.beta == 0) {
            if (Debug.PROTOCOL_PACKET_HEADER) {
              packetHeaderProtocol().includedFirstTime();
            }

            final TagTreeDecoder bitDepthDecoder = bandPrecinct.bitDepthDecoder;

            int missingMSBs = 1;
            for (int threshold = 1; missingMSBs >= threshold; threshold++) {
              missingMSBs = bitDepthDecoder.update(input, block.indices.x, block.indices.y, threshold);
            }

            block.missingMSBs = missingMSBs;
            block.bitplaneMax = Constants.INTEGER_COEFFICIENT_START - missingMSBs;
            block.bitplaneMin = block.bitplaneMax - subband.Kmax + missingMSBs;
            block.beta = 3;
          }

          // Read the amount of new passes according to ITU-T.800, B.10.6, Number of coding passes
          int newPasses = readNewPasses(input);

          // Set the amount of passes the blockContribution should be aware of
          blockContribution.passes = newPasses;

          // Record the next pass index for the next block segment
          int passIdx = block.numPassesTotal;

          // Record total amount of passes for code-block
          block.numPassesTotal += newPasses;

          if (Debug.PROTOCOL_PACKET_HEADER) {
            packetHeaderProtocol().newPasses(newPasses, block);
          }

          // Read the data length. See ITU-T.800, B.10.7, Length of the compressed image data from a
          // given code-block

          // 1st step: increment beta (working as the LBlock variable)
          while (input.readBit() == 1) {
            if (block.beta == 255) {
              throw new JPEG2000Exception(CompressionMessages.LBLOCK_OVERFLOW);
            }
            block.beta++;
          }

          if (Debug.PROTOCOL_PACKET_HEADER) {
            packetHeaderProtocol().lBlock(block.beta);
          }

          final boolean terminateAll = Parameters.isSet(tileComp.modes, COx.MASK_MODE_PASS_TERMINATION);
          final boolean bypass = !terminateAll && Parameters.isSet(tileComp.modes, COx.MASK_MODE_ARITHMETIC_BYPASS);
          final int bypassThresholdIdx = bypass ? 10 : 0;

          int segmentPasses;
          int segmentBytes;
          // Off by one?
          while (newPasses > 0) {
            if (terminateAll) {
              segmentPasses = 1;
            } else if (bypassThresholdIdx > 0) {

              if (passIdx < bypassThresholdIdx) {
                segmentPasses = bypassThresholdIdx - passIdx;
              } else if ((passIdx - bypassThresholdIdx) % 3 == 0) {
                segmentPasses = 2;
              } else {
                segmentPasses = 1;
              }

              if (segmentPasses > newPasses) {
                segmentPasses = newPasses;
              }
            } else {
              segmentPasses = newPasses;
            }

            int lengthBits = 0;
            while (1 << lengthBits <= segmentPasses) {
              lengthBits++;
            }

            lengthBits += block.beta - 1;

            segmentBytes = (int) (input.readBits(lengthBits) & 0xFFFFFFFF);

            if (Debug.PROTOCOL_PACKET_HEADER) {
              packetHeaderProtocol().segmentBytes(segmentBytes, block);
            }

            packetHeader.payloadLength += segmentBytes;

            final Codeword codeword = new Codeword();
            codeword.passIdx = passIdx;
            codeword.passes = segmentPasses;
            codeword.numBytes = segmentBytes;
            blockContribution.codewords.add(codeword);

            passIdx += segmentPasses;
            newPasses -= segmentPasses;
          }
        }
      }
    }
    input.finish();
  }

  private int readNewPasses(PacketHeaderInput input) throws IOException, CorruptBitstuffingException {
    int newPasses = 1 + input.readBit();
    if (newPasses >= 2) {
      newPasses += input.readBit();

      if (newPasses >= 3) {
        newPasses += input.readBits(2) & 0x3;

        if (newPasses >= 6) {
          newPasses += input.readBits(5) & 0x1F;

          if (newPasses >= 37) {
            newPasses += input.readBits(7) & 0x7F;
          }
        }
      }
    }
    return newPasses;
  }

  protected void handleEmptyPacket(PacketHeader packetHeader, PacketHeaderInput input) throws JPEG2000Exception,
      IOException, CorruptBitstuffingException {

    final TileComponent tileComp = tile.accessTileComp(packetHeader.comp);

    final boolean isLastLayer = tileComp.layers - 1 == packetHeader.layer;

    if (isLastLayer) {
      final Resolution resolution = tileComp.accessResolution(packetHeader.res);
      final SubbandType[] subbandTypes = resolution.subbandTypes();
      for (SubbandType type : subbandTypes) {
        final Subband subband = resolution.accessSubband(type);

         final BandPrecinct bandPrecinct = subband.accessBandPrecinct(packetHeader.precinct);

        for (int y = 0; y < bandPrecinct.numBlocks.y; y++) {
          for (int x = 0; x < bandPrecinct.numBlocks.x; x++) {
            final CodeBlock block = bandPrecinct.accessCodeBlock(new Pair(x, y));

            if (Debug.PROTOCOL_PACKET_HEADER) {
              packetHeaderProtocol().codeBlockStart(block);
            }
            
            packetHeader.blockContributions.add(emptyBlockContribution(block, packetHeader.layer, isLastLayer));

            /*
            if (Debug.PROTOCOL_PACKET_HEADER) {
              packetHeaderProtocol().includedEmpty();
            }
            */
          }
        }
      }
    }

    input.finish();
  }

  private BlockContribution emptyBlockContribution(CodeBlock block, int currentLayer, boolean isLastLayer) {
    final BlockContribution emptyContribution = new BlockContribution(block);
    emptyContribution.layer = currentLayer;
    emptyContribution.passes = 0;
    emptyContribution.isLastLayer = isLastLayer;
    return emptyContribution;
  }

}
