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



// part of an experiment. Currently not in use.
public interface ExecutionStrategy {
  interface Decorator {
    public void started(Stage s);
  
    public void completed(Stage s);
  
    public void error(Stage s, Throwable t);
  
    public <T> T forward(Stage from, Stage to, T t);
  }

  void decorate(Decorator decorator);

  <X extends Throwable> void execute(Pipeline<X> pipeline) throws X;
}
