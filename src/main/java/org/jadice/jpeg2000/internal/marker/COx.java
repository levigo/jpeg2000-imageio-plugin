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

import static org.jadice.jpeg2000.internal.marker.Marker.COC;
import static org.jadice.jpeg2000.internal.marker.Marker.COD;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.codestream.MarkerAccessGraph;
import org.jadice.jpeg2000.internal.codestream.MarkerAccessNode;
import org.jadice.jpeg2000.internal.codestream.MarkerSegmentContainer;
import org.jadice.jpeg2000.internal.image.Tile;
import org.jadice.jpeg2000.msg.CodestreamMessages;

public abstract class COx extends AbstractMarkerSegment {

  /**
   * Default value for <i>PPx</i> and <i>PPy</i>, as mentioned in <i>ITU-T.800, Table A.13</i> and <i>ITU-T.800, Table
   * A.23</i>. Used if no user-defined values are signalled in a {@link COD} marker.
   */
  public static final int PP_DEFAULT = 15;

  /**
   * Masks the four least significant bits of the signalled precinct size value, as described in <i>ITU-T.800, Table
   * A.21</i>. The value will be the <i>PPx</i> value which is used to compute the horizontal precinct size via
   * <code>2<sup>PPx</sup></code>.
   * <p>
   * Mask: <code>xxxx 1111</code>
   */
  public static final int MASK_PPX = 0xF;

  /**
   * Masks the four most significant bits of the signalled precinct size value, as described in <i>ITU-T.800, Table
   * A.21</i>. The value will be the <i>PPy</i> value which is used to compute the vertical precinct size via
   * <code>2<sup>PPy</sup></code>. Use {@link #SHIFT_PPY} within a right shift to normalize the signalled value.
   * <p>
   * Mask: <code>1111 xxxx</code>
   */
  public static final int MASK_PPY = 0xF0;

  /**
   * @see #MASK_PPY
   */
  public static final int SHIFT_PPY = 4;

  /**
   * Value <code>0</code>: Entropy coder, precincts with <code>PPx = 15</code> and <code>PPy = 15</code>.
   * <p>
   * Value <code>1</code>: Entropy coder with precincts defined via {@link COx#SP_precincts}.
   * <p>
   * Also defined in {@link #MASK_CODING_USER_PRECINCTS}.
   */
  public static final int MASK_CODING_USER_PRECINCTS = 1 << 0;

  /**
   * This information as a part of the style parameter {@link COD#Scod} is only present in {@link COD} marker segments.
   * <p>
   * Value <code>0</code>: No SOP marker segments used.
   * <p>
   * Value <code>1</code>: SOP marker segments <b>may</b> be used.
   */
  public static final int MASK_CODING_SOP_MARKER = 1 << 1;

  /**
   * This information as a part of the style parameter {@link COD#Scod} is only present in {@link COD} marker segments.
   * <p>
   * Value <code>0</code>: No EPH marker used.
   * <p>
   * Value <code>1</code>: EPH marker <b>shall</b> be used.
   */
  public static final int MASK_CODING_EPH_MARKER = 1 << 2;

  /**
   * Bit 0: Offset in the horizontal dimension, <code>z<sub>x</sub>=0</code> (CBAP)
   * <p>
   * Bit 1: Offset in the horizontal dimension, <code>z<sub>x</sub>=1</code>
   * <p>
   * For direct use of the value, perform right-shift by {@value #SHIFT_HOR_CODING_ORIGIN}.
   *
   * @see #SHIFT_HOR_CODING_ORIGIN
   */
  public static final int MASK_HOR_CODING_ORIGIN = 1 << 3;

  /**
   * Offset in the vertical dimension, <code>z<sub>y</sub>=0</code> (CBAP)
   * <p>
   * Offset in the vertical dimension, <code>z<sub>y</sub>=1</code>.
   * <p>
   * For direct use of the value, perform right-shift by {@value #SHIFT_VERT_CODING_ORIGIN}.
   *
   * @see #SHIFT_VERT_CODING_ORIGIN
   */
  public static final int MASK_VERT_CODING_ORIGIN = 1 << 4;

  public static final int MASK_MODE_ARITHMETIC_BYPASS = 1 << 0;

  public static final int MASK_MODE_RESET_CONTEXT = 1 << 1;

  public static final int MASK_MODE_PASS_TERMINATION = 1 << 2;

