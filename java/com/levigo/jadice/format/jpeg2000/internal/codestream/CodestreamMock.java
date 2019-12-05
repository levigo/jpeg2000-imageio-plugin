package com.levigo.jadice.format.jpeg2000.internal.codestream;

import com.levigo.jadice.format.jpeg2000.internal.image.DefaultGridRegion;

public class CodestreamMock extends Codestream {

  public void setGridElement(DefaultGridRegion gridElement) {
    this.canvas = gridElement;
  }
  
}
