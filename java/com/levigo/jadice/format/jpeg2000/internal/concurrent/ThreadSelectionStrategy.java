package com.levigo.jadice.format.jpeg2000.internal.concurrent;

public interface ThreadSelectionStrategy {

  Thread selectThread(ThreadPool pool);

}
