package com.levigo.jadice.format.jpeg2000.internal.codestream;

import com.levigo.jadice.format.jpeg2000.internal.marker.MarkerKey;
import com.levigo.jadice.format.jpeg2000.internal.marker.MarkerSegment;

import java.util.HashMap;
import java.util.Map;

public class MarkerSegmentContainer {
  private final Map<MarkerKey, MarkerSegment> markerSegments;

  public MarkerSegmentContainer() {
    markerSegments = new HashMap<>();
  }

  public MarkerSegment access(MarkerKey markerKey) {
    return markerSegments.get(markerKey);
  }

  public void register(MarkerKey markerKey, MarkerSegment markerSegment) {
    markerSegments.put(markerKey, markerSegment);
  }
}
