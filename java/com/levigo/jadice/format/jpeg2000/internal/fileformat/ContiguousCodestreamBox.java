package com.levigo.jadice.format.jpeg2000.internal.fileformat;

import static com.levigo.jadice.format.jpeg2000.internal.fileformat.BoxType.ContiguousCodestream;

import java.io.IOException;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;

public class ContiguousCodestreamBox extends Box {
  @Override
  public BoxType getBoxType() {
    return ContiguousCodestream;
  }
  @Override
  protected boolean readDBox() throws JPEG2000Exception, IOException {
    return false;
  }
}
