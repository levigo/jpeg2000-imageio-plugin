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
import com.levigo.jadice.format.jpeg2000.internal.Validate;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.io.SectorInputStream;
import com.levigo.jadice.format.jpeg2000.internal.param.DirectParameterInfo;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Defined in <i>ITU-T.800, A.7.4</i>
 * <p>
 * <b>Function:</b> A collection of the packet headers from all tiles.
 * <p>
 * NOTE – This is useful so multiple reads are not required to decode headers.
 * <p>
 * <b>Usage:</b> Main header. May be used in the main header for all tile-parts unless a {@link PPT} marker segment is
 * used in the tile-part header. The packet headers shall be in only one of three places within the codestream. If the
 * {@link PPM} marker segment is present, all the packet headers shall be found in the main header. In this case, the
 * {@link PPT} marker segment and packets distributed in the bit stream of the tile-parts are disallowed. If there is
 * no {@link PPM} marker segment then the packet headers can be distributed either in {@link PPT} marker segments or
 * distributed in the codestream as defined in B.10. The packet headers shall not be in both a PPT marker segment and
 * the codestream for the same tile. If the packet headers are in {@link PPT} marker segments, they shall appear in a
 * tile-part header before the corresponding packet data appears (i.e., in the same tile-part header or one with a
 * lower {@link SOT#TPsot} value). There may be multiple {@link PPT} marker segments in a tile-part header.
 * <p>
 * <b>Length:</b> Variable depending on the number of packets in each tile-part and the size of the packet headers.
 */
public class PPM extends AbstractMarkerSegment implements Comparable<PPM> {
  public int Lppm;
  public int Zppm;

  public List<SectorInputStream> Ippms;

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException, JPEG2000Exception {
    Lppm = source.readUnsignedShort();
    Zppm = source.readUnsignedByte();

    Ippms = new LinkedList<>();

    int remainingBytes = Lppm - 3;

    while (remainingBytes > 0) {
      final long n = source.readUnsignedInt();

      if ((remainingBytes - n) < 0) {
        throw new JPEG2000Exception(CodestreamMessages.MARKER_SEGMENT_LENGTH_OVERFLOW);
      }

      Ippms.add(new SectorInputStream(source, source.getStreamPosition(), n));

      source.skipBytes(n);
      remainingBytes -= n;
    }
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lppm);
    sink.writeByte(Zppm);
    int read = 0;
    final byte buffer[] = new byte[65535];
    for (SectorInputStream ippm : Ippms) {
      sink.writeInt((int) ippm.length());
      while ((read = ippm.read(buffer)) > 0) {
        sink.write(buffer, 0, read);
      }
    }
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    Validate.inRange("Lppm", Lppm, 7, 65535);
    Validate.inRange("Zppm", Zppm, 0, 255);
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lppm", Lppm, "TODO"));
    markerInfo.add(new PropertiesParameterInfo("Zppm", Zppm, "TODO"));

    markerInfo.add(new PropertiesParameterInfo("Ippm", "TODO"));
    for (int i = 0; i < Ippms.size(); i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "].length", Ippms.get(i).length()));
    }
  }

  @Override
  public Marker getMarker() {
    return Marker.PPM;
  }

  @Override
  public int compareTo(PPM ppm) {
    return this.Zppm - ppm.Zppm;
  }

  public List<SectorInputStream> getIppms() {
    return Ippms;
  }
}
