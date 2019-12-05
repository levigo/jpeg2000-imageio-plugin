package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

// part of an experiment. Currently not in use.
public interface Transformer<S, D, X extends Throwable> extends Stage {
  void transform(S s, Consumer<? super D, ? extends X> next) throws X;
}
