package com.levigo.jadice.format.jpeg2000.internal.concurrent;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;

public interface Task {

  void run() throws JPEG2000Exception;

}
