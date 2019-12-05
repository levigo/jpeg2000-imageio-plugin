package com.levigo.jadice.format.jpeg2000.internal.fileformat;

import static com.levigo.jadice.format.jpeg2000.internal.fileformat.BoxType.ChannelDefinition;

import java.io.IOException;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;

public class ChannelDefinitionBox extends Box {
  @Override
  public BoxType getBoxType() {
    return ChannelDefinition;
  }
  @Override
  protected boolean readDBox() throws JPEG2000Exception, IOException {
    return false;
  }
}
