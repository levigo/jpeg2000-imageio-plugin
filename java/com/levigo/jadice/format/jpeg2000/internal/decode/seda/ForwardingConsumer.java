package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

// part of an experiment. Currently not in use.
public abstract class ForwardingConsumer<T, X extends Throwable> implements Consumer<T, X> {

  private final Consumer<? super T, ? extends X> next;

  public ForwardingConsumer(Consumer<? super T, ? extends X> next) {
    this.next = next;
  }

  @Override
  public void consume(T t) throws X {
    next.consume(t);
  }
}
