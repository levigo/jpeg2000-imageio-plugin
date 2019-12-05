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
import java.util.List;

import javax.imageio.stream.ImageInputStream;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.codestream.BlockContribution;
import org.jadice.jpeg2000.internal.codestream.Codeword;
import org.jadice.jpeg2000.internal.codestream.PacketHeader;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.Receiver;
import org.jadice.jpeg2000.internal.image.CodeBlock;
import org.jadice.jpeg2000.internal.io.SectorInputStream;
import org.jadice.jpeg2000.msg.GeneralMessages;

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
