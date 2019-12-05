package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

import java.util.ArrayList;
import java.util.List;

// part of an experiment. Currently not in use.
@SuppressWarnings("rawtypes")
public abstract class AbstractExecutionStrategy implements ExecutionStrategy {
  static class CompositeDecorator implements Decorator {
    final List<Decorator> decorators = new ArrayList<>();

    @Override
    public void started(Stage s) {
      for (final Decorator d : decorators) {
        d.started(s);
      }
    }

    @Override
    public void completed(Stage s) {
      for (final Decorator d : decorators) {
        d.completed(s);
      }
    }

    @Override
    public void error(Stage s, Throwable t) {
      for (final Decorator d : decorators) {
        d.error(s, t);
      }
    }

    @Override
    public <T> T forward(Stage from, Stage to, T t) {
      for (final Decorator d : decorators) {
        t = d.forward(from, to, t);
      }
      return t;
    }
  }

  // Build production graph
  abstract class Edge implements Consumer<Object, Throwable> {
    Edge downstream;
    Edge upstream;

    public void setNext(Edge d) {
      downstream = d;
      downstream.upstream = this;
    }

    public void run() throws Throwable {
      // nothing to do
    }

    @Override
    public void consume(Object t) throws Throwable {
      // nothing to do
    }
  }

  protected final CompositeDecorator decorator = new CompositeDecorator();

  public AbstractExecutionStrategy() {
    super();
  }

  @Override
  public void decorate(Decorator decorator) {
    this.decorator.decorators.add(decorator);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <X extends Throwable> void execute(Pipeline<X> pipeline) throws X {
    final List<Stage> stages = pipeline.getStages();

    // Wire up pipeline
    final List<Edge> edges = new ArrayList<>();
    for (final Stage s : stages) {
      final Edge currentEdge = edges.isEmpty() ? null : edges.get(edges.size() - 1);
      if (s instanceof Producer) {
        if (null != currentEdge)
          throw new IllegalArgumentException("Producer must be first in pipeline");
        edges.add(create((Producer) s));
      } else if (s instanceof Consumer) {
        if (null == currentEdge)
          throw new IllegalArgumentException("Consumer must be preceded by a Producer or a Transformer");
        edges.add(create((Consumer) s));
      } else if (s instanceof Transformer) {
        if (null == currentEdge)
          throw new IllegalArgumentException("Transformer must be preceded by a Producer or a Transformer");
        edges.add(create((Transformer) s));
      }

      // wire stages
      if (edges.size() > 1)
        edges.get(edges.size() - 2).setNext(edges.get(edges.size() - 1));
    }

    // Crank pipeline by running all edges
    for (final Edge e : edges) {
      try {
        e.run();
      } catch (final Throwable t) {
        throw (X) t;
      }
    }
  }

  protected abstract Edge create(Transformer t);


  protected abstract Edge create(Consumer c);


  protected abstract Edge create(final Producer p);
}