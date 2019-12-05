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
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;
import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Defined in <i>ITU-T.801, A.3.12</i>
 * <p>
 * <b>Function:</b> Describes the quantization used for compressing a particular component, resolution level, and
 * precinct.
 * <p>
 * <b>Usage:</b> Main and any tile-part header. Several {@link QPC} marker segments may appear in any tile-part header,
 * but only one for each component, resolution level, and precinct. If a {@link QPC} is used in a tile-part header it
 * overrides the quantization characteristics defined by {@link QCD}, {@link QCC}, or {@link QPD} marker segments for
 * the triplet indexed by the {@link QPC} within the scope of the particular tile. Thus, the quantization
 * characteristics of a particular component, resolution level, and precinct is determined by the presence of {@link
 * QCD}, {@link QCC}, {@link QPD} or {@link QPC} markers in the following order of precedence:
 * <p>
 * <code>Any tile-part QPC > Any tile-part QPD > First tile-part QCC > First tile-part QCD > Main QPC > Main QPD > Main
 * QCC > Main QCD</code>
 * <p>
 * When {@link QPC} marker segments are used, they must appear in tile-part headers before any packets are found for
 * the indexed component, resolution level, and precinct.
 * <p>
 * <b>Length:</b> Variable depending on the number of quantized sub-bands within the resolution level indexed.
 */
public class QPC extends Qxx {

  private static final QualifiedLogger LOGGER = LoggerFactory.getQualifiedLogger(QPC.class);

  /**
   * Length of marker segment in bytes (not including the marker).
   */
  public int Lqpc;

  /**
   * The index of the component to which this marker segment relates. The components are indexed 0, 1, 2, etc. (Either
   * 8 or 16 bits depending on {@link SIZ#Csiz} value.)
   */
  public int Cqpc;

  /**
   * The resolution level index for the quantization values signalled. <i>ITU-T.800, Equation A-11</i> shows how this
   * marker segment is constructed based on the resolution level index, lev, as well as the precinct index,
   * <i>prec</i>.
   * <p>
   * The resolution level index, <i>lev</i>, can range from 0 to <i>N<sub>L</sub></i>, where <i>N<sub>L</sub></i> is
   * the number of decomposition levels defined in <i>ITU-T.800, A.6.1</i>.
   */
  public int PLqpc;

  /**
   * The precinct index for the quantization values signaled. The size of this marker segment parameter will be one
   * byte when the {@link #PLqpc} parameter is less than 128, but two bytes when {@link #PLqpc} is greater than or
   * equal to 128. This parameter will then just hold the precinct index, <i>prec</i>. The precinct index, <i>prec</i>,
   * can range from 0 to <i>numprecincts –1</i>, where <i>numprecincts</i> is the number of precincts at resolution
   * level lev and is also defined in <i>ITU-T.800, B.6</i>.
   */
  public int PPqpc;

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lqpc = source.readUnsignedShort();

    if (codestream.numComps < 257) {
      Cqpc = source.readUnsignedByte();
    } else {
      Cqpc = source.readUnsignedShort();
    }

    PLqpc = source.readUnsignedShort();

    if (PLqpc < 128) {
      PPqpc = source.readUnsignedByte();
    } else {
      PPqpc = source.readUnsignedShort();
    }

    Sqxx_guardbits = (byte) (source.readBits(3) & 0x7);
    Sqxx_style = (byte) (source.readBits(5) & 0x1F);

    int numBytes = Lqpc - 3;

    switch (Sqxx_style){
      case VALUE_NO_QUANTIZATION:
        SPqxx_exp = new int[numBytes];

        for (int b = 0; b < numBytes; b++) {
          SPqxx_exp[b] = readExponent(source);
          source.readBits(3); // the rest of each byte are filled with unused bits
        }
        break;
      case VALUE_SCALAR_DERIVED:
      case VALUE_SCALAR_EXPOUNDED:
      case VALUE_TRELLIS_DERIVED:
      case VALUE_TRELLIS_EXPOUNDED:
        int numShorts = numBytes >> 1; // equivalent to '/2'
        SPqxx_exp = new int[numShorts];
        SPqxx_man = new int[numShorts];

        for (int b = 0; b < numShorts; b++) {
          SPqxx_exp[b] = readExponent(source);
          SPqxx_man[b] = readMantissa(source);
        }
        break;

      case VALUE_DEADZONE_AND_SCALAR_DERIVED:
      case VALUE_DEADZONE_AND_SCALAR_EXPOUNDED:
        int numInts = numBytes >> 2; // equivalent to '/4'
        SPqxx_dzone = new int[numInts];
        SPqxx_exp = new int[numInts];
        SPqxx_man = new int[numInts];

        for (int b = 0; b < numInts; b++) {
          SPqxx_dzone[b] = source.readUnsignedShort();
          SPqxx_exp[b] = readExponent(source);
          SPqxx_man[b] = readMantissa(source);
        }

        break;

      case VALUE_DEADZONE_DERIVED_AND_SCALAR_EXPOUNDED:
        numShorts = (numBytes - 2) >> 1;

        SPqxx_dzone = new int[1];
        SPqxx_exp = new int[numShorts];
        SPqxx_man = new int[numShorts];

        SPqxx_dzone[0] = source.readUnsignedShort();

        for (int b = 0; b < numShorts; b++) {
          SPqxx_exp[b] = readExponent(source);
          SPqxx_man[b] = readMantissa(source);
        }
        break;

      default:
        LOGGER.warn(CodestreamMessages.MARKER_SEGMENT_FORMAT_FOR_QCD_UNKNOWN);
        LOGGER.warn(CodestreamMessages.SKIPPING_MARKER_SEGMENT_BYTES, numBytes);
        source.skipBytes(numBytes);
        break;
    }

  }

  @Override
  public Marker getMarker() {
    return Marker.QPD;
  }

  @Override
  public MarkerKey getMarkerKey() {
    return new MarkerKey(getMarker(), Cqpc);
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    throw new UnsupportedOperationException("Currently not yet implemented.");
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    // TODO
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lqpc", Lqpc, "QPx.L"));
    markerInfo.add(new PropertiesParameterInfo("Cqpc", Cqpc, "QxC.C"));
    markerInfo.add(new PropertiesParameterInfo("PLqpc", PLqpc, "QPx.PL"));
    markerInfo.add(new PropertiesParameterInfo("PPqpc", PPqpc, "QPx.PP"));
    markerInfo.add(new PropertiesParameterInfo("Sqpc_guardbits", Sqxx_guardbits, "Qxx.S_guardbits"));
    markerInfo.add(new PropertiesParameterInfo("Sqpc_style", Sqxx_style, "QPx.S_style"));

    markerInfo.add(new PropertiesParameterInfo("SPqpc_dzone", "Qxx.SP_deadzone"));
    for (int i = 0; i < SPqxx_dzone.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", SPqxx_dzone[i]));
    }

    markerInfo.add(new PropertiesParameterInfo("SPqpc_exp", "Qxx.SP_exp"));
    for (int i = 0; i < SPqxx_exp.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", SPqxx_exp[i]));
    }

    markerInfo.add(new PropertiesParameterInfo("SPqpc_man", "Qxx.SP_man"));
    for (int i = 0; i < SPqxx_man.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", SPqxx_man[i]));
    }
  }
}
