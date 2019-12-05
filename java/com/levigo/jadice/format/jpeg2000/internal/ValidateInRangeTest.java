package com.levigo.jadice.format.jpeg2000.internal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.msg.ValidationMessages;

public class ValidateInRangeTest {

  @Test
  public void valid() throws JPEG2000Exception {
    Validate.inRange("value=within", 1, 0, 2);
    Validate.inRange("value=min", 0, 0, 2);
    Validate.inRange("value=max", 2, 0, 2);
  }
  
  @Test
  public void invalidTooSmall() throws JPEG2000Exception {
    JPEG2000Exception actual = null;
    try {
      Validate.inRange("value=too small", 2, 3, 6);
    } catch (JPEG2000Exception e) {
      actual = e;
    }

    Assertions.assertNotNull(actual);

    Assertions.assertEquals(ValidationMessages.ILLEGAL_RANGE, actual.getMessageID(), "message id");
  }

  @Test
  public void invalidTooLarge() throws JPEG2000Exception {
    JPEG2000Exception actual = null;
    try {
      Validate.inRange("value=too large", 6, 1, 5);
    } catch (JPEG2000Exception e) {
      actual = e;
    }

    Assertions.assertNotNull(actual);

    Assertions.assertEquals(ValidationMessages.ILLEGAL_RANGE, actual.getMessageID(), "message id");
  }

}
