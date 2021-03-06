package com.levigo.jadice.format.jpeg2000.internal.codestream;

import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.JPEG2000Matcher;
import com.levigo.jadice.format.jpeg2000.internal.fileformat.Box;
import com.levigo.jadice.format.jpeg2000.internal.fileformat.BoxType;
import com.levigo.jadice.format.jpeg2000.internal.fileformat.Boxes;
import com.levigo.jadice.format.jpeg2000.internal.fileformat.FileFormatReader;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.io.MarkerReader;
import com.levigo.jadice.format.jpeg2000.internal.marker.Marker;
import com.levigo.jadice.format.jpeg2000.internal.marker.MarkerSegment;
import com.levigo.jadice.format.jpeg2000.internal.marker.SOT;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;
import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Collection;

public class Codestreams {

  private static final QualifiedLogger LOGGER = LoggerFactory.getQualifiedLogger(Codestreams.class);

  public static SeekableInputStream createCodestreamSource(SeekableInputStream source)
      throws IOException, JPEG2000Exception {

    if (JPEG2000Matcher.isCodestream(source)) {
      return source;
    }

    if (JPEG2000Matcher.isFileFormat(source)) {
      final FileFormatReader fileFormatReader = new FileFormatReader();
      final Collection<Box> boxes = fileFormatReader.read(source);
      final Box box = Boxes.findBox(BoxType.ContiguousCodestream, boxes);
      if (box != null) {
        return box.DBox;
      }
    }

    throw new JPEG2000Exception(CodestreamMessages.MISSING_SOURCE_FOR_READING);
  }

  public static TilePart readTilePartHeader(ImageInputStream source, Codestream codestream)
      throws IOException, JPEG2000Exception {

    final long tilePartStart = source.getStreamPosition();

    final MarkerReader markerReader = new MarkerReader();
    Marker marker = markerReader.next(source);
    if (marker != Marker.SOT) {
      throw new JPEG2000Exception(CodestreamMessages.EXPECTED_SOT_MARKER);
    }

    final SOT sot = (SOT) readSegment(source, codestream, marker);

    final TilePart tilePart = new TilePart();
    tilePart.initialize(sot, codestream, tilePartStart);

    marker = markerReader.next(source);

    if (marker == Marker.SOD) {
      // There are no marker segments if we reach the SOD marker at this point.
      return tilePart;
    }

    // Retrieve the marker segment store which is in charge for the current tile-part.
    final MarkerSegmentContainer markerSegments;
    if (tilePart.isFirst) {
      final Tile currentTile = tilePart.tile;
      markerSegments = currentTile.markers;
    } else {
      markerSegments = tilePart.markerSegments;
    }

    // Loop through all remaining marker segments in the current tile-part header until a SOD marker occurred. Put
    // every marker segment into the store for later retrieval.
    while (marker != Marker.SOD) {
      final MarkerSegment markerSegment = readSegment(source, codestream, marker);
      if (markerSegment != null) {
        markerSegments.register(markerSegment.getMarkerKey(), markerSegment);
      }
      marker = markerReader.next(source);
    }

    return tilePart;
  }

  public static void readTilePartHeader(ImageInputStream source, Codestream codestream, MarkerSegmentContainer markers)
      throws IOException, JPEG2000Exception {

    final MarkerReader markerReader = new MarkerReader();
    Marker marker = markerReader.next(source);
    while (marker != Marker.SOD) {
      if (marker.hasParameters) {
        final MarkerSegment markerSegment = marker.createMarkerSegment();
        if (markerSegment != null) {
          markerSegment.read(source, codestream, false);
          markers.register(markerSegment.getMarkerKey(), markerSegment);
        }
      }
      marker = markerReader.next(source);
    }
  }

  public static MarkerSegment readSegment(ImageInputStream source, Codestream codestream, Marker marker)
      throws JPEG2000Exception, IOException {
    final MarkerSegment markerSegment = marker.createMarkerSegment();
    if (markerSegment != null && marker.hasParameters) {
      markerSegment.read(source, codestream, false);
    }
    return markerSegment;
  }
}
