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
package com.levigo.jadice.format.jpeg2000.internal.debug.tier2;

import static com.levigo.jadice.format.jpeg2000.internal.debug.BooleanParameter.bool;
import static com.levigo.jadice.format.jpeg2000.internal.debug.ByteParameter.byteParam;
import static com.levigo.jadice.format.jpeg2000.internal.debug.IntegerParameter.integer;
import static com.levigo.jadice.format.jpeg2000.internal.debug.NullParameter.noParam;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderParameterFactory.packetHeader;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.BetaChange;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.CodeBlockStart;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.Empty;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.Finish;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.Included;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.IncludedEmpty;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.IncludedFirstTime;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.LBlock;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.NewPasses;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.NotIncluded;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.NotIncludedFirstTime;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.PacketHeaderStart;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderToken.SegmentBytes;
import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

import java.util.ArrayList;
import java.util.Collection;

import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;
import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolBase;
import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolToken;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import org.jadice.util.log.qualified.QualifiedLogger;

public class BasicPacketHeaderProtocol extends ProtocolBase implements PacketHeaderProtocol {

  private static final QualifiedLogger LOGGER = getQualifiedLogger(BasicPacketHeaderProtocol.class);

  private static final Collection<ProtocolToken> PACKET_HEADER_TOKENS;

  static {
    PACKET_HEADER_TOKENS = new ArrayList<ProtocolToken>();
    for (PacketHeaderToken packetHeaderToken : PacketHeaderToken.values()) {
      PACKET_HEADER_TOKENS.add(packetHeaderToken);
    }
  }

  public BasicPacketHeaderProtocol() {
    super(PACKET_HEADER_TOKENS);
  }

  @Override
  public void finish() {
    createAndNotify(Finish, integer(getNumElements()));
  }

  @Override
  public void empty(boolean empty) {
    if (Debug.LOG_PACKET_HEADER_READ) {
      LOGGER.info("Packet " + (empty ? "is" : "is not") + " empty");
    }
    createAndNotify(Empty, bool(empty));
  }

  @Override
  public void packetHeaderStart(PacketHeader header) {
    if (Debug.LOG_PACKET_HEADER_READ) {
      LOGGER.info("Start reading " + header);
    }
    createAndNotify(PacketHeaderStart, packetHeader(header.comp, header.precinct, header.res, header.layer));
  }

  @Override
  public void codeBlockStart(CodeBlock block) {
    if (Debug.LOG_PACKET_HEADER_READ) {
      LOGGER.info(canonical(block) + "start");
    }
    createAndNotify(CodeBlockStart, noParam());
    // createAndNotify(CodeBlockStart, codeBlockParam(block));
  }

  @Override
  public void beta(byte beta) {
    createAndNotify(BetaChange, byteParam(beta));
  }

  @Override
  public void includedFirstTime() {
    createAndNotify(IncludedFirstTime, noParam());
  }

  @Override
  public void notIncludedFirstTime() {
    createAndNotify(NotIncludedFirstTime, noParam());
  }

  @Override
  public void included() {
    createAndNotify(Included, noParam());
  }

  @Override
  public void notIncluded() {
    createAndNotify(NotIncluded, noParam());
  }

  @Override
  public void newPasses(int newPasses, CodeBlock block) {
    if (Debug.LOG_PACKET_HEADER_READ) {
      LOGGER.info(canonical(block) + "with " + newPasses + " new passes");
    }
    createAndNotify(NewPasses, integer(newPasses));
  }

  @Override
  public void lBlock(byte beta) {
    createAndNotify(LBlock, byteParam(beta));
  }

  @Override
  public void segmentBytes(int segmentBytes, CodeBlock block) {
    if (Debug.LOG_PACKET_HEADER_READ) {
      LOGGER.info(canonical(block) + "compressed data length in segment: " + segmentBytes);
    }
    createAndNotify(SegmentBytes, integer(segmentBytes));
  }

  @Override
  public void includedEmpty() {
    createAndNotify(IncludedEmpty, noParam());
  }

  private static String canonical(CodeBlock block) {
    return "Codeblock (" + block.indices.x + "," + block.indices.y + ") ";
  }

}
