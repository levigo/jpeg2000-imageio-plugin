package com.levigo.jadice.format.jpeg2000.internal.debug;

import java.util.Collection;

public interface Protocol {
  
  int DEFAULT_TOKEN_LENGTH = 4;
  
  byte tokenLength();
  
  Collection<ProtocolToken> knownTokens();

  void addProtocolListener(ProtocolListener listener);
  
  void finish();
}
