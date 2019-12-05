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
package org.jadice.jpeg2000.internal.decode.seda;

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
