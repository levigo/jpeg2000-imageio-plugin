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
import org.jadice.jpeg2000.internal.param.PropertiesParameterInfo;

import static org.jadice.jpeg2000.internal.codestream.Capability.T801_BASED;
import static org.jadice.jpeg2000.internal.codestream.Capability.T801_PRECINCT_DEPENDENT_QUANTIZATION;

import java.io.IOException;

/**
 * Defined in <i>ITU-T.800, A.4.2</i>.
 * <p>
 * <b>Function:</b> Marks the beginning of a tile-part, the index of its tile, and the index of its tile-part. The
 * tile-parts of a given tile shall appear in order (see {@link #TPsot}) in the codestream. However, tile-parts from
 * other tiles may be interleaved in the codestream. Therefore, the tile-parts from a given tile may not appear
 * contiguously in the codestream.
 * <p>
 * <b>Usage:</b> Every tile-part header. Shall be the first marker segment in a tile-part header. There shall be at
 * least one {@link SOT} in a codestream. There shall be only one {@link SOT} per tile-part.
 * <p>
 * <b>Length:</b> Variable depending on the number of components.
 */
public class SOT extends AbstractMarkerSegment {

  /** Allowed value for <code>Lsot</code>. See <i>ITU-T.800, Table A.5</i>. */
  public static final int VALUE_LSOT = 10;

  /** Minimum allowed value for <code>Isot</code>. See <i>ITU-T.800,Table A.5</i>. */
  public static final long MIN_ISOT = 0;

  /** Maximum allowed value for <code>Isot</code>. See <i>ITU-T.800,Table A.5</i>. */
  public static final long MAX_ISOT = 65534;

  /** Minimum allowed value for <code>Psot</code>. See <i>ITU-T.800,Table A.5</i>. */
  public static final long MIN_PSOT = 14;

  /** Maximum allowed value for <code>Psot</code>. See <i>ITU-T.800,Table A.5</i>. */
  public static final long MAX_PSOT = 4294967295l;

  /** Minimum allowed value for <code>TPsot</code>. See <i>ITU-T.800,Table A.5</i>. */
  public static final long MIN_TPSOT = 0;

  /** Maximum allowed value for <code>TPsot</code>. See <i>ITU-T.800,Table A.5</i>. */
  public static final long MAX_TPSOT = 254;

  /** Minimum allowed value for <code>TNsot</code>. See <i>ITU-T.800,Table A.5</i>. */
  public static final long MIN_TNSOT = 0;

  /** Maximum allowed value for <code>TNsot</code>. See <i>ITU-T.800,Table A.5</i>. */
  public static final long MAX_TNSOT = 255;

  /** Minimum allowed value for <code>TPsot</code>. See <i>ITU-T.800,Table A.5</i>. */
  public static final long MIN_TPSOT_EXTENDED = 0;

  /** Maximum allowed value for <code>TPsot</code>. See <i>ITU-T.800,Table A.5</i>. */
  public static final long MAX_TPSOT_EXTENDED = 65535;

  /** Minimum allowed value for <code>TNsot</code>. See <i>ITU-T.800,Table A.5</i>. */
  public static final long MIN_TNSOT_EXTENDED = 0;

  /** Maximum allowed value for <code>TNsot</code>. See <i>ITU-T.800,Table A.5</i>. */
  public static final long MAX_TNSOT_EXTENDED = 65535;

  /** Length of the marker segment in bytes (<b>not</b> including the marker's code). */
  public int Lsot;

  /** Tile index. This number refers to the tiles in raster order starting at the number 0. */
  public int Isot;

  /**
   * Length, in bytes, from the beginning of the first byte of this {@link SOT} marker segment (including marker's
   * code) of this tile-part to the end of the data of that tile-part.
   */
  public long Psot;

  /** Tile-part index. */
  public int TPsot;

  /**
   * Number of tile-parts of a tile in the codestream. Two values are allowed:
   * <ul>
   * <li>the correct number of tile-parts for that tile</li>
   * <li>and zero.</li>
   * </ul>
   * A zero value indicates that the number of tile-parts of this tile is/was not specified in this tile-part's {@link
   * #TNsot} parameter.
   */
  public int TNsot;

  /**
   * Flag indicating marker segment's format.
   * <p>
   * If the flag is set to <code>true</code>, the marker segment is extended like described in <i>ITU-T.801, A.2.2</i>.
   * In particular the {@link #TPsot} and {@link #TNsot} are signalled in 16 bits (2 bytes).
   * <p>
   * If the flag is set to <code>false</code>, the marker segment has the original format like described in
   * <i>ITU-T.800, A.4.2</i>. {@link #TPsot} and {@link #TNsot} are signalled in 8 bits (1 byte).
   */
  public boolean extended;

  @Override
  public Marker getMarker() {
    return Marker.SOT;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lsot = source.readUnsignedShort();
    Isot = source.readUnsignedShort();
    Psot = source.readUnsignedInt();

    extended = T801_BASED.isUsedBy(codestream) && T801_PRECINCT_DEPENDENT_QUANTIZATION.isUsedBy(codestream);

    if (extended) {
      TPsot = source.readUnsignedShort();
      TNsot = source.readUnsignedShort();
    } else {
      TPsot = source.readUnsignedByte();
      TNsot = source.readUnsignedByte();
    }
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lsot);
    sink.writeShort(Isot);
    sink.writeInt((int) Psot);

    if (extended) {
      sink.writeShort(TPsot);
      sink.writeShort(TNsot);
    } else {
      sink.writeByte(TPsot);
      sink.writeByte(TNsot);
    }
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    Validate.exact("Lsot", Lsot, VALUE_LSOT);
    Validate.inRange("Isot", Isot, MIN_ISOT, MAX_ISOT);

    if (Psot != 0) {
      Validate.inRange("Psot", Psot, MIN_PSOT, MAX_PSOT);
    }

    if (extended) {
      Validate.inRange("TPsot", TPsot, MIN_TPSOT, MAX_TPSOT_EXTENDED);
      Validate.inRange("TNsot", TNsot, MIN_TNSOT, MAX_TNSOT_EXTENDED);
    } else {
      Validate.inRange("TPsot", TPsot, MIN_TPSOT, MAX_TPSOT);
      Validate.inRange("TNsot", TNsot, MIN_TNSOT, MAX_TNSOT);
    }
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lsot", Lsot, "SOT.L"));
    markerInfo.add(new PropertiesParameterInfo("Isot", Isot, "SOT.I"));
    markerInfo.add(new PropertiesParameterInfo("Psot", Psot, "SOT.P"));
    markerInfo.add(new PropertiesParameterInfo("TPsot", TPsot, "SOT.TP"));
    markerInfo.add(new PropertiesParameterInfo("TNsot", TNsot, "SOT.TN"));
  }

}
