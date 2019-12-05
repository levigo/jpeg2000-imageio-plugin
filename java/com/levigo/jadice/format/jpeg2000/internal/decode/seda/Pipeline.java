package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

import java.util.List;

// part of an experiment. Currently not in use.
public class Pipeline<X extends Throwable> {
  private final List<Stage> stages;

  Pipeline(List<Stage> stages) {
    this.stages = stages;
  }

  public static <S, X extends Throwable> PipelineBuilder<S, X> startWith(Producer<S, X> producer) {
    return new PipelineBuilder<>(producer);
  }

  public List<Stage> getStages() {
    return stages;
  }
}
