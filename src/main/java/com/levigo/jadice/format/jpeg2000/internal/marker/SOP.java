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
package com.levigo.jadice.format.jpeg2000.internal.marker;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Start of packet marker segment as defined in <i>ITU-T.800, A.8.1</i>
 * <p>
 * <b>Function:</b> Marks the beginning of a packet within a codestream.
 * <p>
 * <b>Usage:</b> Optional. May be used in the bit stream in front of every packet. Shall not be used unless indicated
 * that it is allowed in the proper {@link COD} marker segment (see <i>ITU-T.800, A.6.1</i>). If {@link PPM} or {@link
 * PPT} marker segments are used, then the {@link SOP} marker segment may appear immediately before the packet data in
 * the bit stream.
 * <p>
 * If {@link SOP} marker segments are allowed (by signalling in the {@link COD} marker segment, see <i>ITU-T.800,
 * A.6.1</i>), each packet in any given tile-part may or may not be appended with an {@link SOP} marker segment.
 * However, whether or not the {@link SOP} marker segment is used, the count in the {@link #Nsop} is incremented for
 * each packet. If the packet headers are moved to a {@link PPM} or {@link PPT} marker segments (see <i>ITU-T.800,
 * A.7.4</i> and <i>ITU-T.800, A.7.5</i>), then the {@link SOP} marker segments may appear immediately before the
 * packet body in the tile-part compressed image data portion.
 * <p>
 * <b>Length:</b> Fixed.
 */
public class SOP extends AbstractMarkerSegment {

  /** Length of marker segment in bytes, not including the marker. */
  public int Lsop;

  /**
   * Packet sequence number. The first packet in a coded tile is assigned the value zero. For every successive packet
   * in this coded tile this number is incremented by one. When the maximum number is reached, the number rolls over to
   * zero. Valid range is 0 to 65535.
   */
  public int Nsop;


  @Override
  public Marker getMarker() {
    return Marker.SOP;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lsop = source.readUnsignedShort();
    Nsop = source.readUnsignedShort();
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lsop);
    sink.writeShort(Nsop);
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    // Nothing worth to validate.
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lsop", Lsop, "SOP.L"));
    markerInfo.add(new PropertiesParameterInfo("Nsop", Nsop, "SOP.N"));
  }

  @Override
  public MarkerKey getMarkerKey() {
    return new MarkerKey(getMarker(), Nsop);
  }
}
