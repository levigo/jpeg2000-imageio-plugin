package com.levigo.jadice.format.jpeg2000.msg;

import org.jadice.util.log.message.LogMessage;
import org.jadice.util.log.qualified.MessageID;

import com.levigo.jadice.document.ProductInformation;

public enum JPXMessages implements MessageID {
  @LogMessage("JPX must start with an JPEG2000 Signature box, but was {0}.")
  EXPECTED_JPEG2000_SIGNATURE_BOX,

  @LogMessage("JPX expects a File Type box after a JPEG2000 Signature box, but was {0}.")
  EXPECTED_FILE_TYPE_BOX,

  @LogMessage("Illegal LBox value {0}.")
  ILLEGAL_VALUE_FOR_LBOX;

  public static final String COMPONENT_ID = ProductInformation.getProductId() + "FORMAT.JPEG2000.JPX";

  @Override
  public String getComponentID() {
    return COMPONENT_ID;
  }
}
