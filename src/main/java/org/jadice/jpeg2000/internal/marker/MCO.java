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
import org.jadice.jpeg2000.internal.Validate;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.param.DirectParameterInfo;
import org.jadice.jpeg2000.internal.param.PropertiesParameterInfo;

import java.io.IOException;

/**
 * Defined in <i>ITU-T.801, A.3.9</i>
 * <p>
 * <b>Function:</b> Describes the order in which multiple component transforms are applied during inverse multiple
 * component transform processing.
 * <p>
 * <b>Usage:</b> Present only if the multiple component transformation capability bit in the {@link SIZ#Rsiz} parameter
 * (see <i>ITU-T.801, A.2.1</i>) is a one value. At most one {@link MCO} marker segment in main and first tile-part
 * header of a given tile. If used in the main header, this marker segment defines the default ordering of multiple
 * component transform stages for all tiles. If used in the first tile-part header, then the component transform order
 * established by the {@link MCO} marker segment overrides any default ordering defined by a main header {@link MCO}
 * marker segment.
 * <p>
 * <b>Length:</b> Variable depending on the number of multiple component transform stages used.
 */
public class MCO extends AbstractMarkerSegment {

  /**
   * Length of marker segment in bytes (not including the marker). The length is given by the following expression:
   * <i>Lmco = 3 + Nmco</i>.
   */
  public int Lmco;

  /**
   * Number of multiple component transform stages specified for inverse transform processing. If <i>{@link #Nmco} =
   * 0</i>, then no multiple component transform processing is used for the current tile and no {@link
   * #Imco}<sup>i</sup> parameters shall appear. Otherwise, {@link #Nmco} specifies the number of {@link MCC} marker
   * segment identifiers that will follow.
   */
  public int Nmco;

  /**
   * Index of the {@link MCC} marker segment containing the component collection information for the ith inverse
   * multiple component transform stage (see <i>ITU-T.801, A.3.8</i>).
   */
  public byte[] Imco;

  @Override
  public Marker getMarker() {
    return Marker.MCO;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException, JPEG2000Exception {
    Lmco = source.readUnsignedShort();
    Nmco = source.readUnsignedByte();

    Imco = new byte[Nmco];
    source.read(Imco);
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lmco);
    sink.writeByte(Nmco);
    sink.write(Imco);
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    Validate.exact("Lmco", Lmco, 3 + Nmco);
    Validate.inRange("Nmco", Nmco, 0, 255);
    for (int i = 0; i < Imco.length; i++) {
      Validate.inRange("Imco[" + i + "]", Imco[i], 0, 255);
    }
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lmco", Lmco, "MCO.L"));
    markerInfo.add(new PropertiesParameterInfo("Nmco", Nmco, "MCO.N"));

    if (Nmco > 0) {
      markerInfo.add(new PropertiesParameterInfo("Imco", "MCO.I"));
      for (int i = 0; i < Imco.length; i++) {
        markerInfo.add(new DirectParameterInfo("->[" + i + "]", Imco[i]));
      }
    }
  }
}
