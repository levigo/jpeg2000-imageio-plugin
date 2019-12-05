package com.levigo.jadice.format.jpeg2000.internal.tier2;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;

@Refer(to = Spec.J2K_CORE, page = 59, section = "B.12", called = "Progression Order")
public interface PacketSequencer {
  void append(PacketHeader packetHeader);

  boolean hasNext();

  PacketHeader next();

  void reset();
}
