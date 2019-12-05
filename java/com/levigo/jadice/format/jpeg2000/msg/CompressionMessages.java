package com.levigo.jadice.format.jpeg2000.msg;

import org.jadice.util.log.message.LogMessage;
import org.jadice.util.log.qualified.MessageID;

public enum CompressionMessages implements MessageID {
  @LogMessage("Corrupt bitstuffing.")
  CORRUPT_BITSTUFFING,

  @LogMessage("Failed to decode block contribution.")
  FAILED_BLOCK_CONTRIBUTION_DECODING,

  @LogMessage("LBlock (beta) overflow.")
  LBLOCK_OVERFLOW,

  @LogMessage("Inconsistent segment lengths.")
  INCONSISTENT_SEGMENT_LENGTHS;

  private static final String COMPONENT_ID = "JPEG2000.COMPRESSION";

  @Override
  public String getComponentID() {
    return COMPONENT_ID;
  }
}
