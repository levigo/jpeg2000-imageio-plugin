package com.levigo.jadice.format.jpeg2000.internal.debug;

public class PrintlineProtocolListener implements ProtocolListener {

  private long numElements;

  public PrintlineProtocolListener() {
    numElements = 0;
  }

  @Override
  public void newProtocolElement(ProtocolElement protocolElement) {
    System.err.println("[" + numElements + "]: " + protocolElement);
    numElements++;
  }
}
