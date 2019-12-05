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

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Validate;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Capability;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.param.DirectParameterInfo;
import com.levigo.jadice.format.jpeg2000.internal.param.Parameters;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;

/**
 * Defined in <i>ITU-T.800, A.5.1</i>.
 * <p>
 * <b>Function:</b> Provides information about the uncompressed image such as the width and height of the reference
 * grid, the width and height of the tiles, the number of components, component bit depth, and the separation of
 * component samples with respect to the reference grid (see <i>ITU-T.800, B.2</i>).
 * <p>
 * <b>Usage:</b> Main header. There shall be one and only one in the main header immediately after the {@link
 * Marker#SOC}. There shall be only one {@link SIZ} per codestream.
 * <p>
 * <b>Length:</b> Variable depending on the number of components.
 */
public class SIZ extends AbstractMarkerSegment {

  public static final int MASK_SAMPLE_SIGN = 0x80;
  public static final int MASK_BIT_DEPTH = 0x7F;

  /** All features specified in <i>ITU-T.800</i> required. */
  public static final int VALUE_RSIZ_T800_FULL = 0;

  /** Codestream restricted as described for <i>Profile 0</i> from <i>ITU-T.800, Table A.45</i>. */
  public static final int VALUE_RSIZ_PROFILE_0 = 1 << 0;

  /** Codestream restricted as described for <i>Profile 1</i> from <i>ITU-T.800, Table A.45</i>. */
  public static final int VALUE_RSIZ_PROFILE_1 = 1 << 1;

  /**
   * Value for <code>Rsiz</code> if at least one of the extended capabilities specified in <i>ITU-T.801, Table A.2</i>
   * is present. Corresponds with {@link #MASK_RSIZ_T801}.
   * <p>
   * Value: <code>1000 xxxx xxxx xxxx</code>
   */
  public static final int VALUE_RSIZ_T801 = 1 << 15;

  /**
   * Mask for determining if at least one of the extended capabilities specified in <i>ITU-T.801, Table A.2</i> is
   * present. Correspongs with {@link #VALUE_RSIZ_T801}.
//   * <p>
   * Mask: <code>1111 xxxx xxxx xxxx</code>
   * <p>
   * Value: <code>1000 xxxx xxxx xxxx</code> (see {@link #VALUE_RSIZ_T801}).
   */
  public static final int MASK_RSIZ_T801 = 0xF000;

  /**
   * Value for determining if variable DC offset capability is required to decode this codestream. Corresponds with
   * {@link #MASK_RSIZ_VARIABLE_DC_OFFSET}.
   * <p>
   * <b>Note:</b> Shall not be used with the multiple component transformation.
   * <p>
   * Mask: <code>xxxx xxx0 xxxx xxx1</code>
   */
  public static final int VALUE_RSIZ_VARIABLE_DC_OFFSET = 1 << 0;

  /**
   * Mask for determining if variable DC offset capability is required to decode this codestream. Corresponds with
   * {@link #VALUE_RSIZ_VARIABLE_DC_OFFSET}.
   * <p>
   * <b>Note:</b> Shall not be used with the multiple component transformation.
   * <p>
   * Mask: <code>xxxx xxx0 xxxx xxx1</code>
   */
  public static final int MASK_RSIZ_VARIABLE_DC_OFFSET = 1 << 8 | 1 << 0;

  /**
   * Mask for determining if variable scalar quantization capability is required to decode this codestream.
   * <p>
   * Mask: <code>xxxx xxxx xxxx xx1x</code>
   */
  public static final int MASK_RSIZ_VARIABLE_SCALAR_QUANTIZATION = 1 << 1;

  /**
   * Mask for determining if trellis coded quantization capability is useful to decode this codestream
   * <p>
   * Mask: <code>xxxx xxxx xxxx x1xx</code>
   */
  public static final int MASK_RSIZ_TRELLIS_CODED_QUANTIZATION = 1 << 2;

  /**
   * Mask for determining if visual masking capability is useful to decode this codestream.
   * <p>
   * Mask: <code>xxxx xxxx xxxx 1xxx</code>
   */
  public static final int MASK_RSIZ_VISUAL_MASKING = 1 << 3;

