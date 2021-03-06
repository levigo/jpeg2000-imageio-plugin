package com.levigo.jadice.format.jpeg2000.internal.fileformat;

import static com.levigo.jadice.format.jpeg2000.internal.fileformat.BoxType.FileType;

import java.io.IOException;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;

/**
 * Specified by <i>ITU-T.800, I.5.2</i>
 * <p>
 * The File Type box specifies the Recommendation | International Standard which completely defines all of the contents
 * of this file, as well as a separate list of readers, defined by other Recommendations | International Standards,
 * with which this file is compatible, and thus the file can be properly interpreted within the scope of that other
 * standard. This box shall immediately follow the JPEG 2000 Signature box. This differentiates between the standard
 * which completely describes the file, from other standards that interpret a subset of the file.
 */
public class FileTypeBox extends Box {
  @Override
  public BoxType getBoxType() {
    return FileType;
  }
  
  @Override
  protected boolean readDBox() throws JPEG2000Exception, IOException {
    return false;
  }
}
