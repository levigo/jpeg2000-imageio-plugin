package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

// part of an experiment. Currently not in use.
public interface Consumer<T, X extends Throwable> extends Stage {
  void consume(T it) throws X;
}
