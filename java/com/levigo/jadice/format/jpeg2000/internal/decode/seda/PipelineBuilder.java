package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

import java.util.ArrayList;
import java.util.List;

// part of an experiment. Currently not in use.
public class PipelineBuilder<T, X extends Throwable> {
  private final List<Stage> stages;

  PipelineBuilder(Producer<T, X> p) {
    stages = new ArrayList<>();
    stages.add(p);
  }

  PipelineBuilder(List<Stage> stages, Transformer<?, T, X> t) {
    this.stages = stages;
    stages.add(t);
  }

  public <D> PipelineBuilder<D, X> append(Transformer<T, D, X> t) {
    return new PipelineBuilder<>(stages, t);
  }

  public Pipeline<X> finishWith(Consumer<T, X> c) {
    stages.add(c);
    return new Pipeline<>(stages);
  }
}
