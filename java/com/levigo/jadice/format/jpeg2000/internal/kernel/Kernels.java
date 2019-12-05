package com.levigo.jadice.format.jpeg2000.internal.kernel;

import com.levigo.jadice.format.jpeg2000.internal.marker.COx;

// part of an experiment. Currently not in use.
public class Kernels {

  public static Kernel createKernel(int kernelId) {
    // TODO implement
    switch (kernelId){
      case COx.VALUE_KERNEL_5_3:
      case COx.VALUE_KERNEL_9_7:
      default:
        throw new UnsupportedOperationException("Only kernels defined in JPEG2000 Part-1 (ITU-T.800) are supported.");
    }
  }

}
