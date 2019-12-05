package com.levigo.jadice.format.jpeg2000.internal.tier1;

public interface Expectation<T> {

  void validate(T t) throws ExpectationNotSatisfiedException;

}