  public static final int MASK_MODE_CAUSAL = 1 << 3;

  public static final int MASK_MODE_PREDICTABLE_TERMINATION = 1 << 4;

  public static final int MASK_MODE_SEGMENTATION_SYMBOLS = 1 << 5;

  /**
   * <b>Value <code>0</code>:</b> No multiple component transformation specified.
   * <p>
   * <b>Value <code>1</code>:</b> Component transformation used on components 0, 1, 2 for coding efficiency.
   * Irreversible component transformation used with irreversible filters. Reversible component transformation used
   * with reversible filters.
   */
  public static final int MASK_MCT_YCC = 1 << 0;

  /**
   * Array-based multiple component transformation is used if the flag is set. May be combined with wavelet-based
   * multiple component transform.
   */
  public static final int MASK_MCT_ARRAY_BASED = 1 << 1;

  /**
   * Wavelet-based multiple component transformation is used if the flag is set. May be combined with array-based
   * multiple component transform.
   */
  public static final int MASK_MCT_WAVELET_BASED = 1 << 2;

  /**
   * Mask for determining if single sample overlap is used.
   * <p>
   * Mask: <code>1xxx xxxx xxxx xxxx</code>
   * <p>
   * Bit: <code>0:=</code> SSO not used, <code>1:=</code> SSO used.
   */
  public static final int MASK_SSO_USED = 1 << 15;

  /**
   * Mask for determining if TSSO is used for every tile in the image (shall be the same in every COD marker segment).
   * <p>
   * Mask: <code>x1xx xxxx xxxx xxxx</code>
   * <p>
   * Bit: <code>0:=</code> TSSO not used, <code>1:=</code> TSSO used.
   */
  public static final int MASK_SSO_TSSO_USED = 1 << 14;

  /**
   * Mask for determining the value of <i>Vovlp</i> (shall be the same in every {@link COD} marker segment).
   * <p>
   * Mask: <code>xx1x xxxx xxxx xxxx</code>
   * <p>
   * Bit: <code>0:=</code> <i>Vvolp</i><code>=0</code>, <code>1:=</code> <i>Vovlp</i><code>=1</code>
   */
  public static final int MASK_SSO_VOVLP = 1 << 13;

  /**
   * Mask for determining the value of <i>Hovlp</i> (shall be the same in every {@link COD} marker segment).
   * <p>
   * Mask: <code>xxx1 xxxx xxxx xxxx</code>
   * <p>
   * Bit: <code>0:=</code> <i>Hvolp</i><code>=0</code>, <code>1:=</code> <i>Hovlp</i><code>=1</code>
   */
  public static final int MASK_SSO_HOVLP = 1 << 12;

  /**
   * Mask for determining if <i>TBDWT</i> is used (shall be the same in every {@link COD} marker segment).
   * <p>
   * <b>Note</b>: <i>TBDWT</i> can only be enabled if <i>TSSO</i> is used!
   * <p>
   * Mask: <code>xxxx 0xxx xxxx xxxx</code>
   * <p>
   * Bit:<code>0:=</code> <i>TBDWT</i> is not used, <code>1:=</code> <i>TBDWT</i> is used if <i>TSSO</i> is also
   * enabled.
   */
  public static final int MASK_SSO_TBDWT = 1 << 11;

  /**
   * Cell width exponent value, <code>XC=2<sup>value</sup></code>
   * <p>
   * Mask: <code>xxxx xxxx xxxx 1111</code>
   * <p>
   * Values: 0 to 15
   */
  public static final int MASK_SSO_CELL_WIDTH_EXPONENT = 0x000F;

  /**
   * Cell width exponent value, <code>YC=2<sup>value</sup></code>
   * <p>
   * Mask: <code>xxxx xxxx 1111 xxxx</code>
   * <p>
   * Values: 0 to 15 (masked value has to be right-shifted by 4)
   */
  public static final int MASK_SSO_CELL_HEIGHT_EXPONENT = 0x00F0;

  public static final int SHIFT_HOR_CODING_ORIGIN = 3;

  public static final int SHIFT_VERT_CODING_ORIGIN = 4;

  /**
   * Value that defines the use of 9-7 floating-point based irreversible filter.
   */
  public static final int VALUE_KERNEL_9_7 = 0;

  /**
   * Value that defines the use of 5-3 integer-based reversible filter.
   */
  public static final int VALUE_KERNEL_5_3 = 1;

