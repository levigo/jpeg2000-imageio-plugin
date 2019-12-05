package com.levigo.jadice.format.jpeg2000.internal.debug;

import static org.jadice.util.base.Objects.requireNotNull;

public class BasicProtocolElement implements ProtocolElement {

  private final ProtocolToken token;
  private final Parameter parameter;

  public static BasicProtocolElement newElement(ProtocolToken token, Parameter parameter) {
    return new BasicProtocolElement(token, parameter);
  }

  public BasicProtocolElement(ProtocolToken token, Parameter parameter) {
    this.token = requireNotNull("token", token);
    this.parameter = requireNotNull("parameter", parameter);
  }

  @Override
  public ProtocolToken token() {
    return token;
  }

  @Override
  public Parameter parameter() {
    return parameter;
  }

  @Override
  public boolean matches(Object o) {
    if(!(o instanceof ProtocolElement)) {
      return false;
    }

    final ProtocolElement other = (ProtocolElement) o;
    return token.matches(other.token()) && parameter.matches(other.parameter());
  }

  @Override
  public String toString() {
    return token + " {" + parameter + "}";
  }
}
