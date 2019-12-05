package com.levigo.jadice.format.jpeg2000.internal.debug.tier1;

import com.levigo.jadice.format.jpeg2000.internal.debug.Protocol;

public interface BlockCodingProtocol extends Protocol {
  void run(int symbol);
  void runlength(int symbol);
  void contextWord(int ctxWord);
  void significance(boolean symbol);
  void sign(int symbol);
  void sample(int sample);
  void segmentationMark(int symbol);
  void refinement(int symbol);
}
