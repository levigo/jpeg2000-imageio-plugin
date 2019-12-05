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
import com.levigo.jadice.format.jpeg2000.internal.param.DirectParameterInfo;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Defined in <i>ITU-T.801, A.3.3</i>
 * <p>
 * <b>Function:</b> Describes the arbitrary decomposition pattern for the lowest resolution sub-band for all tiles of a
 * given component.
 * <p>
 * <b>Usage:</b> Present only if the custom decomposition style bit in the {@link SIZ#Rsiz} parameter (see
 * <i>ITU-T.801, A.2.1</i>) is a one value. Main header. Assigned to a component by an index in the main header {@link
 * COD} or {@link COC} markers.
 * <p>
 * <b>Length:</b> Variable.
 */
public class DFS extends AbstractMarkerSegment {

  /** Length of marker segment in bytes (not including the marker). */
  public int Ldfs;

  /**
   * The index of this {@link DFS} marker segment. This marker segment is associated with a component via the parameter
   * in the {@link COD} or {@link COC} marker segments found in the main header.
   */
  public int Sdfs;

  /** Number of elements in the string defining the number of decomposition sub-levels. */
  public int Idfs;

  /**
   * String defining the number of decomposition sub-levels. The two bit elements are packed into bytes in big endian
   * order. The final byte is padded to a byte boundary.
   */
  public byte[] Ddfs;

  @Override
  public Marker getMarker() {
    return Marker.DFS;
  }

  @Override
  public MarkerKey getMarkerKey() {
    return new MarkerKey(getMarker(), Sdfs);
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Ldfs = source.readUnsignedShort();
    Sdfs = source.readUnsignedShort();
    Idfs = source.readUnsignedByte();
    Ddfs = new byte[Idfs];
    source.readFully(Ddfs);
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    throw new UnsupportedOperationException("Currently not yet implemented.");
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    throw new UnsupportedOperationException("Currently not yet implemented.");
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Ldfs", Ldfs, "DFS.L"));
    markerInfo.add(new PropertiesParameterInfo("Sdfs", Sdfs, "DFS.S"));
    markerInfo.add(new PropertiesParameterInfo("Idfs", Idfs, "DFS.I"));
    markerInfo.add(new PropertiesParameterInfo("Ddfs", "DFS.I"));
    for (int i = 0; i < Ddfs.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", Ddfs[i]));
    }
  }
}
