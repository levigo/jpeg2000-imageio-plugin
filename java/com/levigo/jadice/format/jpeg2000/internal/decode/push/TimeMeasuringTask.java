package com.levigo.jadice.format.jpeg2000.internal.decode.push;

import com.levigo.jadice.format.jpeg2000.internal.Debug;

public class TimeMeasuringTask<P extends Pushable> implements DecodeTask<P> {

  private final DecodeTask<P> task;
  private final MeasurementAggregator<Long> aggregator;

  public TimeMeasuringTask(DecodeTask<P> task, MeasurementAggregator<Long> aggregator) {
    this.task = task;
    this.aggregator = aggregator;
  }

  @Override
  public P call() throws Exception {
    final long timestampAtStart = System.nanoTime();
    final P p = task.call();
    final long timestampAtFinish = System.nanoTime();
    aggregator.aggregate(timestampAtFinish - timestampAtStart);
    Debug.printTimeNanoBased(aggregator, "EBCoT", System.out);
    return p;
  }
}
