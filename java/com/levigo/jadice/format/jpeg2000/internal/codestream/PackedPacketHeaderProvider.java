package com.levigo.jadice.format.jpeg2000.internal.codestream;

import com.levigo.jadice.format.jpeg2000.internal.marker.PPM;

import java.util.TreeSet;

public class PackedPacketHeaderProvider {

  public TreeSet<PPM> markerSegments;

  public PackedPacketHeaderProvider() {
    markerSegments = new TreeSet<>();
  }

  public void register(PPM markerSegment) {
    markerSegments.add(markerSegment);
  }

}
