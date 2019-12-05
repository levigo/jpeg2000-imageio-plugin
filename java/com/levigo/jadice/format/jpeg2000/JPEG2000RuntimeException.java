package com.levigo.jadice.format.jpeg2000;

import com.levigo.jadice.document.JadiceRuntimeException;
import org.jadice.util.log.qualified.MessageID;

/**
 * A {@link RuntimeException} to be used in places where JPEG2000-decoding may cause an exception
 * but no suitable declared exception can be used.
 */
public class JPEG2000RuntimeException extends JadiceRuntimeException {
  private static final long serialVersionUID = 1L;

  public JPEG2000RuntimeException(MessageID messageId, Object[] args) {
    super(messageId, args);
  }

  public JPEG2000RuntimeException(MessageID messageId, Throwable cause, Object... args) {
    super(messageId, cause, args);
  }
}
