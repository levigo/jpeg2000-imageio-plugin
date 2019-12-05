package com.levigo.jadice.format.jpeg2000.internal.tier1;

public class CtxExpectation implements Expectation<ContextContainer> {

  private final int expectedIndex;

  public CtxExpectation(int expectedIndex) {
    this.expectedIndex = expectedIndex;
  }

  @Override
  public void validate(ContextContainer contextContainer) throws ExpectationNotSatisfiedException {
    if (contextContainer.index != expectedIndex) {
      throw new ExpectationNotSatisfiedException(expectedIndex, contextContainer.index);
    }
  }
}
