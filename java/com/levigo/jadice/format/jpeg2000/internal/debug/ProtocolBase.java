package com.levigo.jadice.format.jpeg2000.internal.debug;

import static com.levigo.jadice.format.jpeg2000.internal.debug.BasicProtocolElement.newElement;

import java.util.ArrayList;
import java.util.Collection;

public abstract class ProtocolBase implements Protocol {

  private final Collection<ProtocolToken> knownTokens;

  private final Collection<ProtocolListener> listeners;

  private int numElements;

  protected ProtocolBase(Collection<ProtocolToken> knownTokens) {
    this.knownTokens = knownTokens;

    listeners = new ArrayList<ProtocolListener>(3);
    numElements = 0;
  }

  @Override
  public byte tokenLength() {
    return DEFAULT_TOKEN_LENGTH;
  }

  @Override
  public Collection<ProtocolToken> knownTokens() {
    return knownTokens;
  }

  @Override
  public void addProtocolListener(ProtocolListener listener) {
    listeners.add(listener);
  }

  protected void createAndNotify(ProtocolToken token, Parameter parameter) {
    final BasicProtocolElement protocolElement = newElement(token, parameter);
    for (ProtocolListener listener : listeners) {
      listener.newProtocolElement(protocolElement);
    }
    numElements++;
  }

  protected int getNumElements() {
    return numElements;
  }
}