  /**
   * Mask for determining if single sample overlap capability is required to decode this codestream.
   * <p>
   * Mask: <code>xxxx xxxx xxx1 xxxx</code>
   */
  public static final int MASK_RSIZ_SINGLE_SAMPLE_OVERLAP = 1 << 4;

  /**
   * Mask for determining if arbitrary decomposition style capability is required to decode this codestream.
   * <p>
   * Mask: <code>xxxx xxxx xx1x xxxx</code>
   */
  public static final int MASK_RSIZ_ARBITRARY_DECOMPOSITION_STYLE = 1 << 5;

  /**
   * Mask for determining if arbitrary transformation kernel capability is required to decode this codestream.
   * <p>
   * Mask: <code>xxxx xxxx x1xx xxxx</code>
   */
  public static final int MASK_RSIZ_ARBITRARY_TRANSFORMATION_KERNEL = 1 << 6;

  /**
   * Mask for determining if whole sample symmetric transformation kernel capability is required to decode this
   * codestream.
   * <p>
   * Mask: <code>xxxx xxxx 1xxx xxxx</code>
   */
  public static final int MASK_RSIZ_WHOLE_SAMPLE_SYMMETRIC_TRANSFORMATION_KERNEL = 1 << 7;

  /**
   * Mask for determining if multiple component transformation capability is required to decode this codestream.
   * <p>
   * Mask: <code>xxxx xxx1 xxxx xxxx</code>
   */
  public static final int MASK_RSIZ_MULTIPLE_COMPONENT_TRANSFORMATION = 1 << 8;

  /**
   * Mask for determining if non-linear point transformation capability is useful to decode this codestream.
   * <p>
   * Mask: <code>xxxx xx1x xxxx xxxx</code>
   */
  public static final int MASK_RSIZ_NONLINEAR_POINT_TRANSFORMATION = 1 << 9;

  /**
   * Mask for determining if arbitrary shaped region of interest capability is required to decode this codestream.
   * <p>
   * Mask: <code>xxxx x1xx xxxx xxxx</code>
   */
  public static final int MASK_RSIZ_ARBITRARY_SHAPED_ROI = 1 << 10;

  /**
   * Mask for determining if precinct-dependent quantization is required to decode this codestream.
   * <p>
   * Mask: <code>xxxx 1xxx xxxx xxxx</code>
   */
  public static final int MASK_RSIZ_PRECINCT_DEPENDENT_QUANTIZATION = 1 << 11;

  /** Minimum allowed value for <code>Lsiz</code>. See <i>ITU-T.800,Table A.9</i>. */
  public static final int Lsiz_MIN = 41;

  /** Maximum allowed value for <code>Lsiz</code>. See <i>ITU-T.800,Table A.9</i>. */
  public static final int Lsiz_MAX = 49190;

  /**
   * Minimum allowed value for <code>Xsiz</code>, <code>Ysiz</code>, <code>XTsiz</code> and <code>YTsiz</code>. See
   * <i>ITU-T.800,Table A.9</i>.
   */
  public static final byte MIN_DIMENSION = 1;

  /**
   * Maximum allowed value (2<sup>32</sup>-1) for <code>Xsiz</code>, <code>Ysiz</code>, <code>XTsiz</code> and
   * <code>YTsiz</code>. See <i>ITU-T.800,Table A.9</i>.
   */
  public static final long MAX_DIMENSION = 4294967295l;

  /**
   * Minimum allowed value for <code>XOsiz</code>, <code>YOsiz</code>, <code>XTOsiz</code> and <code>YTOsiz</code>. See
   * <i>ITU-T.800,Table A.9</i>.
   */
  public static final byte MIN_OFFSET = 0;

  /**
   * Maximum allowed value (2<sup>32</sup>-2) for <code>XOsiz</code>, <code>YOsiz</code>, <code>XTOsiz</code> and
   * <code>YTOsiz</code>. See <i>ITU-T.800,Table A.9</i>.
   */
  public static final long MAX_OFFSET = 4294967294l;

  /** Minimum allowed value for <code>XRsiz</code> and <code>YRsiz</code>. See <i>ITU-T.800,Table A.9</i>. */
  public static final long MIN_SUBSAMPLING = 1;

  /** Maximum allowed value for <code>XRsiz</code> and <code>YRsiz</code>. See <i>ITU-T.800,Table A.9</i>. */
  public static final long MAX_SUBSAMPLING = 255;

