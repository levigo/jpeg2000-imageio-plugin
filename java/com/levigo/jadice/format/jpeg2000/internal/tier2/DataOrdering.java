package com.levigo.jadice.format.jpeg2000.internal.tier2;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.CorruptBitstuffingException;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.codestream.BlockContribution;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codeword;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;
import com.levigo.jadice.format.jpeg2000.internal.codestream.TilePartPointer;
import com.levigo.jadice.format.jpeg2000.internal.codestream.TilePartPointerProvider;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;
import com.levigo.jadice.format.jpeg2000.msg.CompressionMessages;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;
import org.jadice.util.log.qualified.QualifiedLogger;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

public class DataOrdering {

  private static final QualifiedLogger LOGGER = getQualifiedLogger(DataOrdering.class);

  private PacketSequencer packetSequencer;
  private PacketHeaderReader packetHeaderReader;

  private boolean lastPacketFound;
  private long nextBitStreamPos;
  private TilePartPointer currentTilePartPointer;
  private Iterator<TilePartPointer> tilePartPointerIterator;
  private List<TilePartPointer> tilePartPointers;

  private final Tile tile;
  private final ImageInputStream source;
  private long tilePartEndPos;

  public DataOrdering(Codestream codestream, Tile tile) throws JPEG2000Exception {
    this.tile = tile;

    source = codestream.source;
    packetHeaderReader = new PacketHeaderReader(tile);

    final TilePartPointerProvider tilePartPointerProvider = codestream.accessTilePartPointerProvider();
    tilePartPointers = tilePartPointerProvider.getTilePartPointers(tile.idx);

    if (tilePartPointers == null || tilePartPointers.isEmpty()) {
      throw new JPEG2000Exception(CodestreamMessages.MISSING_TILE_PARTS);
    }

    packetSequencer = tile.packetSequencer;

    restart();
  }

  public void restart() throws JPEG2000Exception {
    // TODO packet sequencer restart!!

    tilePartPointerIterator = tilePartPointers.iterator();
    currentTilePartPointer = null;
    lastPacketFound = true;
  }

  public PacketHeader nextPacket() throws JPEG2000Exception, IOException {
    if (!packetSequencer.hasNext()) {
      return null;
    }

    if (lastPacketFound || currentTilePartPointer == null) {
      if (!tilePartPointerIterator.hasNext()) {
        return null;
      }

      currentTilePartPointer = tilePartPointerIterator.next();
      try {
        tile.readTilePartHeader(currentTilePartPointer);
        nextBitStreamPos = currentTilePartPointer.tilePartDataStart;
        tilePartEndPos = currentTilePartPointer.tilePartStart + currentTilePartPointer.tilePartLength;
        lastPacketFound = false;
      } catch (IOException e) {
        throw new JPEG2000Exception(GeneralMessages.IO_ERROR, e);
      }
    }

    final PacketHeader packetHeader = packetSequencer.next();

    synchronized (source) {
      source.seek(nextBitStreamPos);

      try {
        packetHeaderReader.read(source, packetHeader);
      } catch (CorruptBitstuffingException e) {
        LOGGER.error(CompressionMessages.CORRUPT_BITSTUFFING, e);
        // TODO resync
      }

      final long packetHeaderLength = source.getStreamPosition() - nextBitStreamPos;

      if (Debug.LOG_PACKET_HEADER_READ) {
        LOGGER.info("Packet header length: " + packetHeaderLength);
      }

      final List<BlockContribution> blockContributions = packetHeader.blockContributions;
      for (BlockContribution blockContribution : blockContributions) {
        for (Codeword segment : blockContribution.codewords) {
          // FIXME Disabled. See PacketBodyReadingStage.
          // segment.bytes = new byte[segment.numBytes];
          // source.readFully(segment.bytes);
        }
      }

      nextBitStreamPos += packetHeaderLength + packetHeader.payloadLength;
      if (source.getStreamPosition() != nextBitStreamPos) {
        throw new JPEG2000Exception(CompressionMessages.INCONSISTENT_SEGMENT_LENGTHS);
      }

      if (nextBitStreamPos >= tilePartEndPos) {
        lastPacketFound = true;
      }
    }

    return packetHeader;
  }
}
