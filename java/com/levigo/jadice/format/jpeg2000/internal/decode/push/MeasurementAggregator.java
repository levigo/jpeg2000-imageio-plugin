package com.levigo.jadice.format.jpeg2000.internal.decode.push;

public interface MeasurementAggregator<T> {

  void aggregate(T measurement);
  
  T get();
  
}
