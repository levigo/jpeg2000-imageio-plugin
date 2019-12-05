package com.levigo.jadice.format.jpeg2000.internal.debug.tcq;

import com.levigo.jadice.format.jpeg2000.internal.debug.Protocol;

public interface QuantizationProtocol extends Protocol {
  void downshift(int downshift);
  void valueBefore(int value);
  void valueAfter(int value);
  void fillWithZeros();
}
