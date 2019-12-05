package com.levigo.jadice.format.jpeg2000.internal.debug.tier2;

import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;
import com.levigo.jadice.format.jpeg2000.internal.debug.Protocol;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;

public interface PacketHeaderProtocol extends Protocol {
  void empty(boolean empty);
  void packetHeaderStart(PacketHeader packetHeader);
  void codeBlockStart(CodeBlock block);
  void beta(byte beta);
  void notIncludedFirstTime();
  void notIncluded();
  void included();
  void includedFirstTime();
  void newPasses(int newPasses, CodeBlock block);
  void lBlock(byte beta);
  void segmentBytes(int segmentBytes, CodeBlock block);
  void includedEmpty();
}
