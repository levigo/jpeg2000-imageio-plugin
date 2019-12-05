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
