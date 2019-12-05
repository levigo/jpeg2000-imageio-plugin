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
 * Defined in <i>ITU-T.801, A.3.11</i>
 * <p>
 * <b>Function:</b> Describes the quantization default used for compressing all components of a particular resolution
 * level and precinct. The parameter values can be overridden for an individual component, resolution level, and
 * precinct by a {@link QPC} marker segment which, if present, must appear in a tile-part header prior to any packets
 * for that component, resolution level, and precinct.
 * <p>
 * <b>Usage:</b> Main and any tile-part header. Several {@link QPD} marker segments may appear in any tile-part header,
 * but only one for each resolution level and precinct. If a {@link QPD} is used in a tile-part header it overrides the
 * quantization characteristics defined by either {@link QCD} or {@link QCC} marker segments for all components of the
 * resolution level and precinct indexed by the {@link QPD} within the scope of the particular tile. Thus, the
 * quantization characteristics of a particular resolution level, precinct pair is determined by the presence of {@link
 * QCD}, {@link QCC}, {@link QPD} or {@link QPC} markers in the following order of precedence:
 * <p>
 * <code>Any tile-part QPC > Any tile-part QPD > First tile-part QCC > First tile-part QCD > Main QPC > Main QPD > Main
 * QCC > Main QCD</code>
 * <p>
 * When {@link QPD} marker segments are used, they must appear in tile-part headers before any packets are found for
 * the indexed resolution level and precinct.
 * <p>
 * <b>Length:</b> Variable depending on the number of quantized sub-bands within the resolution level indexed.
 */
public class QPD extends Qxx {

  private static final QualifiedLogger LOGGER = LoggerFactory.getQualifiedLogger(QPD.class);

  /**
   * Length of marker segment in bytes (not including the marker).
   */
  public int Lqpd;

  /**
   * The resolution level index for the quantization values signaled. <i>ITU-T.801, Equation A-9</i> shows how this
   * marker segment is constructed based on the resolution level index, <i>lev</i>, as well as the precinct index,
   * <i>prec</i>.
   * <p>
   * The resolution level index, <i>lev</i>, can range from 0 to <i>N<sub>L</sub></i>, where <i>N<sub>L</sub></i> is
   * the number of decomposition levels defined in <i>ITU-T.800, A.6.1</i>.
   */
  public int PLqpd;

  /**
   * The precinct index for the quantization values signaled. The size of this marker segment parameter will be one
   * byte when the {@link #PLqpd} parameter is less than 128, but two bytes when {@link #PLqpd} is greater than or
   * equal to 128. This parameter will then just hold the precinct index, <i>prec</i>. The precinct index, <i>prec</i>,
   * can range from 0 to <i>numprecincts –1</i>, where <i>numprecincts</i> is the number of precincts at resolution
   * level lev and is also defined in <i>ITU-T.800, B.6</i>.
   */
  public int PPqpd;

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lqpd = source.readUnsignedShort();
    PLqpd = source.readUnsignedShort();

    if (PLqpd < 128) {
      PPqpd = source.readUnsignedByte();
    } else {
      PPqpd = source.readUnsignedShort();
    }

    Sqxx_guardbits = (byte) (source.readBits(3) & 0x7);
    Sqxx_style = (byte) (source.readBits(5) & 0x1F);

    int numBytes = Lqpd - 3;

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
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    throw new UnsupportedOperationException("Currently not yet implemented.");
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    throw new UnsupportedOperationException("Currently not yet implemented.");
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lqpd", Lqpd, "QPx.L"));
    markerInfo.add(new PropertiesParameterInfo("PLqpd", PLqpd, "QPx.PL"));
    markerInfo.add(new PropertiesParameterInfo("PPqpd", PPqpd, "QPx.PP"));
    markerInfo.add(new PropertiesParameterInfo("Sqpd_guardbits", Sqxx_guardbits, "Qxx.S_guardbits"));
    markerInfo.add(new PropertiesParameterInfo("Sqpd_style", Sqxx_style, "QPx.S_style"));

    markerInfo.add(new PropertiesParameterInfo("SPqpd_dzone", "Qxx.SP_deadzone"));
    for (int i = 0; i < SPqxx_dzone.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", SPqxx_dzone[i]));
    }

    markerInfo.add(new PropertiesParameterInfo("SPqpd_exp", "Qxx.SP_exp"));
    for (int i = 0; i < SPqxx_exp.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", SPqxx_exp[i]));
    }

    markerInfo.add(new PropertiesParameterInfo("SPqpd_man", "Qxx.SP_man"));
    for (int i = 0; i < SPqxx_man.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", SPqxx_man[i]));
    }
  }
}
