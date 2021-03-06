package com.levigo.jadice.format.jpeg2000.internal.decode.push.stage;

import java.io.IOException;
import java.util.List;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.BlockContribution;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codeword;
import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.io.SectorInputStream;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;

public class PacketBodyReading implements Receiver<PacketHeader> {

  private final Receiver<BlockContribution> nextStage;

  public PacketBodyReading(Receiver<BlockContribution> receiver) {
    nextStage = receiver;
  }

  @Override
  public void receive(PacketHeader packetHeader, DecoderParameters parameters) throws JPEG2000Exception {
    final ImageInputStream source = packetHeader.source;
    try {
      final List<BlockContribution> blockContributions = packetHeader.blockContributions;
      for (BlockContribution contribution : blockContributions) {
        final CodeBlock block = contribution.block;
        final long streamPosition;
        if (block.restart) {
          streamPosition = push(contribution, source, parameters);
        } else if (contribution.isLastLayer) {
          streamPosition = mergeAndPush(contribution, source, parameters);
        } else {
          streamPosition = merge(contribution, source, parameters);
        }
        source.seek(streamPosition);
      }
    } catch (IOException e) {
      throw new JPEG2000Exception(GeneralMessages.IO_ERROR, e);
    }
  }

  private long push(BlockContribution contribution, ImageInputStream source, DecoderParameters parameters)
      throws IOException, JPEG2000Exception {
    for (Codeword segment : contribution.codewords) {
      segment.input = new SectorInputStream(source, source.getStreamPosition(), segment.numBytes);
      source.skipBytes(segment.numBytes);
    }

    final long streamPosition = source.getStreamPosition();
    
    nextStage.receive(contribution, parameters);
    
    return streamPosition;
  }

  private long mergeAndPush(BlockContribution contribution, ImageInputStream source, DecoderParameters parameters)
      throws JPEG2000Exception, IOException {
    final long streamPosition = merge(contribution, source, parameters);

    final CodeBlock block = contribution.block;
    final Codeword codeword = block.codeword;
    
    final BlockContribution blockContribution = new BlockContribution(block);
    blockContribution.isLastLayer = true;
    blockContribution.passes = codeword.passes;
    blockContribution.codewords.add(codeword);

    block.codeword = null;
    
    nextStage.receive(blockContribution, parameters);

    return streamPosition;
  }

  private long merge(BlockContribution contribution, ImageInputStream source, DecoderParameters parameters)
      throws IOException {
    final CodeBlock block = contribution.block;

    if (block.codeword == null) {
      block.codeword = new Codeword();
    }

    for (Codeword segment : contribution.codewords) {
      segment.input = new SectorInputStream(source, source.getStreamPosition(), segment.numBytes);
      block.codeword.merge(segment);
      source.skipBytes(segment.numBytes);
    }

    return source.getStreamPosition();
  }
}
