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
import org.jadice.jpeg2000.internal.param.Parameters;

import java.io.IOException;

/**
 * Defined in <i>ITU-T.801, A.3.5</i>
 * <p>
 * <b>Function:</b> Describes a transformation kernel and an index that allows assignment to tile-components.
 * <p>
 * <b>Usage:</b> Present only if the arbitrary transformation kernel capability bit in the {@link SIZ#Rsiz} parameter
 * (see <i>ITU-T.801, A.2.1</i>) is set. Main and first tile-part header of a given tile. May be up to 254 marker
 * segments in any header. A marker segment in the tile-part header with the same index as one in the main header
 * overrides the main header marker segment.
 * <p>
 * <b>Length:</b> Variable.
 */
public class ATK extends AbstractMarkerSegment {

  /**
   * Mask for determining the index of the {@link ATK#Satk} parameter of an {@link ATK} marker segment (value range: 2
   * to 255). Value can be used directly after mask was applied.
   */
  public static final int MASK_INDEX = 0xFF;

  /**
   * Mask (<code>xxxx x111 xxxx xxxx</code>) for determining the parameter size. See <i>ITU-T.801, Table A.28</i>.
   *
   * @see #VALUE_PARAM_SIZE_8_BIT_SIGNED_INTEGER
   * @see #VALUE_PARAM_SIZE_16_BIT_SIGNED_INTEGER
   * @see #VALUE_PARAM_SIZE_32_BIT_FLOATING_POINT
   * @see #VALUE_PARAM_SIZE_64_BIT_FLOATING_POINT
   * @see #VALUE_PARAM_SIZE_128_BIT_FLOATING_POINT
   */
  public static final int MASK_PARAM_SIZE = 0x700;

  /**
   * Parameters 8-bit signed integer, <i>Coeff_Typ</i><code> = 0</code>.
   * <p>
   * Value: <code>xxxx x000 xxxx xxxx</code>.
   */
  public static final int VALUE_PARAM_SIZE_8_BIT_SIGNED_INTEGER = 0x0;

  /**
   * Parameters 16-bit signed integer, <i>Coeff_Typ</i><code> = 1</code>.
   * <p>
   * Value: <code>xxxx x001 xxxx xxxx</code>.
   */
  public static final int VALUE_PARAM_SIZE_16_BIT_SIGNED_INTEGER = 0x100;

  /**
   * Parameters 32-bit floating point (<i>IEEE Std. 754-1985 R1990</i>), <i>Coeff_Typ</i><code> = 2</code>.
   * <p>
   * Value: <code>xxxx x010 xxxx xxxx</code>
   */
  public static final int VALUE_PARAM_SIZE_32_BIT_FLOATING_POINT = 0x200;

  /**
   * Parameters 64-bit floating point (<i>IEEE Std. 754-1985 R1990</i>), <i>Coeff_Typ</i><code> = 3</code>.
   * <p>
   * Value: <code>xxxx x011 xxxx xxxx</code>.
   */
  public static final int VALUE_PARAM_SIZE_64_BIT_FLOATING_POINT = 0x300;

  /**
   * Parameters 128-bit floating point (<i>IEEE Std. 754-1985 R1990</i>), <i>Coeff_Typ</i><code> = 4</code>.
   * <p>
   * Value: <code>xxxx x100 xxxx xxxx</code>.
   */
  public static final int VALUE_PARAM_SIZE_128_BIT_FLOATING_POINT = 0x400;

  /**
   * <b>Mask</b> (<code>xxxx 1xxx xxxx xxxx</code>) for determining the wavelet filter category, <i>Filt_Cat</i>.
   * <p>
   * <b>Values:</b><br>
   * <code>0</code>: Arbitrary filters, <i>Filt_Cat = ARB</i><br>
   * <code>1</code>: WS filters, <i>Filt_Cat = WS</i>. Only true in combination with a set bit for <i>Exten = WS</i>.
   */
  public static final int MASK_FILTER_CATEGORY = 0x800;

  /**
   * <b>Mask</b> (<code>xxx1 xxxx xxxx xxxx</code>) for determining wavelet transformation type.
   * <p>
   * <b>Values:</b><br>
   * <code>0</code>: Irreversible filter, <i>WT_Type = IRR</i>.<br>
   * <code>1</code>: Reversible filter, <i>WT_Type = REV</i>.
   */
  public static final int MASK_WAVELET_TRANSFORMATION_TYPE = 0x1000;

  /**
   * <b>Mask</b> (<code>xx1x xxxx xxxx xxxx</code>) for determining the initial odd or even subsequence,
   * m<sub>init</sub>.
   * <p>
   * <b>Values:</b><br>
   * <code>0</code>: Modify even-indexed subsequence in first reconstruction step, <i>m<sub>init</sub> = 0</i>.<br>
   * <code>1</code>: Modify odd-indexed subsequence in first reconstruction step, <i>m<sub>init</sub> = 1</i>.
   */
  public static final int MASK_SUBSEQUENCE = 0x2000;

  /**
   * <b>Mask</b> (<code>x1xx xxxx xxxx xxxx</code>) for determining the boundary extension method used in lifting
   * steps.
   * <p>
   * <b>Values:</b><br>
   * <code>0</code>: Constant, <i>Exten = CON</i>.<br>
   * <code>1</code>: Whole-sample symmetric, <i>Exten = WS</i>.
   */
  public static final int MASK_EXTENSION = 0x4000;

