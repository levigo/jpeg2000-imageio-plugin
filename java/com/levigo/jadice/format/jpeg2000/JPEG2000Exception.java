package com.levigo.jadice.format.jpeg2000;

import com.levigo.jadice.document.JadiceException;
import org.jadice.util.log.qualified.MessageID;

public class JPEG2000Exception extends JadiceException {

  private static final long serialVersionUID = 1L;

  public JPEG2000Exception(MessageID message) {
    super(message);
  }

  public JPEG2000Exception(MessageID message, Object... args) {
    super(message, args);
  }

  public JPEG2000Exception(MessageID messageId, Throwable cause, Object... args) {
    super(messageId, cause, args);
  }
}
