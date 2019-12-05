package com.levigo.jadice.format.jpeg2000.internal;


import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.msg.ValidationMessages;

public class Validate {

  public static void inRange(String identifier, long value, long min, long max) throws JPEG2000Exception {
    if (value < min || value > max) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_RANGE, value, identifier, min, max);
    }
  }

  public static void notNull(String identifier, Object value) throws JPEG2000Exception {
    if (value == null) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_NULL_VALUE, identifier);
    }
  }

  public static void exact(String identifier, Object value, Object expected) throws JPEG2000Exception {
    if (!value.equals(expected)) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_VALUE, identifier, value, expected);
    }
  }
}
