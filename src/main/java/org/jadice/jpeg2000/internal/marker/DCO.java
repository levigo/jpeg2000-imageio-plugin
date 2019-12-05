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
import org.jadice.jpeg2000.internal.param.DirectParameterInfo;
import org.jadice.jpeg2000.internal.param.PropertiesParameterInfo;

import java.io.IOException;

/**
 * Defined in <i>ITU-T.801, A.3.1</i>
 * <p>
 * <b>Function:</b> Describes the variable DC offset for every component.
 * <p>
 * <b>Usage:</b> Present only if the variable DC offset capability bit in the {@link
 * org.jadice.jpeg2000.internal.marker.SIZ#Rsiz} parameter (see <i>ITU-T.801, A.2.1</i>) is a one value.
 * Main and first tile-part header of a given tile. Optional in both the main and tile-part headers. No more than one
 * shall appear in any header. If present in the main header, it describes the variable DC offset for every component
 * in every tile. If present in the first tile-part header of a given tile, it describes the variable DC offset for
 * every component in that tile only. When used in both the main header and the first tile-part header, the {@link DCO}
 * in the first tile part header overrides the main for that tile. Thus the order of precedence is the following:
 * <p>
 * <code>Tile-part DCO > Main DCO</code>
 * <p>
 * where the "greater than" sign, <code>></code>, means that the greater overrides the lesser marker segment. Shall not
 * be used with the multiple component transform.
 * <p>
 * <b>Length:</b> Variable depending on the number of components.
 * <p>
 * <i>NOTE</i> – If {@link #Ldco} were to be larger than 65535, then the {@link DCO} marker segment cannot be used.
 * Instead multiple component transformation functionality could be used.
 */
public class DCO extends AbstractMarkerSegment {

  /** Possible value in offset type definition that defines 8-bit <b>unsigned</b> integer offsets. */
  public static final int VALUE_8_BIT_UNSIGNED_INTEGER = 0;

  /** Possible value in offset type definition that defines 16-bit <b>signed</b> integer offsets. */
  public static final int VALUE_16_BIT_SIGNED_INTEGER = 1;

  /**
   * Possible value in offset type definition that defines 32-bit floating point offsets (<i>IEEE Std. 754-1985
   * R1990</i>)
   */
  public static final int VALUE_32_BIT_FLOATING_POINT = 2;

  /**
   * Possible value in offset type definition that defines 64-bit floating point offsets (<i>IEEE Std. 754-1985
   * R1990</i>)
   */
  public static final int VALUE_64_BIT_FLOATING_POINT = 3;

  /**
   * Length of marker segment in bytes (not including the marker). The value of this parameter is determined by
   * equation <i>ITU-T.800, (A-1)</i>.
   */
  public int Ldco;

  /**
   * Variable DC offset type definition. Possible values are defined in <i>ITU-T.800, Table A.21</i>:
   * <ul>
   * <li>{@link #VALUE_8_BIT_UNSIGNED_INTEGER} = {@value #VALUE_8_BIT_UNSIGNED_INTEGER} (<code>0000 0000</code>)</li>
   * <li>{@link #VALUE_16_BIT_SIGNED_INTEGER} = {@value #VALUE_16_BIT_SIGNED_INTEGER} (<code>0000 0001</code>)</li>
   * <li>{@link #VALUE_32_BIT_FLOATING_POINT} = {@value #VALUE_32_BIT_FLOATING_POINT} (<code>0000 0010</code>)</li>
   * <li>{@link #VALUE_64_BIT_FLOATING_POINT} = {@value #VALUE_64_BIT_FLOATING_POINT} (<code>0000 0011</code>)</li>
   * </ul>
   */
  public int Sdco;

  /** Variable DC offset for the ith component. There is one SPdco parameter for every component in the image. */
  public Number[] SPdco;

  @Override
  public Marker getMarker() {
    return Marker.DCO;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Ldco = source.readUnsignedShort();
    Sdco = source.readUnsignedByte();

    SPdco = new Number[codestream.numComps];
    switch (Sdco){
      case VALUE_8_BIT_UNSIGNED_INTEGER:
        for (int compIdx = 0; compIdx < SPdco.length; compIdx++) {
          SPdco[compIdx] = source.readUnsignedByte();
        }
        break;

      case VALUE_16_BIT_SIGNED_INTEGER:
        for (int compIdx = 0; compIdx < SPdco.length; compIdx++) {
          SPdco[compIdx] = source.readInt();
        }
        break;

      case VALUE_32_BIT_FLOATING_POINT:
        for (int compIdx = 0; compIdx < SPdco.length; compIdx++) {
          SPdco[compIdx] = source.readFloat();
        }
        break;

      case VALUE_64_BIT_FLOATING_POINT:
        for (int compIdx = 0; compIdx < SPdco.length; compIdx++) {
          SPdco[compIdx] = source.readDouble();
        }
        break;
    }
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Ldco);
    sink.writeByte(Sdco);
    switch (Sdco){
      case VALUE_8_BIT_UNSIGNED_INTEGER:
        for (Number number : SPdco) {
          sink.writeByte(number.intValue());
        }
        break;

      case VALUE_16_BIT_SIGNED_INTEGER:
        for (Number number : SPdco) {
          sink.writeShort(number.intValue());
        }
        break;

      case VALUE_32_BIT_FLOATING_POINT:
        for (Number number : SPdco) {
          sink.writeFloat((Float) number); // Utilizes the correct IEEE 754 conversion internally.
        }
        break;
      case VALUE_64_BIT_FLOATING_POINT:
        for (Number number : SPdco) {
          sink.writeDouble((Double) number); // Utilizes the correct IEEE 754 conversion internally.
        }
        break;
    }
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    // TODO
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Ldco", Ldco, "DCO.L"));
    markerInfo.add(new PropertiesParameterInfo("Sdco", Sdco, "DCO.S"));
    markerInfo.add(new PropertiesParameterInfo("SPdco", "DCO.SP"));
    for (int i = 0; i < SPdco.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", SPdco[i]));
    }
  }
}
