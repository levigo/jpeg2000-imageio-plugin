package com.levigo.jadice.format.jpeg2000.internal.codestream;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.document.io.ConcatenatedInputStream;
import com.levigo.jadice.document.io.SeekableInputStream;

@Refer(to = Spec.J2K_CORE, page = 56, section = "B.10.7", called = "Length of the Compressed Image Data from a Given Code Block")
public class Codeword {

  public volatile int passes;
  public volatile int passIdx;

  public int numBytes;

  public SeekableInputStream input;

  public void merge(Codeword additional) {
    passes += additional.passes;
    numBytes += additional.numBytes;

    if (input == null) {
      input = new ConcatenatedInputStream();
    }
    
    ((ConcatenatedInputStream) input).appendInputStream(additional.input);
  }
}
