package com.levigo.jadice.format.jpeg2000.internal.marker;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Defined in <i>ITU-T.800, A.4.4</i>
 * <p>
 * <b>Function:</b> Indicates the end of the codestream.
 * <p>
 * <i><b>Note 1:</b> This marker shares the same code as the EOI marker in <i>ITU-T Rec. T.81 | ISO/IEC
 * 10918-1</i>.</i>
 * <p>
 * <b>Usage:</b> Shall be the last marker in a codestream. There shall be one EOC per codestream.
 * <p>
 * <i><b>Note 2:</b> In the case a file has been corrupted, it is possible that a decoder could extract much useful
 * compressed image data without encountering an {@link EOC} marker.</i>
 * <p>
 * <b>Length:</b> Fixed.
 */
public class EOC implements MarkerSegment {

  @Override
  public Marker getMarker() {
    return Marker.EOC;
  }

  @Override
  public MarkerKey getMarkerKey() {
    // This won't be effectively called. Don't complain and provide a correct key anyway.
    return getMarker().key();
  }

  @Override
  public void read(ImageInputStream source, Codestream codestream, boolean validate)
      throws IOException, JPEG2000Exception {
    // no parameters to read
  }

  @Override
  public void read(ImageInputStream source, boolean validate) throws IOException, JPEG2000Exception {
    read(source, null, validate);
  }

  @Override
  public void write(ImageOutputStream sink, Codestream codestream, boolean validate)
      throws IOException, JPEG2000Exception {
    // no parameters to write
  }

}
