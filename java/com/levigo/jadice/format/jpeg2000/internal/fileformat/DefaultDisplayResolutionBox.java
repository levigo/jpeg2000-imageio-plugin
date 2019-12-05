package com.levigo.jadice.format.jpeg2000.internal.fileformat;

import static com.levigo.jadice.format.jpeg2000.internal.fileformat.BoxType.DefaultDisplayResolution;

import java.io.IOException;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;

public class DefaultDisplayResolutionBox extends Box {
  @Override
  public BoxType getBoxType() {
    return DefaultDisplayResolution;
  }
  @Override
  protected boolean readDBox() throws JPEG2000Exception, IOException {
    return false;
  }
}
