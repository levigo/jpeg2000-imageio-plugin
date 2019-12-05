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
import org.jadice.jpeg2000.internal.param.Parameters;
import org.jadice.jpeg2000.internal.param.PropertiesParameterInfo;

import java.io.IOException;

/**
 * Defined in <i>ITU-T.801, A.3.6</i>
 * <p>
 * <b>Function:</b> Defines the bit depth of reconstructed image components coming out of any multiple component
 * transformation process.
 * <p>
 * <b>Usage:</b> Present only if the multiple component transformation capability bit in the {@link SIZ#Rsiz} parameter
 * (see <i>ITU-T.801, A.2.1</i>) is a one value. Main header. The {@link CBD} marker segment is required if the
 * multiple component transformation processes are used. At most there can be one {@link CBD} in the main header. The
 * presence of a {@link CBD} marker segment in a codestream alters the procedures used to determine the precision of
 * output image components and the interpretation of the {@link SIZ} marker. See <i>ITU-T.801, Annex J</i> for further
 * details.
 * <p>
 * <b>Length:</b> Variable depending on the number of reconstructed image component bit depths signalled.
 */
public class CBD extends AbstractMarkerSegment {

  /**
   * <b>Mask</b> (<code>x111 1111 1111 1111</code>) for determining the number of reconstructed
   * image components (1-16384). Masked values can be used directly. Applies to {@link CBD#Ncbd}.
   */
  public static final int MASK_NUM_BIT_DEPTHS = 0x7FFF;

  /**
   * <b>Mask</b> (<code>1xxx xxxx xxxx xxxx</code>) for determining if bit depths are included per
   * reconstructed image component or one for all. Applies to {@link CBD#Ncbd}.<br>
   * <b>Values:</b><br>
   * <code>0xxx xxxx xxxx xxxx</code>: Bit depths included, one per reconstructed image component.<br>
   * <code>1xxx xxxx xxxx xxxx</code>: One bit depth included, applies to all reconstructed image
   * components.
   */
  public static final int MASK_BIT_DEPTHS_INCLUDED = 0x8000;

  /**
   * <b>Mask</b> (<code>x111 1111</code>) for determining the <i>component sample bit depth = value
   * + 1</i>. From 1 bit deep through 38 bits deep respectively. Masked values can be used directly.
   * Applies to {@link CBD#BDcbd}.
   */
  public static final int MASK_BIT_DEPTH = 0x7F;

  /**
   * <b>Mask</b> (<code>1xxx xxxx</code>) for determining the sign of the sample values. Applies to
   * {@link CBD#BDcbd}.<br>
   * <b>Values:</b><br>
   * <code>0xxx xxxx</code>: Component sample values are unsigned values<br>
   * <code>1xxx xxxx</code>: Component sample values are signed values
   */
  public static final int MASK_IS_SIGNED = 0x80;

  /**
   * Length of marker segment in bytes (not including the marker).
   */
  public int Lcbd;

  /**
   * Number of component bit depths included in marker segment. <i>ITU-T.801, Table A.30</i> shows the value for the
   * {@link #Ncbd} parameter.
   */
  public int Ncbd;

  /**
   * Bit depth and sign of the reconstructed image components in the order in which they are created as determined by
   * the {@link MCC} and {@link MCO} marker segments. Either one value is signalled for all components (see
   * <i>ITU-T.801, Table A.30</i>) or an individual bit depth is given for each component.
   */
  public byte[] BDcbd;

  @Override
  public Marker getMarker() {
    return Marker.CBD;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lcbd = source.readUnsignedShort();
    Ncbd = source.readUnsignedShort();

    if (Parameters.isSet(Ncbd, MASK_BIT_DEPTHS_INCLUDED)) {
      BDcbd = new byte[1];
    } else {
      final int numParams = Parameters.extract(Ncbd, MASK_NUM_BIT_DEPTHS);
      BDcbd = new byte[numParams];
    }

    source.readFully(BDcbd);
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lcbd);
    sink.writeShort(Ncbd);
    sink.write(BDcbd);
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    final boolean oneBitDepthIncluded = Parameters.isSet(Ncbd, MASK_BIT_DEPTHS_INCLUDED);
    final int expectedBitDepths = oneBitDepthIncluded ? 1 : Parameters.extract(Ncbd, MASK_NUM_BIT_DEPTHS);

    Validate.exact("Lcbd", Lcbd, 4 + expectedBitDepths);
    Validate.exact("Ncbd number", Ncbd, expectedBitDepths);
    Validate.exact("BDcbd number", BDcbd.length, expectedBitDepths);

    for (int i = 0; i < expectedBitDepths; i++) {
      Validate.inRange("BDcbd[" + i + "]", BDcbd[i], 0, 37);
    }
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lcbd", Lcbd, "CBD.L"));
    markerInfo.add(new PropertiesParameterInfo("Ncbd", Ncbd, "CBD.N"));
    markerInfo.add(new PropertiesParameterInfo("BDcbd", "CBD.BD"));
    for (int i = 0; i < BDcbd.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", BDcbd[i]));
    }
  }
}
