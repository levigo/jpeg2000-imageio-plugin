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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.levigo.jadice.format.jpeg2000.internal.decode.seda.ExecutionStrategy.Decorator;

public class SedaPipelineTest {

  @Test
  public void testSimplePipelineExecution() throws Exception {
    final Producer<String, Exception> source = new Producer<String, Exception>() {
      @Override
      public void run(Consumer<String, Exception> next) throws Exception {
        next.consume("Hello");
      }
    };

    final Transformer<String, String, Exception> transformer1 = new Transformer<String, String, Exception>() {
      @Override
      public void transform(String s, Consumer<? super String, ? extends Exception> next) throws Exception {
        next.consume(s + " world!");
      }
    };

    final Transformer<String, String, Exception> transformer2 = new Transformer<String, String, Exception>() {
      @Override
      public void transform(String s, Consumer<? super String, ? extends Exception> next) throws Exception {
        next.consume(s.replaceFirst("world", "cruel world"));
      }
    };

    @SuppressWarnings("unchecked")
    final Consumer<String, Exception> sink = Mockito.mock(Consumer.class);

    final Pipeline<Exception> pipeline = Pipeline.startWith(source).append(transformer1).append(transformer2).finishWith(
        sink);

    new NaiveSingleThreadedExecutionStrategy().execute(pipeline);

    Mockito.verify(sink).consume("Hello cruel world!");
  }

  @Test
  public void testStagedPipelineExecution() throws Exception {
    final AtomicInteger callCounter = new AtomicInteger(1);

    final Producer<String, Exception> source = new Producer<String, Exception>() {
      @Override
      public void run(Consumer<String, Exception> next) throws Exception {
        next.consume(callCounter.getAndIncrement() + " Hello");
        next.consume(callCounter.getAndIncrement() + " Hello");
      }
    };

    final Transformer<String, String, Exception> transformer1 = new Transformer<String, String, Exception>() {
      @Override
      public void transform(String s, Consumer<? super String, ? extends Exception> next) throws Exception {
        next.consume(s + " " + callCounter.getAndIncrement() + " world!");
      }
    };

    @SuppressWarnings("unchecked")
    final Consumer<String, Exception> sink = mock(Consumer.class);

    final Pipeline<Exception> pipeline = Pipeline.startWith(source).append(transformer1).finishWith(sink);

    new StageByStageBufferedExecutionStrategy().execute(pipeline);

    verify(sink).consume("1 Hello 3 world!");
    verify(sink).consume("2 Hello 4 world!");
  }

  @Test
  public void testSingleThreadedDecorators() throws Exception {
    class Foo implements Producer<String, Exception> {
      @Override
      public void run(Consumer<String, Exception> next) throws Exception {
        next.consume("foo");
      }
    }

    class Bar implements Transformer<String, String, Exception> {
      @Override
      public void transform(String s, Consumer<? super String, ? extends Exception> next) throws Exception {
        next.consume(s + "!");
      }
    }

    class Baz implements Consumer<String, Exception> {
      @Override
      public void consume(String t) {
      }
    }

    final Foo foo = new Foo();
    final Bar bar = new Bar();
    final Baz baz = new Baz();
    final Pipeline<Exception> pipeline = Pipeline.startWith(foo).append(bar).finishWith(baz);

    final NaiveSingleThreadedExecutionStrategy xs = new NaiveSingleThreadedExecutionStrategy();

    final Decorator decorator = Mockito.spy(new Decorator() {
      @Override
      public void started(Stage s) {
      }

      @Override
      public void error(Stage s, Throwable t) {
      }

      @Override
      public void completed(Stage s) {
      }

      @Override
      public <T> T forward(Stage from, Stage to, T t) {
        return t;
      }
    });

    xs.decorate(decorator);

    xs.execute(pipeline);

    final InOrder io = inOrder(decorator);

    io.verify(decorator).started(foo);
    // io.verify(decorator).forward(foo, bar, "foo");
    // io.verify(decorator).forward(bar, baz, "foo!");
    // io.verify(decorator).completed(baz);
    // io.verify(decorator).completed(bar);
    io.verify(decorator).completed(foo);
    io.verifyNoMoreInteractions();
  }
}