  /** Minimum allowed value for <code>Csiz</code>. See <i>ITU-T.800,Table A.9</i>. */
  public static final long Csiz_MIN = 1;

  /** Maximum allowed value for <code>Csiz</code>. See <i>ITU-T.800,Table A.9</i>. */
  public static final long Csiz_MAX = 16384;

  /**
   * Length of marker segment in bytes (not including the marker). The value of this parameter is determined by the
   * following equation: <code>Lsiz = 38 + 3 ⋅ Csiz</code>.
   */
  public int Lsiz;

  /**
   * Denotes capabilities that a decoder needs to properly decode the codestream. See and use {@link Capability} for
   * information extraction.
   */
  public int Rsiz;

  /** Width of the reference grid. */
  public int Xsiz;

  /** Height of the reference grid. */
  public int Ysiz;

  /** Horizontal offset from the origin of the reference grid to the left side of the image area. */
  public int XOsiz;

  /** Vertical offset from the origin of the reference grid to the top side of the image area. */
  public int YOsiz;

  /** Width of one reference tile with respect to the reference grid. */
  public int XTsiz;

  /** Height of one reference tile with respect to the reference grid. */
  public int YTsiz;

  /** Horizontal offset from the origin of the reference grid to the left side of the first tile. */
  public int XTOsiz;

  /** Vertical offset from the origin of the reference grid to the top side of the first tile. */
  public int YTOsiz;

  /** Number of components in the image. */
  public int Csiz;

  /**
   * Precision (depth) in bits and sign of the <i>ith</i> component samples. The precision is the precision of the
   * component samples before DC level shifting is performed (i.e., the precision of the original component samples
   * before any processing is performed).
   * <p>
   * If the component sample values are signed, then the range of component sample values is <code>–2<sup>(Ssiz+1 AND
   * 0x7F)–1</sup> ≤ component sample value ≤ 2<sup>(Ssiz+1 AND 0x7F)–1</sup> – 1</code>.
   * <p>
   * There is one occurrence of this parameter for each component. The order corresponds to the component's index,
   * starting with zero.
   * <p>
   * Component sample bit depth = <b>value + 1</b>. From 1 bit deep through 38 bits deep respectively (counting the
   * sign bit, if appropriate), <i>R<sub>I</sub></i>. The component sample precision is limited by the number of guard
   * bits, quantization, growth of coefficients at each decomposition level and the number of coding passes that can be
   * signalled. Not all combinations of coding styles will allow the coding of 38-bit samples.
   */
  public int[] Ssiz;

  /**
   * Horizontal separation of a sample of <i>ith</i> component with respect to the reference grid. There is one
   * occurrence of this parameter for each component.
   */
  public int[] XRsiz;

  /**
   * Vertical separation of a sample of <i>ith</i> component with respect to the reference grid. There is one
   * occurrence of this parameter for each component.
   */
  public int[] YRsiz;

  @Override
  public Marker getMarker() {
    return Marker.SIZ;
  }

  protected void read(ImageInputStream source, Codestream codestream)
      throws IOException {

    Lsiz = source.readUnsignedShort();
    Rsiz = source.readUnsignedShort();

    Xsiz = (int) source.readUnsignedInt();
    Ysiz = (int) source.readUnsignedInt();
    XOsiz = (int) source.readUnsignedInt();
    YOsiz = (int) source.readUnsignedInt();
    XTsiz = (int) source.readUnsignedInt();
    YTsiz = (int) source.readUnsignedInt();
    XTOsiz = (int) source.readUnsignedInt();
    YTOsiz = (int) source.readUnsignedInt();

    Csiz = source.readUnsignedShort();

    Ssiz = new int[Csiz];
    XRsiz = new int[Csiz];
    YRsiz = new int[Csiz];

    for (int c = 0; c < Csiz; c++) {
      Ssiz[c] = source.readUnsignedByte();
      XRsiz[c] = source.readUnsignedByte();
      YRsiz[c] = source.readUnsignedByte();
    }
  }

