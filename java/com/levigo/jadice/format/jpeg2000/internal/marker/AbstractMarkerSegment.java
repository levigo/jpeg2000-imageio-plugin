package com.levigo.jadice.format.jpeg2000.internal.marker;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

public abstract class AbstractMarkerSegment implements MarkerSegment {

  protected static final QualifiedLogger LOGGER = LoggerFactory.getQualifiedLogger(AbstractMarkerSegment.class);

  @Override
  public MarkerKey getMarkerKey() {
    return getMarker().key();
  }

  @Override
  public void read(ImageInputStream source, Codestream codestream, boolean validate)
      throws IOException, JPEG2000Exception {

    read(source, codestream);

    if (validate) {
      validate(codestream);
    }

    if (Debug.LOG_MARKER_READ && LOGGER.isInfoEnabled()) {
      final MarkerInfo markerInfo = new MarkerInfo(this);
      fillMarkerInfo(markerInfo);
      LOGGER.info(markerInfo.getFormatted());
    }
  }

  @Override
  public void read(ImageInputStream source, boolean validate) throws IOException, JPEG2000Exception {
    read(source, null, validate);
  }

  @Override
  public void write(ImageOutputStream sink, Codestream codestream, boolean validate)
      throws IOException, JPEG2000Exception {

    if (validate) {
      validate(codestream);
    }

    sink.writeShort(getMarker().code);
    write(sink, codestream);

    if (Debug.LOG_MARKER_WRITE && LOGGER.isInfoEnabled()) {
      final MarkerInfo markerInfo = new MarkerInfo(this);
      fillMarkerInfo(markerInfo);
      LOGGER.info(markerInfo.getFormatted());
    }
  }

  protected abstract void read(ImageInputStream source, Codestream codestream) throws IOException, JPEG2000Exception;

  protected abstract void write(ImageOutputStream sink, Codestream codestream) throws IOException;

  protected abstract void validate(Codestream codestream) throws JPEG2000Exception;

  protected abstract void fillMarkerInfo(MarkerInfo markerInfo);

  @Override
  public String toString() {
    final MarkerInfo markerInfo = new MarkerInfo(this);
    fillMarkerInfo(markerInfo);
    return markerInfo.getFormatted();
  }
}
