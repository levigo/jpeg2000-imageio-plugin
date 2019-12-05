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
import org.jadice.jpeg2000.internal.codestream.TilePartPointer;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.Receiver;
import org.jadice.jpeg2000.internal.image.Tile;
import org.jadice.jpeg2000.internal.tier2.PacketHeaderReader;
import org.jadice.jpeg2000.internal.tier2.PacketSequencer;
import org.jadice.jpeg2000.msg.CompressionMessages;
import org.jadice.jpeg2000.msg.GeneralMessages;
import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;

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
