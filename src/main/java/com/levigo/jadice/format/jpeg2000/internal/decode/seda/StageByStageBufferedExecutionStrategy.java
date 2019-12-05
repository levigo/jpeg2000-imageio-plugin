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