  protected void validate(Codestream codestream) throws JPEG2000Exception {
    Validate.inRange("Lsiz", Lsiz, Lsiz_MIN, Lsiz_MAX);

    Validate.inRange("Xsiz", Xsiz, MIN_DIMENSION, MAX_DIMENSION);
    Validate.inRange("Ysiz", Ysiz, MIN_DIMENSION, MAX_DIMENSION);
    Validate.inRange("XTsiz", XTsiz, MIN_DIMENSION, MAX_DIMENSION);
    Validate.inRange("YTsiz", YTsiz, MIN_DIMENSION, MAX_DIMENSION);

    Validate.inRange("XOsiz", XOsiz, MIN_OFFSET, MAX_OFFSET);
    Validate.inRange("Ysiz", YOsiz, MIN_OFFSET, MAX_OFFSET);
    Validate.inRange("XTOsiz", XTOsiz, MIN_OFFSET, MAX_OFFSET);
    Validate.inRange("YTOsiz", YTOsiz, MIN_OFFSET, MAX_OFFSET);

    Validate.inRange("Csiz", Csiz, Csiz_MIN, Csiz_MAX);

    for (int c = 0; c < Csiz; c++) {
      final int bitDepth = Parameters.extract(Ssiz[c], MASK_BIT_DEPTH);
      Validate.inRange("Ssiz[" + c + "] bit depth value", bitDepth, 0, 37);
      Validate.inRange("XRsiz[" + c + "]", XRsiz[c], MIN_SUBSAMPLING, MAX_SUBSAMPLING);
      Validate.inRange("YRsiz[" + c + "]", YRsiz[c], MIN_SUBSAMPLING, MAX_SUBSAMPLING);
    }
  }

  @Override
  public void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lsiz);
    sink.writeShort(Rsiz);

    sink.writeInt(Xsiz);
    sink.writeInt(Ysiz);
    sink.writeInt(XOsiz);
    sink.writeInt(YOsiz);
    sink.writeInt(XTsiz);
    sink.writeInt(YTsiz);
    sink.writeInt(XTOsiz);
    sink.writeInt(YTOsiz);

    sink.writeShort(Csiz);

    for (int c = 0; c < Csiz; c++) {
      sink.writeByte(Ssiz[c]);
      sink.writeByte(XRsiz[c]);
      sink.writeByte(YRsiz[c]);
    }
  }

  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lsiz", Lsiz, "SIZ.L"));
    markerInfo.add(new PropertiesParameterInfo("Rsiz", Integer.toBinaryString(Rsiz), "SIZ.R"));
    markerInfo.add(new PropertiesParameterInfo("Xsiz", Xsiz, "SIZ.X"));
    markerInfo.add(new PropertiesParameterInfo("Ysiz", Ysiz, "SIZ.Y"));
    markerInfo.add(new PropertiesParameterInfo("XOsiz", XOsiz, "SIZ.XO"));
    markerInfo.add(new PropertiesParameterInfo("YOsiz", YOsiz, "SIZ.YO"));
    markerInfo.add(new PropertiesParameterInfo("XTsiz", XTsiz, "SIZ.XT"));
    markerInfo.add(new PropertiesParameterInfo("YTsiz", YTsiz, "SIZ.YT"));
    markerInfo.add(new PropertiesParameterInfo("XTOsiz", XTOsiz, "SIZ.XTO"));
    markerInfo.add(new PropertiesParameterInfo("YTOsiz", YTOsiz, "SIZ.YTO"));
    markerInfo.add(new PropertiesParameterInfo("Csiz", Csiz, "SIZ.C"));

    for (int c = 0; c < Csiz; c++) {
      String comp = "[comp=" + c + "]";
      final int s = Ssiz[c];
      final boolean isSigned = Parameters.isSet(s, MASK_SAMPLE_SIGN);
      final int precision = Parameters.extract(s, MASK_BIT_DEPTH);
      markerInfo.add(new PropertiesParameterInfo("Ssiz" + comp, Integer.toBinaryString(s), "SIZ.S"));
      markerInfo.add(new DirectParameterInfo("Ssiz:Signed" + comp, isSigned ? "yes" : "no"));
      markerInfo.add(new DirectParameterInfo("Ssiz:Precision" + comp, precision));
      markerInfo.add(new PropertiesParameterInfo("XRsiz" + comp, XRsiz[c], "SIZ.XR"));
      markerInfo.add(new PropertiesParameterInfo("YRsiz" + comp, YRsiz[c], "SIZ.YR"));
    }
  }

}
