/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jadice.jpeg2000.internal.marker;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.io.SectorInputStream;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PLM extends AbstractMarkerSegment {

  /** Length of marker segment in bytes (not including the marker). */
  public int Lplm;

  /** Index of this marker segment relative to all other PLM marker segments present in the current header. */
  public int Zplm;

  /**
   * Length of the jth packet in the ith tile-part. If packet headers are stored with the packet, this length includes
   * the packet header. If packet headers are stored in {@link PPM} or {@link PPT}, this length does not include the
   * packet header length. One range of values for each tile-part. One value for each packet in the tile.
   */
  public List<SectorInputStream> Iplm;

  public PLM() {
    Iplm = new LinkedList<>();
  }

  @Override
  public Marker getMarker() {
    return Marker.PLM;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException, JPEG2000Exception {
    Lplm = source.readUnsignedShort();
    Zplm = source.readUnsignedByte();

    int remainingSegmentBytes = Lplm - 3;
    while (remainingSegmentBytes > 0) {
      int numIplmBytes = source.readUnsignedByte();
      Iplm.add(new SectorInputStream(source, source.getStreamPosition(), numIplmBytes));
      source.skipBytes(numIplmBytes);
      remainingSegmentBytes -= numIplmBytes + 1;
    }
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    throw new UnsupportedOperationException("Currently not yet implemented");
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    throw new UnsupportedOperationException("Currently not yet implemented");
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    throw new UnsupportedOperationException("Currently not yet implemented");
  }
}
