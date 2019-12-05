package com.levigo.jadice.format.jpeg2000.internal.decode.seda;


@SuppressWarnings({
    "unchecked", "rawtypes"
})
// part of an experiment. Currently not in use.
public class NaiveSingleThreadedExecutionStrategy extends AbstractExecutionStrategy implements ExecutionStrategy {
  @Override
  protected Edge create(final Transformer t) {
    return new Edge() {
      @Override
      public void consume(Object it) throws Throwable {
        t.transform(decorator.forward(upstream, t, it), downstream);
      }
    };
  }


  @Override
  protected Edge create(final Consumer c) {
    return new Edge() {
      @Override
      public void consume(Object it) throws Throwable {
        c.consume(decorator.forward(upstream, c, it));
      }
    };
  }


  @Override
  protected Edge create(final Producer p) {
    return new Edge() {
      @Override
      public void run() throws Throwable {
        decorator.started(p);
        p.run(downstream);
        decorator.completed(p);
      }
    };
  }
}