  /**
   * Minimum allowed value for <code>SGcod layers</code>. See <i>ITU-T.800, Table A.14</i>.
   */
  public static final long MIN_LAYERS = 1;

  /**
   * Maximum allowed value for <code>SGcod layers</code>. See <i>ITU-T.800, Table A.14</i>.
   */
  public static final long MAX_LAYERS = 65535;

  /**
   * Number of decomposition levels, <i>N<sub>L</sub></i>. Zero implies no transformation. <i>ITU-T.801, A.2.3</i>
   * extends this parameters. See <i>ITU-T.801, Table A.9</i>.
   */
  public int SP_NL;

  /**
   * Transformation type. Following values are extracted:
   * <ul>
   * <li><code>0000 0000</code>: 9-7 irreversible wavelet transform as defined in ITU-T.800</li>
   * <li><code>0000 0001</code>: 5-3 reversible wavelet transform as defined in ITU-T.800</li>
   * <li><code>0000 0010</code> to <code>1111 1111</code>: Arbitrary transformation kernel definition index value
   * (2-255). Definitions are found in the appropriate {@link ATK} marker segment (see <i>ITU-T.801, A.3.5 </i>).</li>
   * </ul>
   */
  public int SP_kernel;

  /** Code-block width exponent offset value, <i>xcb</i>. See <i>ITU-T.800, Table A.18</i>. */
  public int SP_xcb;

  /** Code-block height exponent offset value, <i>ycb</i>. See <i>ITU-T.800, Table A.18</i>. */
  public int SP_ycb;

  /**
   * Style of the code-block coding passes. For details see <i>ITU-T.800, Table A.23</i>. To access flags use integer
   * masks with the '<code>MASK_MODE_</code>' prefix defined in {@link COx}.
   */
  public int SP_modes;

  /**
   * User-defined precincts.
   * <p>
   * Byte '<code>---- xxxx</code>': 4 LSBs are the precinct width exponent, <code>PPx = value</code>. This value may
   * only equal zero at the resolution level corresponding to the <i>N<sub>L</sub>LL</i> band.
   * <p>
   * Byte '<code>xxxx ----</code>': 4 MSBs are the precinct height exponent, <code>PPy = value</code>. This value may
   * only equal zero at the resolution level corresponding to the <i>N<sub>L</sub>LL</i> band.
   */
  public int[] SP_precincts;

  public static COD accessCOD(Codestream codestream, Tile tile) throws JPEG2000Exception {
    final MarkerAccessGraph codGraph = new MarkerAccessGraph();
    codGraph.add(new MarkerAccessNode(tile.markers, COD.key()));
    codGraph.add(new MarkerAccessNode(codestream.markers, COD.key()));

    final MarkerSegment markerSegment = codGraph.access();
    if (markerSegment == null) {
      throw new JPEG2000Exception(CodestreamMessages.MISSING_CODING_MARKER_SEGMENT);
    }
    if (!(markerSegment instanceof COD)) {
      throw new IllegalStateException("expected " + COD.class.getSimpleName() + " marker segment, but was " +
          markerSegment.getClass().getSimpleName());
    }

    return (COD) markerSegment;
  }

  public static COx accessCOx(Codestream codestream, Tile tile, int compIdx) throws JPEG2000Exception {
    final MarkerSegmentContainer mainSegments = codestream.markers;
    final MarkerSegmentContainer tileSegments = tile.markers;

    final MarkerAccessGraph coGraph = new MarkerAccessGraph();
    coGraph.add(new MarkerAccessNode(tileSegments, COC.key(compIdx)));
    coGraph.add(new MarkerAccessNode(tileSegments, COD.key()));
    coGraph.add(new MarkerAccessNode(mainSegments, COC.key(compIdx)));
    coGraph.add(new MarkerAccessNode(mainSegments, COD.key()));

    final MarkerSegment markerSegment = coGraph.access();
    if (markerSegment == null) {
      throw new JPEG2000Exception(CodestreamMessages.MISSING_CODING_MARKER_SEGMENT);
    }
    if (!(markerSegment instanceof COx)) {
      throw new IllegalStateException("expected " + COx.class.getSimpleName() + " marker segment, but was " +
          markerSegment.getClass().getSimpleName());
    }

    return (COx) markerSegment;
  }

}
