package com.levigo.jadice.format.jpeg2000;

import org.jadice.util.log.message.LogMessage;
import org.jadice.util.log.qualified.MessageID;

public enum TestMessages implements MessageID {
  @LogMessage("Unexpected error: {0}")
  UNEXPECTED_ERROR,

  @LogMessage("Expectation not satisfied. Expected {0}. Actual {1}.")
  EXPECTATION_NOT_SATISFIED;

  private static final String COMPONENT_ID = "JPEG2000.TEST";

  @Override
  public String getComponentID() {
    return COMPONENT_ID;
  }
}
