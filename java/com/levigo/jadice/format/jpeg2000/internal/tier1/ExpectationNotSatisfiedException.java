package com.levigo.jadice.format.jpeg2000.internal.tier1;

import com.levigo.jadice.document.JadiceException;
import com.levigo.jadice.format.jpeg2000.TestMessages;

public class ExpectationNotSatisfiedException extends JadiceException {

  public ExpectationNotSatisfiedException(Object expected, Object actual) {
    super(TestMessages.EXPECTATION_NOT_SATISFIED, expected, actual);
  }

}
