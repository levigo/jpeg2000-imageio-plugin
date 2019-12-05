package com.levigo.jadice.format.jpeg2000.internal.marker;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

public interface MarkerSegment {

  Marker getMarker();

  MarkerKey getMarkerKey();

  void read(ImageInputStream source, Codestream codestream, boolean validate) throws IOException, JPEG2000Exception;

  void read(ImageInputStream source, boolean validate) throws IOException, JPEG2000Exception;

  void write(ImageOutputStream sink, Codestream codestream, boolean validate) throws IOException, JPEG2000Exception;

}
