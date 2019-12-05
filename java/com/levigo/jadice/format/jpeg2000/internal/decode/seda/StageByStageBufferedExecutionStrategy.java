package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

import java.util.ArrayList;
import java.util.List;

// part of an experiment. Currently not in use.
@SuppressWarnings({
    "rawtypes", "unchecked"
})
public class StageByStageBufferedExecutionStrategy extends AbstractExecutionStrategy {
  class SourceEdge extends Edge {
    List<Object> buffer = new ArrayList<>();

    @Override
    public void consume(Object t) {
      buffer.add(t);
    }
  }

  @Override
  protected Edge create(final Transformer t) {
    return new SourceEdge() {
      @Override
      public void run() throws Throwable {
        for (final Object o : buffer) {
          t.transform(o, downstream);
        }
      }
    };
  }

  @Override
  protected Edge create(final Consumer c) {
    return new SourceEdge() {
      @Override
      public void run() throws Throwable {
        for (final Object o : buffer) {
          c.consume(o);
        }
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
