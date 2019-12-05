package com.levigo.jadice.format.jpeg2000.internal.codestream;

import com.levigo.jadice.format.jpeg2000.internal.marker.MarkerSegment;

import java.util.Iterator;
import java.util.LinkedList;

public class MarkerAccessGraph extends LinkedList<MarkerAccessNode> {

  public MarkerSegment access() {
    final Iterator<MarkerAccessNode> iterator = iterator();
    MarkerSegment markerSegment = null;

    while (markerSegment == null && iterator.hasNext()) {
      final MarkerAccessNode node = iterator.next();
      markerSegment = node.access();
    }

    return markerSegment;
  }

}
