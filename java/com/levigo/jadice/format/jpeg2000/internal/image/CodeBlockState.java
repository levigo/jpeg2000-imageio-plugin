package com.levigo.jadice.format.jpeg2000.internal.image;

import com.levigo.jadice.format.jpeg2000.internal.tier1.Pass;

public class CodeBlockState {
  public int bitplane;
  public int[] sampleBuffer;
  public int[] ctxBuffer;
  public Pass lastPass;
  public int passIdx;
  public boolean finished;
}
