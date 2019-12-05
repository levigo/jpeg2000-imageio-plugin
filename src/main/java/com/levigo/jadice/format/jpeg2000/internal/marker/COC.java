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
import com.levigo.jadice.format.jpeg2000.internal.codestream.Capability;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.param.DirectParameterInfo;
import com.levigo.jadice.format.jpeg2000.internal.param.Parameters;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Defined in <i>ITU-T.800, A.6.2</i>
 * <p>
 * <b>Function:</b> Describes the coding style and number of decomposition levels used for compressing a particular
 * component.
 * <p>
 * <b>Usage:</b> Main and first tile-part header of a given tile. Optional in both the main and tile-part headers. No
 * more than one per any given component may be present in either the main or tile-part headers. If there are multiple
 * tile-parts in a tile, and this marker segment is present, it shall be found only in the first tile-part
 * (<code>{@link SOT#TPsot}</code> = 0). When used in the main header, it overrides the main {@link COD} marker segment
 * for the specific component. When used in the tile-part header, it overrides the main {@link COD}, main {@link COC},
 * and tile {@link COD} for the specific component. Thus, the order of precedence is the following:
 * <p>
 * <code>Tile-part COC > Tile-part COD > Main COC > Main COD</code>
 * <p>
 * where the "greater than" sign, <code>></code>, means that the greater overrides the lessor marker segment.
 * <p>
 * <b>Length:</b> Variable depending on the value of {@link #Scoc}.
 */
public class COC extends COx {

  /**
   * Length of marker segment in bytes (not including the marker). The value of this parameter is determined by
   * equation <i>ITU-T.800, (A-3)</i>.
   */
  public int Lcoc;

  /**
   * The index of the component to which this marker segment relates. The components are indexed 0, 1, 2, etc.
   */
  public int Ccoc;

  /**
   * Coding style for this component. <i>ITU-T.800, Table A.23</i> shows the value for each {@link #Scoc} parameter.
   * <p>
   * This parameter defines only precinct size information. Either maximum precinct values <code>PPx = PPy = 15</code>
   * or user-defined values ({@link #SP_precincts}) are used.
   * <p>
   * To access flags use integer masks with the '<code>MASK_CODING_</code>' prefix defined in super class {@link COx}.
   */
  public int Scoc;


  /** SSO overlap values. */
  public int SPcoc_sso;

  private boolean extended;

  @Override
  public Marker getMarker() {
    return Marker.COC;
  }

  @Override
  public MarkerKey getMarkerKey() {
    return new MarkerKey(getMarker(), Ccoc);
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lcoc = source.readUnsignedShort();
    Ccoc = codestream.numComps < 257 ? source.readUnsignedByte() : source.readUnsignedShort();
    Scoc = source.readUnsignedByte();
    SP_NL = source.readUnsignedByte();
    SP_xcb = source.readUnsignedByte() + 2;
    SP_ycb = source.readUnsignedByte() + 2;
    SP_modes = source.readUnsignedByte();
    SP_kernel = source.readUnsignedByte();

    extended = Capability.T801_SINGLE_SAMPLE_OVERLAP.isUsedBy(codestream);

    if (extended) {
      SPcoc_sso = source.readUnsignedByte();
    }

    if (Parameters.isSet(Scoc, MASK_CODING_USER_PRECINCTS)) {
      final int numParams = SP_NL + 1;
      SP_precincts = new int[numParams];
      for (int i = 0; i < numParams; i++) {
        SP_precincts[i] = source.readUnsignedByte();
      }
    }
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lcoc);

    if (codestream.numComps < 257) {
      sink.writeByte(Ccoc);
    } else {
      sink.writeShort(Ccoc);
    }

    sink.writeByte(Scoc);
    sink.writeByte(SP_NL);
    sink.writeByte(SP_xcb);
    sink.writeByte(SP_ycb);
    sink.writeByte(SP_modes);
    sink.writeByte(SP_kernel);

    if (extended) {
      sink.writeByte(SPcoc_sso);
    }

    if (SP_precincts != null && Parameters.isSet(Scoc, MASK_CODING_USER_PRECINCTS)) {
      for (int precinct : SP_precincts) {
        sink.writeByte(precinct);
      }
    }
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    // TODO
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lcoc", Lcoc, "COC.L"));
    markerInfo.add(new PropertiesParameterInfo("Ccoc", Ccoc, "COC.C"));
    markerInfo.add(new PropertiesParameterInfo("Scoc", Integer.toBinaryString(Scoc), "COC.S"));
    markerInfo.add(new PropertiesParameterInfo("NL (SPcoc)", SP_NL, "COC.SP.NL"));
    markerInfo.add(new PropertiesParameterInfo("xcb (SPcoc)", SP_xcb, "COC.SP.xcb"));
    markerInfo.add(new PropertiesParameterInfo("ycb (SPcoc)", SP_ycb, "COC.SP.ycb"));
    markerInfo.add(new PropertiesParameterInfo("Modes (SPcoc)", SP_modes, "COC.SP.modes"));
    markerInfo.add(new PropertiesParameterInfo("Kernel (SPcoc)", SP_kernel, "COC.SP.kernel"));

    if (extended) {
      markerInfo.add(new PropertiesParameterInfo("SSO (SPcoc)", SPcoc_sso, "COC.SP.sso"));
    }

    if (SP_precincts != null) {
      markerInfo.add(new PropertiesParameterInfo(" Precincts (SPcoc)", "COC.SP.precincts"));
      for (int i = 0; i < SP_precincts.length; i++) {
        final String precinctSize = Integer.toBinaryString(SP_precincts[i]);
        markerInfo.add(new DirectParameterInfo("->[" + i + "]", precinctSize));
      }
    }
  }
}