  /**
   * Length of marker segment in bytes (not including the marker).
   */
  public int Latk;

  /**
   * Coded information:
   * <ul>
   * <li>Index of this {@link ATK} marker segment.</li>
   * <li>The type, <i>Coeff_Typ</i>, of the scaling factor and lifting step parameters.</li>
   * <li>The wavelet filter category, <i>Filt_Cat</i>.</li>
   * <li>wavelet transformation type, <i>WT_Typ</i>.</li>
   * <li>The initial odd or even subsequence, <i>m<sub>init</sub></i>.</li>
   * <li>Boundary extension method used in lifting steps, <i>Exten</i>.</li>
   * </ul>
   * The values '<code>0000 0000</code>' (0) and '<code>0000 0001</code>' (1) are not available for this index having
   * been assigned to the 9-7 irreversible wavelet filter and the 5-3 reversible wavelet filter respectively in
   * <i>ITU-T.800, Annex A</i>.
   */
  public int Satk;

  /**
   * The scaling factor, <i>K</i>. Present for irreversible transformation only, <i>WT_Typ = IRR</i>.
   */
  public byte[] Katk;

  /**
   * Number of lifting steps, <i>N<sub>LS</sub></i>.
   */
  public int Natk;

  /**
   * Offset for each lifting step <i>s</i>, <i>off<sub>s</sub></i>. The index, <i>s</i>, ranges from <i>s = 0</i> to
   * <i>Natk – 1</i>. Present only if <i>Filt_Cat = ARB</i>.
   */
  public byte[] Oatk;

  /**
   * The base two scaling exponent for lifting step <i>s</i>, <i>ε<sub>s</sub></i>. Present only with reversible
   * transformation, <i>WT_Typ = REV</i>. The index, <i>s</i>, ranges from <i>s = 0</i> to <i>Natk – 1</i>.
   */
  public byte[] Eatk;

  /**
   * Additive residue for lifting step, <i>s</i>. Present for reversible transformations, <i>WT_Typ = REV</i> only. The
   * index, <i>s</i>, ranges from <i>s = 0</i> to <i>Natk – 1</i>.
   */
  public byte[][] Batk;

  /**
   * Number of lifting coefficients signaled for lifting step <i>s</i>. Provides the range, <i>k</i>, for
   * <i>Aatk<sup>sk</sup></i>. The index, <i>s</i>, ranges from <i>s = 0</i> to <i>Natk – 1</i>.
   */
  public byte[] LCatk;

  /**
   * The <i>k</i><sup>th</sup> lifting coefficient for the lifting step <i>s</i>, <i>α<sub>s,k</sub></i>. The index,
   * <i>s</i>, ranges from <i>s = 0</i> to <i>Natk – 1</i>. The index, <i>k</i>, ranges from <i>k = 0</i> to <i>LCatk –
   * 1</i>.<br>
   */
  public byte[][][] Aatk;

  private boolean isReversible;
  private boolean isArbitrary;

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Latk = source.readUnsignedShort();
    Satk = source.readUnsignedShort();

    isReversible = Parameters.isValue(Satk, MASK_WAVELET_TRANSFORMATION_TYPE, 1);
    isArbitrary = Parameters.isValue(Satk, MASK_FILTER_CATEGORY, 0);

    final int numParamBytes = numParamBytes(Satk);

    if (!isReversible) {
      Katk = new byte[numParamBytes];
      source.readFully(Katk);
    }

    Natk = source.readUnsignedByte();

    if (isArbitrary) {
      Oatk = new byte[Natk];
      source.readFully(Oatk);
    }

    if (isReversible) {
      Eatk = new byte[Natk];
      Batk = new byte[Natk][];
    }

    LCatk = new byte[Natk];
    Aatk = new byte[Natk][][];

    for (int s = 0; s < Natk; s++) {
      if (isArbitrary) {
        Oatk[s] = (byte) source.readUnsignedByte();
      }

      if (isReversible) {
        Eatk[s] = (byte) source.readUnsignedByte();

        final byte[] b = new byte[numParamBytes];
        source.readFully(b);
        Batk[s] = b;
      }

      final int numCoefficients = LCatk[s] = (byte) source.readUnsignedByte();
      Aatk[s] = new byte[numCoefficients][];
      for (int k = 0; k < numCoefficients; k++) {
        final byte[] b = new byte[numParamBytes];
        source.readFully(b);
        Aatk[s][k] = b;
      }
    }
  }

  private static int numParamBytes(final int style) {
    switch (style & ATK.MASK_PARAM_SIZE){
      case VALUE_PARAM_SIZE_8_BIT_SIGNED_INTEGER:
        return 1;
      case VALUE_PARAM_SIZE_16_BIT_SIGNED_INTEGER:
        return 2;
      case VALUE_PARAM_SIZE_32_BIT_FLOATING_POINT:
        return 4;
      case VALUE_PARAM_SIZE_64_BIT_FLOATING_POINT:
        return 8;
      case VALUE_PARAM_SIZE_128_BIT_FLOATING_POINT:
        return 16;
      default:
        return 0;
    }
  }

  @Override
  public Marker getMarker() {
    return Marker.ATK;
  }

  @Override
  public MarkerKey getMarkerKey() {
    return new MarkerKey(getMarker(), Parameters.extract(Satk, MASK_INDEX));
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
    throw new UnsupportedOperationException("Currently not yet implemented.");
  }
}
