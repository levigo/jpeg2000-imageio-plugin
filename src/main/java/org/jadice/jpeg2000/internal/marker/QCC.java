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

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.param.DirectParameterInfo;
import org.jadice.jpeg2000.internal.param.PropertiesParameterInfo;
import org.jadice.jpeg2000.msg.CodestreamMessages;
import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Defined in <i>ITU-T.800, A.6.5</i>.
 * <p>
 * <b>Function:</b> Describes the quantization used for compressing a particular component.
 * <p>
 * <b>Usage:</b> Main and first tile-part header of a given tile. Optional in both the main and tile-part headers. No
 * more than one per any given component may be present in either the main or tile-part headers. If there are multiple
 * tile-parts in a tile, and this marker segment is present, it shall be found only in the first tile-part
 * ({@link SOT#TPsot} = 0). Optional in both the main and tile-part headers. When used in the main header, it overrides
 * the main {@link QCD} marker segment for the specific component. When used in the tile-part header, it overrides the
 * main {@link QCD}, main {@link QCC}, and tile {@link QCD} for the specific component. Thus, the order of precedence
 * is the following:
 * <p>
 * <code>Tile-part QCC > Tile-part QCD > Main QCC > Main QCD</code>
 * <p>
 * where the "greater than" sign, >, means that the greater overrides the lessor marker segment.
 * <p>
 * <b>Length:</b> Variable depending on the number of quantized elements.
 */
public class QCC extends Qxx {

  /**
   * Length of marker segment in bytes (not including the marker).
   * <p>
   * <b>Note:</b> The {@link #Lqcc} can be used to determine how many step sizes are present in the marker segment.
   * However, there is not necessarily a correspondence with the number of sub-bands present because the sub-bands can
   * be truncated with no requirement to correct this marker segment.
   */
  public int Lqcc;

  /**
   * The index of the component to which this marker segment relates. The components are indexed 0, 1, 2, etc. (either
   * 8 or 16 bits depending on {@link SIZ#Csiz} value).
   */
  public int Cqcc;

  @Override
  public Marker getMarker() {
    return Marker.QCC;
  }

  @Override
  public MarkerKey getMarkerKey() {
    return new MarkerKey(getMarker(), Cqcc);
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    final int numComps = codestream.numComps;
    
    Lqcc = source.readUnsignedShort();

    int remainingBytes = Lqcc - 3;
    if(numComps < 257) {
      Cqcc = source.readUnsignedByte();
    } else {
      Cqcc = source.readUnsignedShort();
      remainingBytes--;
    }
      
    Sqxx_guardbits = (byte) (source.readBits(3) & 0x7);
    Sqxx_style = (byte) (source.readBits(5) & 0x1F);
    remainingBytes--;
    

    switch (Sqxx_style){
      case VALUE_NO_QUANTIZATION:
        int numLoops = remainingBytes;
        SPqxx_exp = new int[numLoops];
        for (int b = 0; b < numLoops; b++) {
          SPqxx_exp[b] = readExponent(source);
          source.readBits(3); // the rest of each byte are filled with unused bits
        }
        break;

      case VALUE_SCALAR_DERIVED:
      case VALUE_SCALAR_EXPOUNDED:
      case VALUE_TRELLIS_DERIVED:
      case VALUE_TRELLIS_EXPOUNDED:
        numLoops = remainingBytes >> 1; // equivalent to a division by 2
        SPqxx_exp = new int[numLoops];
        SPqxx_man = new int[numLoops];

        for (int b = 0; b < numLoops; b++) {
          SPqxx_exp[b] = readExponent(source);
          SPqxx_man[b] = readMantissa(source);
        }
        break;
      case VALUE_DEADZONE_AND_SCALAR_DERIVED:
      case VALUE_DEADZONE_AND_SCALAR_EXPOUNDED:
        numLoops = remainingBytes >> 2; // equivalent to a division by 4
        SPqxx_dzone = new int[numLoops];
        SPqxx_exp = new int[numLoops];
        SPqxx_man = new int[numLoops];

        for (int b = 0; b < numLoops; b++) {
          SPqxx_dzone[b] = source.readUnsignedShort();
          SPqxx_exp[b] = readExponent(source);
          SPqxx_man[b] = readMantissa(source);
        }
        break;
      case VALUE_DEADZONE_DERIVED_AND_SCALAR_EXPOUNDED:
        numLoops = (remainingBytes - 2) >> 1;
        SPqxx_dzone = new int[1];
        SPqxx_exp = new int[numLoops];
        SPqxx_man = new int[numLoops];

        SPqxx_dzone[0] = source.readUnsignedShort();

        for (int b = 0; b < numLoops; b++) {
          SPqxx_exp[b] = readExponent(source);
          SPqxx_man[b] = readMantissa(source);
        }
        break;
      default:
        final QualifiedLogger LOG = LoggerFactory.getQualifiedLogger(QCC.class);
        LOG.warn(CodestreamMessages.MARKER_SEGMENT_FORMAT_FOR_QCC_UNKNOWN);

        LOG.warn(CodestreamMessages.SKIPPING_MARKER_SEGMENT_BYTES, remainingBytes);
        source.skipBytes(remainingBytes);
    }
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
    markerInfo.add(new PropertiesParameterInfo("Lqcc", Lqcc, "QCC.L"));
    markerInfo.add(new PropertiesParameterInfo("Cqcc", Cqcc, "QxC.C"));
    markerInfo.add(new PropertiesParameterInfo("Guardbits (Sqcc)", Sqxx_guardbits, "Qxx.S.guardbits"));
    markerInfo.add(new PropertiesParameterInfo("Style (Sqcc)", Sqxx_style, "QxC.S.style"));

    if (SPqxx_dzone != null) {
      markerInfo.add(new PropertiesParameterInfo("DZone (SPqcc)", "Qxx.SP.deadzone"));
      for (int b = 0; b < SPqxx_dzone.length; b++) {
        markerInfo.add(new DirectParameterInfo("->[" + b + "]", SPqxx_dzone[b]));
      }
    }

    if (SPqxx_exp != null) {
      markerInfo.add(new PropertiesParameterInfo("Exponent (SPqcc)", SPqxx_man == null ? "Qxx.SP.exp0" : "Qxx.SP.exp"));
      for (int b = 0; b < SPqxx_exp.length; b++) {
        markerInfo.add(new DirectParameterInfo("->[" + b + "]", SPqxx_exp[b]));
      }
    }

    if (SPqxx_man != null) {
      markerInfo.add(new PropertiesParameterInfo("Mantissa (SPqcc)", "Qxx.SP.man"));
      for (int b = 0; b < SPqxx_man.length; b++) {
        markerInfo.add(new DirectParameterInfo("->[" + b + "]", SPqxx_man[b]));
      }
    }
  }
}
