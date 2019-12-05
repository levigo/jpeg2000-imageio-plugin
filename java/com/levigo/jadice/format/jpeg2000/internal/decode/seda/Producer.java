package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

// part of an experiment. Currently not in use.
public interface Producer<S, X extends Throwable> extends Stage {
  void run(Consumer<S, X> next) throws X;
}
