package com.levigo.jadice.format.jpeg2000.internal.debug;

public interface ProtocolElement {
  
  ProtocolToken token();
  
  Parameter parameter();

  boolean matches(Object o);
  
}
