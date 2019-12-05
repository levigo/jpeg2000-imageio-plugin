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
import org.jadice.jpeg2000.internal.param.Parameters;
import org.jadice.jpeg2000.internal.param.PropertiesParameterInfo;

import java.io.IOException;

/**
 * Defined in <i>ITU-T.801, A.3.7</i>.
 * <p>
 * <b>Function:</b> Defines one multiple component transformation array per marker segment. The type and index of the
 * array defined in this marker distinguishes it from other {@link MCT} marker segments in a given header. This array
 * can be assigned to a collection of components within the {@link MCC} marker segment.
 * <p>
 * <b>Usage:</b> Present only if the multiple component transformation capability bit in the {@link SIZ#Rsiz} parameter
 * (see <i>ITU-T.801, A.2.1</i>) is a one value. Main and first tile-part header of a given tile. There may be up to
 * 255 {@link MCT} marker segments, or series of marker segments, in the main header. There may be up to 255 {@link
 * MCT} marker segments, or series of marker segments, in any tile-part header. An {@link MCT} marker segment in a
 * tile-part header overrides a main header {@link MCT} segment for that tile if and only if the ten low-order bits of
 * the {@link #Imct} fields of both marker segments are identical.
 * <p>
 * A series of {@link MCT} marker segments (defined as having the same {@link #Imct} value in the same header and a
 * <i>{@link #Ymct} > 0</i>) shall all appear in the same header in order of (consecutive) {@link #Zmct} parameter
 * values.
 * <p>
 * To apply the transformation array included in an {@link MCT} marker segment, an {@link MCC} marker segment must
 * exist that associates the {@link MCT} marker segment with a component collection. This association is made through
 * the array definition index of the {@link MCT} marker segment and the {@link MCC#Tmcc} fields of {@link MCC} marker
 * segments. If no such {@link MCC} marker segment exists, then the transformation array included in the {@link MCT}
 * marker segment shall not be used in the decoding process.
 * <p>
 * <b>Length:</b> Variable depending on the size of the array.
 */
public class MCT extends AbstractMarkerSegment {

  /**
   * <b>Mask</b> (<code>xxxx xxxx 1111 1111</code>) for determining the index of the array definition. Possible/valid
   * values are 1 to 255. Masked {@link MCT#Imct} value can be used directly.
   */
  public static final int MASK_INDEX = 0xFF;

  /**
   * <b>Mask</b> (<code>xxxx xx11 xxxx xxxx</code>) for determining the array type.
   *
   * @see #VALUE_ARRAY_TYPE_DEPENDENCY_TRANSFORMATION
   * @see #VALUE_ARRAY_TYPE_DECORRELATION_TRANSFORMATION
   * @see #VALUE_ARRAY_TYPE_OFFSET
   */
  public static final int MASK_ARRAY_TYPE = 0x300;

  /**
   * <b>Value</b> of array type parameter defining 'dependency transformation'. Use in combination with {@link
   * #MASK_ARRAY_TYPE}.
   *
   * @see #MASK_ARRAY_TYPE
   */
  public static final int VALUE_ARRAY_TYPE_DEPENDENCY_TRANSFORMATION = 0x0;

  /**
   * <b>Value</b> of array type parameter defining 'decorrelation transformation'. Use in combination with {@link
   * #MASK_ARRAY_TYPE}.
   *
   * @see #MASK_ARRAY_TYPE
   */
  public static final int VALUE_ARRAY_TYPE_DECORRELATION_TRANSFORMATION = 0x100;

  /**
   * <b>Value</b> of array type parameter defining 'offset'. Use in combination with {@link #MASK_ARRAY_TYPE}.
   *
   * @see #MASK_ARRAY_TYPE
   */
  public static final int VALUE_ARRAY_TYPE_OFFSET = 0x200;

  /**
   * <b>Mask</b> (<code>xxxx 11xx xxxx xxxx</code>) for determining the size of the array elements.
   *
   * @see #VALUE_ARRAY_ELEMENT_SIZE_16_BIT_SIGNED_INTEGER
   * @see #VALUE_ARRAY_ELEMENT_SIZE_32_BIT_SIGNED_INTEGER
   * @see #VALUE_ARRAY_ELEMENT_SIZE_32_BIT_FLOATINGPOINT
   * @see #VALUE_ARRAY_ELEMENT_SIZE_64_BIT_FLOATINGPOINT
   */
  public static final int MASK_ARRAY_ELEMENT_SIZE = 0xC00;

  /**
   * <b>Value</b> of the array element size parameter defining 16 bit signed integers. Use in combination with {@link
   * #MASK_ARRAY_ELEMENT_SIZE}.
   *
   * @see #MASK_ARRAY_ELEMENT_SIZE
   */
  public static final int VALUE_ARRAY_ELEMENT_SIZE_16_BIT_SIGNED_INTEGER = 0x0;

  /**
   * <b>Value</b> of the array element size parameter defining 32 bit signed integers. Use in combination with {@link
   * #MASK_ARRAY_ELEMENT_SIZE}.
   *
   * @see #MASK_ARRAY_ELEMENT_SIZE
   */
  public static final int VALUE_ARRAY_ELEMENT_SIZE_32_BIT_SIGNED_INTEGER = 0x400;

  /**
   * <b>Value</b> of the array element size parameter defining 32 bit floating point numbers (<i>IEEE Std. 754-1985
   * R1990</i>). Use in combination with {@link #MASK_ARRAY_ELEMENT_SIZE}.
   *
   * @see #MASK_ARRAY_ELEMENT_SIZE
   */
  public static final int VALUE_ARRAY_ELEMENT_SIZE_32_BIT_FLOATINGPOINT = 0x800;

