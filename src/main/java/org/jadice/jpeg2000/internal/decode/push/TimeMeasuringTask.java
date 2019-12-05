/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jadice.jpeg2000.internal.decode.push;

import org.jadice.jpeg2000.internal.Debug;

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
