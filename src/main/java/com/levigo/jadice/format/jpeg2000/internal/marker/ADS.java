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
import com.levigo.jadice.format.jpeg2000.internal.param.DirectParameterInfo;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * <b>Function:</b> Describes the arbitrary decomposition pattern for a tile-component or all tile-components within a
 * single tile.
 * <p>
 * <b>Usage:</b> Present only if the custom decomposition style capability bit in the {@link SIZ#Rsiz} parameter (see
 * <i>ITU-T.801, A.2.1</i>) is a one value. Shall not be used to describe the decomposition described in <i>ITU-T.800,
 * Annex F</i>. Main and first tile-part header of a given tile. There may be up to 127 such marker segments with
 * unique index values. If an index value is found in a tile-part header, then it is used instead of an {@link ADS}
 * marker segment in the main header with the same index value. These are assigned to a particular tile-component via
 * the parameter in the {@link COD} or {@link COC} marker segments found only in a specific tile-part header.
 * <p>
 * <b>Length:</b> Variable.
 */
public class ADS extends AbstractMarkerSegment {

  /**
   * Length of marker segment in bytes (not including the marker).
   */
  public int Lads;

  /**
   * The index of this {@link ADS} marker segment. This marker segment is associated with a component via the parameter
   * in the {@link COD} or {@link COC} marker segments found in that tile-part header.
   */
  public int Sads;

  /**
   * Number of elements in the string defining the number of decomposition sub-levels.
   */
  public int IOads;

  /**
   * String defining the number of decomposition sub-levels. The two bit elements are packed into bytes in big endian
   * order. The final byte is padded to a byte boundary.
   */
  public byte[] DOads;

  /**
   * Number of elements in the string defining the arbitrary decomposition structure.
   */
  public int ISads;

  /**
   * String defining the arbitrary decomposition structure. The two bit elements are packed into bytes in big endian
   * order. The final byte is padded to a byte boundary.
   */
  public byte[] DSads;

  @Override
  public Marker getMarker() {
    return Marker.ADS;
  }

  @Override
  public MarkerKey getMarkerKey() {
    return new MarkerKey(getMarker(), Sads);
  }


  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lads = source.readUnsignedShort();
    Sads = source.readUnsignedByte();

    IOads = source.readUnsignedByte();
    DOads = new byte[IOads];
    source.readFully(DOads);

    ISads = source.readUnsignedByte();
    DSads = new byte[ISads];
    source.readFully(DSads);
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lads);
    sink.writeByte(Sads);
    sink.writeByte(IOads);
    sink.write(DOads);
    sink.writeByte(ISads);
    sink.write(DSads);
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    Validate.inRange("Lads", Lads, 3, 65535);
    Validate.inRange("Sads", Sads, 1, 127);
    Validate.inRange("IOads", IOads, 0, 255);
    Validate.inRange("ISads", ISads, 0, 255);
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lads", Lads, "ADS.L"));
    markerInfo.add(new PropertiesParameterInfo("Sads", Sads, "ADS.S"));
    markerInfo.add(new PropertiesParameterInfo("IOads", IOads, "ADS.IO"));
    markerInfo.add(new PropertiesParameterInfo("DOads", "ADS.DO"));
    for (int i = 0; i < DOads.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", DOads[i]));
    }
    markerInfo.add(new PropertiesParameterInfo("ISads", ISads, "ADS.IS"));
    markerInfo.add(new PropertiesParameterInfo("DSads", "ADS.DS"));
    for (int i = 0; i < DSads.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", DSads[i]));
    }
  }
}