  /**
   * <b>Value</b> of the array element size parameter defining 64 bit floating point numbers (<i>IEEE Std. 754-1985
   * R1990</i>). Use in combination with {@link #MASK_ARRAY_ELEMENT_SIZE}.
   *
   * @see #MASK_ARRAY_ELEMENT_SIZE
   */
  public static final int VALUE_ARRAY_ELEMENT_SIZE_64_BIT_FLOATINGPOINT = 0xC00;

  /** Length of marker segment in bytes (not including the marker). */
  public int Lmct;

  /**
   * Index of this marker segment in a series of {@link MCT} marker segments. All the marker segments in the series
   * have the same {@link #Imct} parameter value present in this header. The data in each subsequent {@link MCT} marker
   * segment shall be appended, in order, to make on stream of {@link #SPmct}<sup>i</sup> parameter values. The {@link
   * #Ymct} parameter values are present only in the first marker segment in the series (<i>{@link #Zmct} = 0</i>).
   */
  public int Zmct;

  /**
   * Multiple component transformation index value, array type, and parameter size. An {@link MCT} marker segment, or
   * series, with a given {@link #Imct} value in the tile-part header overrides a main header {@link MCT} marker
   * segment, or series, if and only if the ten low-order bits (index, and transformation type) of the {@link #Imct}
   * values of both markers are identical.
   */
  public int Imct;

  /**
   * Index of the last number of {@link MCT} marker segment in the series. For every series of {@link MCT} marker
   * segments (i.e., {@link MCT} marker segments in this header with the same {@link #Imct} parameter value), there
   * shall be {@link MCT} marker segment with {@link #Zmct} parameter values of <i>0 to {@link #Ymct}</i>. The last
   * {@link MCT} marker segment will have <i>{@link #Zmct} = {@link #Ymct}</i>. This value is present only in the first
   * marker segment in the series (<i>{@link #Zmct} = 0</i>).
   */
  public int Ymct;

  /**
   * Parameters for the multiple component transformation definition. One parameter value for each element in the
   * array. See <i>ITU-T.801, J.2</i> to determine the number of array elements and their order in the marker segment.
   * The number of elements in a row and the number of rows (elements in a column) are determined by the type of array
   * and the number of the input and output components to which it is assigned.
   * <p>
   * Variable size. Array of types as indicated in <i>ITU-T.801, Table A.33</i>.
   */
  public long[] SPmct;

  @Override
  public Marker getMarker() {
    return Marker.CBD;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException, JPEG2000Exception {
    Lmct = source.readUnsignedShort();
    Zmct = source.readUnsignedShort();
    Imct = source.readUnsignedShort();

    int remainingBytes;
    if (Zmct == 0) {
      Ymct = source.readUnsignedShort();
      remainingBytes = Lmct - 8;
    } else {
      remainingBytes = Lmct - 6;
    }

    int numParams = 0;
    switch (Parameters.extract(Imct, MASK_ARRAY_ELEMENT_SIZE)){
      case VALUE_ARRAY_ELEMENT_SIZE_16_BIT_SIGNED_INTEGER:
        numParams = remainingBytes / 2;
        SPmct = new long[numParams];
        for (int i = 0; i < numParams; i++) {
          SPmct[i] = source.readShort();
        }
        break;

      case VALUE_ARRAY_ELEMENT_SIZE_32_BIT_SIGNED_INTEGER:
        numParams = remainingBytes / 4;
        SPmct = new long[numParams];
        for (int i = 0; i < numParams; i++) {
          SPmct[i] = source.readInt();
        }

      case VALUE_ARRAY_ELEMENT_SIZE_32_BIT_FLOATINGPOINT:
        numParams = remainingBytes / 4;
        SPmct = new long[numParams];
        for (int i = 0; i < numParams; i++) {
          SPmct[i] = source.readBits(32);
        }
        break;

      case VALUE_ARRAY_ELEMENT_SIZE_64_BIT_FLOATINGPOINT:
        numParams = remainingBytes / 8;
        SPmct = new long[numParams];
        for (int i = 0; i < numParams; i++) {
          SPmct[i] = source.readBits(64);
        }
        break;
    }

  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lmct);
    sink.writeShort(Zmct);
    sink.writeShort(Imct);

    if (Zmct == 0) {
      sink.writeShort(Ymct);
    }

    switch (Parameters.extract(Imct, MASK_ARRAY_ELEMENT_SIZE)){
      case VALUE_ARRAY_ELEMENT_SIZE_16_BIT_SIGNED_INTEGER:
        for (long l : SPmct) {
          sink.writeShort((short) l);
        }
        break;

      case VALUE_ARRAY_ELEMENT_SIZE_32_BIT_SIGNED_INTEGER:
        for (long l : SPmct) {
          sink.writeInt((int) l);
        }
        break;

      case VALUE_ARRAY_ELEMENT_SIZE_32_BIT_FLOATINGPOINT:
        for (long l : SPmct) {
          sink.writeBits(l, 32);
        }
        break;

      case VALUE_ARRAY_ELEMENT_SIZE_64_BIT_FLOATINGPOINT:
        for (long l : SPmct) {
          sink.writeBits(l, 64);
        }
        break;
    }

  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    throw new UnsupportedOperationException("Currently not yet implemented.");
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lmct", Lmct, "MCT.L"));
    markerInfo.add(new PropertiesParameterInfo("Zmct", Zmct, "MCT.Z"));
    markerInfo.add(new PropertiesParameterInfo("Imct", Imct, "MCT.I"));

    if (Zmct == 0) {
      markerInfo.add(new PropertiesParameterInfo("Ymct", Ymct, "MCT.Y"));
    }

    markerInfo.add(new PropertiesParameterInfo("SPmct", "MCT.SP"));
    for (int i = 0; i < SPmct.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", SPmct[i]));
    }
  }
}
