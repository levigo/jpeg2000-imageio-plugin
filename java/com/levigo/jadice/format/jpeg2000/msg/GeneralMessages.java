package com.levigo.jadice.format.jpeg2000.msg;

import org.jadice.util.log.message.LogMessage;
import org.jadice.util.log.qualified.MessageID;

import com.levigo.jadice.document.ProductInformation;

public enum GeneralMessages implements MessageID {

  @LogMessage("Wrapped exception.")
  WRAPPED_EXCEPTION,

  @LogMessage("Unsupported number of components: {0}.")
  UNSUPPORTED_COMPONENT_COUNT,

  @LogMessage("Missing result.")
  MISSING_RESULT,

  @LogMessage("I/O error occurred.")
  IO_ERROR,

  @LogMessage("Processing error occurred.")
  PROCESSING_ERROR;

  private static final String COMPONENT_ID = ProductInformation.getProductId() + ".FORMAT.JPEG2000.GENERAL";

  @Override
  public String getComponentID() {
    return COMPONENT_ID;
  }
}
