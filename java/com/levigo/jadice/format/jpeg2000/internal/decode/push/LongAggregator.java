package com.levigo.jadice.format.jpeg2000.internal.decode.push;

import java.util.concurrent.atomic.AtomicLong;

public class LongAggregator implements MeasurementAggregator<Long> {

  private final AtomicLong aggregation;

  public LongAggregator() {
    this(0);
  }
  
  public LongAggregator(long initialValue) {
    this.aggregation = new AtomicLong(initialValue);
  }
  
  @Override
  public void aggregate(Long measurement) {
    aggregation.addAndGet(measurement);
  }

  @Override
  public Long get() {
    return aggregation.get();
  }
}
