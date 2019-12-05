package com.levigo.jadice.format.jpeg2000.internal.codestream;

import com.levigo.jadice.format.jpeg2000.internal.marker.MarkerKey;
import com.levigo.jadice.format.jpeg2000.internal.marker.MarkerSegment;

public class MarkerAccessNode {
  private final MarkerSegmentContainer markerSegmentContainer;
  private final MarkerKey markerKey;

  public MarkerAccessNode(MarkerSegmentContainer markerSegmentContainer, MarkerKey markerKey) {
    this.markerSegmentContainer = markerSegmentContainer;
    this.markerKey = markerKey;
  }

  public MarkerSegment access() {
    return markerSegmentContainer.access(markerKey);
  }
}
