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
