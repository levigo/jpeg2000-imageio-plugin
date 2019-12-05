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
import com.levigo.jadice.format.jpeg2000.internal.param.Parameters;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Defined in <i>ITU-T.801, A.3.8</i>.
 * <p>
 * <b>Function:</b> Describes the collection of input intermediate components, the collection of output intermediate
 * components, and the associated wavelets or arrays for a multiple component transform. This marker segment can appear
 * in the main header and can be referred to or overridden by an {@link MCC} marker in a tile-part header.
 * <p>
 * <b>Usage:</b> Present only if the multiple component transformation capability bit in the {@link SIZ#Rsiz} parameter
 * (see <i>ITU-T.801, A.2.1</i>) is a one value. Main and first tile-part header of a given tile. There may be up to
 * 255 {@link MCC} marker segments, or series of marker segments, in the main header. There may be up to 255 {@link
 * MCC} marker segments, or series of marker segments, in any tilepart header. An {@link MCC} marker segment in a
 * tile-part header with the same index ({@link #Imcc}) as one in the main header overrides the main header {@link MCC}
 * segment for that tile. A series of {@link MCC} marker segments (defined as having the same {@link #Imcc} value in
 * the same header and a <i>{@link #Ymcc} > 0</i>) shall all appear in the same header in order of (consecutive) {@link
 * #Zmcc} parameter values.
 * <p>
 * <b>Length:</b> Variable depending on the number of component collections.
 */
public class MCC extends AbstractMarkerSegment {

  /**
   * <b>Mask</b> (<code>xxxx xx11</code>) for determining component collection transform type.
   *
   * @see #VALUE_TRANSFORM_ARRAY_BASED_DEPENDENCY
   * @see #VALUE_TRANSFORM_ARRAY_BASED_DECORRELACTION
   * @see #VALUE_TRANSFORM_WAVELET_BASED
   */
  public static final int MASK_TRANSFORM = 0x3;

  /**
   * <b>Value</b> defining the use of array-based dependency transform for a component collection transform. Use in
   * combination with {@link #MASK_TRANSFORM}.
   *
   * @see #MASK_TRANSFORM
   */
  public static final int VALUE_TRANSFORM_ARRAY_BASED_DEPENDENCY = 0;

  /**
   * <b>Value</b> defining the use of array-based decorrelation transform for a component collection transform. Use in
   * combination with {@link #MASK_TRANSFORM}.
   *
   * @see #MASK_TRANSFORM
   */
  public static final int VALUE_TRANSFORM_ARRAY_BASED_DECORRELACTION = 1;

  /**
   * <b>Value</b> defining the use of wavelet-based transform for a component collection transform. Use in
   * combination with {@link #MASK_TRANSFORM}.
   *
   * @see #MASK_TRANSFORM
   */
  public static final int VALUE_TRANSFORM_WAVELET_BASED = 3;

  /**
   * <b>Mask</b> (<code>1xxx xxxx xxxx xxxx</code>) for determining the size of the {@link MCC#Wmcc} and {@link
   * MCC#Wmcc} parameters.<br>
   * <b>Values:</b><br>
   * Bit 0: Input or output component collection indices ({@link MCC#Wmcc}<sup>i</sup>) are 8 bit integers.<br>
   * Bit 1: Input or output component collection indices ({@link MCC#Wmcc}<sup>i</sup>) are 16 bit integers.
   */
  public static final int MASK_COLLECTION_INDICES_SIZE = 1 << 15;

  /**
   * <b>Mask</b> (<code>x111 1111 1111 1111</code>) for determining the number of input or output components in
   * <i>i</i>-th component collection (1-16384). Masked value can be used directly.
   */
  public static final int MASK_NUMBER_COMPONENTS = 0x7FFF;

  /**
   * <b>Mask</b> (<code>xxxx xxxx xxxx xxxx 1111 1111</code>) for determining the decorrelation or dependency transform
   * array index.<br>
   * <b>Values:</b><br>
   * 0: No assigned transform array (NULL array).<br>
   * all other: Decorrelation or dependency transform array index.
   */
  public static final int MASK_TRANSFORM_ARRAY = 0xFF;

  /**
   * <b>Mask</b> (<code>xxxx xxxx 1111 1111 xxxx xxxx</code>) for determining the offset array index. Use in
   * combination with a left-shift by {@value #SHIFT_OFFSET_ARRAY}<br>
   * <b>Values:</b><br>
   * 0: No assigned offset array (NULL array).<br>
   * all other: Offset array index.
   */
  public static final int MASK_OFFSET_ARRAY = 0xFF00;

  /** Use in combination with {@link #MASK_OFFSET_ARRAY}. */
  public static final int SHIFT_OFFSET_ARRAY = 8;

  /**
   * <b>Mask</b> (<code>xxxx xxx1 xxxx xxxx xxxx xxxx</code>) for determining the decorrelation or dependency transform
   * type.<br>
   * <b>Values:</b><br>
   * Bit 0: Decorrelation or dependency transform is irreversible.<br>
   * Bit 1: Decorrelation or dependency transform is reversible.
   */
  public static final int MASK_TRANSFORM_TYPE = 1 << 16;

  /**
   * <b>Mask</b> (<code>xxxx xxxx xxxx xxxx 1111 1111</code>) for determining the wavelet kernel.<br>
   * <b>Values:</b><br>
   * 0: 9-7 irreversible filter, defined in <i>ITU-T.800</i>.<br>
   * 1: 5-3 irreversible filter, defined in <i>ITU-T.800</i>.<br>
   * all other: Index of the {@link ATK} marker segment containing the wavelet kernel for component collection
   */
  public static final int MASK_KERNEL = 0xFF;

  /**
   * <b>Mask</b> (<code>xxxx xxxx 1111 1111 xxxx xxxx</code>) for determining the index of {@link MCT} marker segment
   * containing additive offsets for component collection. Use in combination with a left-shift by {@value
   * #SHIFT_MCT_INDEX}.
   *
   * @see #SHIFT_MCT_INDEX
   */
  public static final int MASK_MCT_INDEX = 0xFF00;

  /** Use in combination with {@link #MASK_MCT_INDEX}. */
  public static final int SHIFT_MCT_INDEX = 8;

  /**
   * <b>Mask</b> (<code>xx11 1111 xxxx xxxx xxxx xxxx</code>) for determining the number of dyadic wavelet
   * decomposition levels used in the component collection (0-32). Use in combination with a left-shift by {@value
   * #SHIFT_NUMBER_DYADIC_DL}.
   */
  public static final int MASK_NUMBER_DYADIC_DL = 0x3F0000;

  /** Use in combination with {@link #MASK_NUMBER_DYADIC_DL}. */
  public static final int SHIFT_NUMBER_DYADIC_DL = 16;

  /** Length of marker segment in bytes (not including the marker). */
  public int Lmcc;

  /**
   * Index of this marker segment in a series of {@link MCC} marker segments. All the marker segments in the series
   * have the same {@link #Imcc} parameter value present in this header. The data in each subsequent {@link MCC} marker
   * segment shall be appended, in order, to make one stream of the other parameters. The {@link #Ymcc} and {@link
   * #Qmcc} parameter appears only in the first marker segment (<i>{@link #Zmcc} = 0</i>).
   */
  public int Zmcc;

  /**
   * Index of this marker segment. An {@link MCC} marker segment, or series, with a given {@link #Imcc} value in the
   * tile-part header overrides a main header {@link MCC} marker segment, or series, with the same {@link #Imcc} value.
   */
  public int Imcc;

  /**
   * Index of the last number of {@link MCC} marker segment in the series. For every series of {@link MCC} marker
   * segments (i.e., {@link MCC} marker segments in this header with the same {@link #Imcc} parameter value), there
   * shall be {@link MCC} marker segment with {@link #Zmcc} parameter values of 0 to {@link #Ymcc}. The last {@link
   * MCC} marker segment will have <i> {@link #Zmcc} = {@link #Ymcc}</i>. This value is present only in the first
   * marker segment in the series (<i>{@link #Zmcc} = 0</i>).
   */
  public int Ymcc;

  /**
   * The number of collections in the {@link MCC} marker segment. This value is present only in the first marker
   * segment in the series (<i>{@link #Zmcc} = 0</i>).
   */
  public int Qmcc;

  /**
   * Indicates type of multiple component transform used for the <i>i</i>-th component collection (wavelet or
   * array-based decorrelation or array-based dependency). Defines the interpretation applied to {@link #Tmcc}.
   */
  public int Xmcc;

  /**
   * Indicates the number of input components for the <i>i</i>-th component collection and defines the number of bits
   * (8 or 16) used to represent the component indices in <i>i</i>-th collection.
   */
  public int Nmcc;

  /**
   * Input intermediate component indices included the <i>i</i>-th component collection. The number of indices in the
   * <i>i</i>-th component collection is {@link #Nmcc} <sup>i</sup>. Each index denotes an input intermediate
   * component. The order of the indices defines the ordering applied to the input intermediate components prior to
   * application of the inverse transform.
   */
  public int[] Cmcc;

  /**
   * Indicates the number of output intermediate components for the <i>i</i>-th component collection and defines the
   * number of bits (8 or 16) used to represent the component indices in <i>i</i>-th collection. If anything other than
   * an array-based irreversible decorrelation transform is used, {@link #Mmcc}<sup>i</sup> must equal {@link
   * #Nmcc}<sup>i</sup>.
   */
  public int Mmcc;

  /**
   * Intermediate component indices included the <i>i</i>-th output component collection. The number of indices in the
   * <i>i</i>-th component collection is {@link #Mmcc}<sup>i</sup>. All output intermediate component indices in a
   * given {@link MCC} marker segment shall appear only once across all collections in that {@link MCC} marker.
   */
  public int[] Wmcc;

  /**
   * For array-based component collection transforms, {@link #Tmcc}<sup>i</sup> assigns arrays defined in an {@link
   * MCT} marker segment to the <i>i</i>-th component collection. An {@link MCT} marker segment with the right type and
   * index in the first tile-part header of a tile is used before an {@link MCT} marker segment with the right type and
   * index in the main header. {@link #Tmcc}<sup>i</sup> also indicates the reversibility of array-based component
   * transforms.
   * <p>
   * For wavelet-based component collection transforms, {@link #Tmcc}<sup>i</sup> assigns a wavelet kernel defined in
   * <i>ITU-T.800, Annex A</i> or an {@link ATK} marker segment and the number of wavelet decomposition levels for the
   * <i>i</i>-th component collection (only the dyadic decomposition of <i>ITU-T.800</i> is supported). An {@link ATK}
   * marker segment with the proper index in the first tile-part header of a tile is used before an {@link ATK} marker
   * segment with the proper index in the main header. {@link #Tmcc}<sup>i</sup> also contains the index of an {@link
   * MCT} marker segment that contains component additive offsets.
   */
  public long Tmcc;

  /**
   * Present in the {@link MCC} marker segment only for those component collections that use a wavelet-based transform.
   * {@link #Omcc}<sup>i</sup> indicates the reference grid offset to apply in the component dimension for the
   * <i>i</i>-th component collection (see <i>ITU-T.801, J.2.2</i>).
   */
  public long Omcc;

  @Override
  public MarkerKey getMarkerKey() {
    return new MarkerKey(Marker.MCC, Zmcc);
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException, JPEG2000Exception {
    Lmcc = source.readUnsignedShort();
    Zmcc = source.readUnsignedShort();
    Imcc = source.readUnsignedByte();

    if (Zmcc == 0) {
      Ymcc = source.readUnsignedShort();
      Qmcc = source.readUnsignedShort();
    }

    // FIXME
    // There should be an iteration over i and j. Currently this implementation is incorrect. See ITU-T.801, A.3.8.
    Xmcc = source.readUnsignedByte();
    Nmcc = source.readUnsignedShort();

    Cmcc = new int[Nmcc & MASK_NUMBER_COMPONENTS];
    if (Parameters.isSet(Nmcc, MASK_COLLECTION_INDICES_SIZE)) {
      for (int i = 0; i < Cmcc.length; i++) {
        Cmcc[i] = source.readUnsignedShort();
      }
    } else {
      for (int i = 0; i < Cmcc.length; i++) {
        Cmcc[i] = source.readUnsignedByte();
      }
    }

    Mmcc = source.readUnsignedShort();
    Wmcc = new int[Mmcc & MASK_NUMBER_COMPONENTS];
    if (Parameters.isSet(Mmcc, MASK_COLLECTION_INDICES_SIZE)) {
      for (int i = 0; i < Wmcc.length; i++) {
        Wmcc[i] = source.readUnsignedShort();
      }
    } else {
      for (int i = 0; i < Wmcc.length; i++) {
        Wmcc[i] = source.readUnsignedByte();
      }
    }

    Tmcc = source.readBits(24);
    Omcc = source.readUnsignedInt();
  }

  @Override
  public Marker getMarker() {
    return Marker.MCC;
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
