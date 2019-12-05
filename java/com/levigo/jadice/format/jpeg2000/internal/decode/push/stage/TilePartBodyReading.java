package com.levigo.jadice.format.jpeg2000.internal.decode.push.stage;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.CorruptBitstuffingException;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;
import com.levigo.jadice.format.jpeg2000.internal.codestream.TilePartPointer;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.tier2.PacketHeaderReader;
import com.levigo.jadice.format.jpeg2000.internal.tier2.PacketSequencer;
import com.levigo.jadice.format.jpeg2000.msg.CompressionMessages;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;

@Refer(to = Spec.J2K_CORE, page = 52, section = "B.9", called = "Packets")
public class TilePartBodyReading implements Receiver<TilePartPointer> {
  private final Receiver<PacketHeader> nextStage;

  public TilePartBodyReading(Receiver<PacketHeader> nextStage) {
    this.nextStage = nextStage;
  }

  @Override
  public void receive(TilePartPointer tilePartPointer, DecoderParameters parameters) throws JPEG2000Exception {
    final Tile tile = tilePartPointer.tile;
    final Codestream codestream = tile.codestream;

    final long tilePartEnd = tilePartPointer.tilePartStart + tilePartPointer.tilePartLength;

    final PacketHeaderReader packetHeaderReader = new PacketHeaderReader(tile);
    final ImageInputStream source = codestream.source;

    final PacketSequencer packetSequencer = tile.packetSequencer;

    try {
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
    } catch (IOException e) {
      throw new JPEG2000Exception(GeneralMessages.IO_ERROR, e);
    }
  }
}
