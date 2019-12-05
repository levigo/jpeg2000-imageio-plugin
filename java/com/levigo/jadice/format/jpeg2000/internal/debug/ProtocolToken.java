package com.levigo.jadice.format.jpeg2000.internal.debug;

public interface ProtocolToken extends Parameter {
  
  boolean matches(Object o);
  
  ParameterFactory getParameterFactory();
  
}
