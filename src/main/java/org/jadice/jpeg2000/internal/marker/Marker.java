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

import org.jadice.util.base.collections.IntHashtable;
import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;

/**
 * The markers defined here are designated by the three-letter code of the marker associated with the marker segment.
 */
@Refer(to = Spec.J2K_CORE, page = 14, section = "Table A.2", called = "List of Markers and Marker-Segments")
public enum Marker {

  /** Start of codestream marker, marker code only. */
  SOC(0xFF4F, false, "Start of codestream"),

  /** End of codestream marker, marker code only. */
  EOC(0xFFD9, false, "End of codestream") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new EOC();
    }
  },

  /** Start of tile-part marker */
  SOT(0xFF90, "Start of tile-part") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new SOT();
    }
  },

  /**
   * Start of data marker, marker code only.
   */
  SOD(0xFF93, false, "Start of data"),
  // -------------------------------------------

  // -------------------------------------------
  // Fixed information marker segments
  /**
   * Image and tile size marker
   */
  SIZ(0xFF51, "Image and tile size") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new SIZ();
    }
  },
  // -------------------------------------------

  // -------------------------------------------
  // Functional marker segments
  /**
   * Coding style default marker
   */
  COD(0xFF52, "Coding style default") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new COD();
    }
  },

  /** Coding style component marker */
  COC(0xFF53, "Coding style component") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new COC();
    }
  },

  /** Region-of-interest marker */
  RGN(0xFF5E, "Region-of-interest") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new RGN();
    }
  },

  /**
   * Quantization default marker
   */
  QCD(0xFF5C, "Quantization default") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new QCD();
    }
  },

  /**
   * Quantization component marker
   */
  QCC(0xFF5D, "Quantization component") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new QCC();
    }
  },

  /**
   * Progression order change marker (required if there are progression order changes).
   */
  POC(0xFF5F, "Progression order change") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new POC();
    }
  },
  // -------------------------------------------

  // -------------------------------------------
  // Informational marker segments
  /**
   * Component registration marker
   */
  CRG(0xFF63, "Component registration") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new CRG();
    }
  },

  /**
   * Comment marker
   */
  COM(0xFF64, "Comment") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new COM();
    }
  },
  // -------------------------------------------

  // -------------------------------------------
  // Pointer marker segments
  /**
   * Packet length, main header marker
   */
  PLM(0xFF57, "Packet length, main header"),

  /**
   * Packet length, tile-part header marker
   */
  PLT(0xFF58, "Packet length, tile-part header"),

  /**
   * Packed packet headers, main header marker
   */
  PPM(0xFF60, "Packed packet headers, main header"),

  /**
   * Packed packet headers, tile-part header marker
   */
  PPT(0xFF61, "Packed packet headers, tile-part header"),

  /**
   * Tile-part lengths marker
   */
  TLM(0xFF55, "Tile-part lengths") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new TLM();
    }
  },
  // -------------------------------------------

  // -------------------------------------------
  // In-bit-stream markers
  /**
   * Start of packet marker code, marker code only.
   */
  SOP(0xFF91, "Start of packet") {
    @Override
    public MarkerSegment createMarkerSegment() {
      return new SOP();
    }
  },

  /**
   * End of packet header marker code, marker code only.
   */
  EPH(0xFF92, "End of packet header marker"),
  // -------------------------------------------

  // -------------------------------------------
  // Additional marker segments specified by ITU-T.801, A.3
  /**
   * Variable DC offset
   */
  DCO(0xFF70, "Variable DC offset"),

  /**
   * Visual masking
   */
  VMS(0xFF71, "Visual masking"),

  /**
   * Downsampling factor styles
   */
  DFS(0xFF72, "Downsampling factor styles"),

  /**
   * Arbitrary decomposition styles
   */
  ADS(0xFF73, "Arbitrary decomposition styles"),

  /**
   * Arbitrary transformation kernels
   */
  ATK(0xFF79, "Arbitrary transformation kernels"),

  /**
   * Component bit depth definition
   */
  CBD(0xFF78, "Component bit depth definition"),

  /**
   * Multiple component transformation definition
   */
  MCT(0xFF74, "Multiple component transformation definition"),

  /**
   * Multiple component transform collection
   */
  MCC(0xFF75, "Multiple component transform collection "),

  /**
   * Multiple component transform ordering
   */
  MCO(0xFF77, "Multiple component transform ordering"),

  /**
   * Non-linearity point transformation
   */
  NLT(0xFF76, "Non-linearity point transformation"),

  /**
   * Quantization default, precinct
   */
  QPD(0xFF5A, "Quantization default, precinct"),

  /**
   * Quantization component, precinct
   */
  QPC(0xFF5B, "Quantization component, precinct"),
  // -------------------------------------------

  // -------------------------------------------
  // Reserved
  /**
   * Synthetic marker. <b>Not</b> defined in <i>ITU-T.800</i>. Introduced to mark unknown markers.
   */
  RES(0x0, false, "Unknown, synthetic marker")
  // -------------------------------------------
  ;

  public final int code;

  public final boolean hasParameters;

  public String description;

  private Marker(int code, String description) {
    this(code, true, description);
  }

  private Marker(int code, boolean hasParameters, String description) {
    this.code = code;
    this.description = description;
    this.hasParameters = hasParameters;
  }

  /**
   * Always creates a new instance of {@link MarkerKey} identifying this {@link org.jadice.jpeg2000.internal.marker.Marker}.
   *
   * @return a new instance of {@link MarkerKey} representing the {@link org.jadice.jpeg2000.internal.marker.Marker}.
   */
  public MarkerKey key() {
    return new MarkerKey(this);
  }

  /**
   * Always creates a new instance of {@link MarkerKey} identifying this {@link org.jadice.jpeg2000.internal.marker.Marker}
   * in association with an given
   * identifier.
   *
   * @param id kind of identifier for the requested {@link MarkerSegment}.
   * @return a new instance of {@link MarkerKey} representing the {@link org.jadice.jpeg2000.internal.marker.Marker}
   * in association with the given
   * identifier.
   */
  public MarkerKey key(int id) {
    return new MarkerKey(this, id);
  }

  /**
   * Validates the given marker code value. The validation supports all marker codes defined in <i>ITU-T.800</i> and
   * <i>ITU-T.801</i>.
   *
   * @param code the (unsigned short) value that should be tested.
   * @return <code>true</code> if the marker code is valid as defined in <i>ITU-T.800</i> and <i>ITU-T.801</i>,
   * <code>false</code> if not.
   */
  public static boolean isValid(int code) {
    if ((code & 0xFF00) != 0xFF00) {
      return false;
    }

    return (0xFF4F <= code && code <= 0xFF6F) // ITU-T.800
        || (0xFF90 <= code && code <= 0xFF93) // ITU-T.800
        || (0xFF30 <= code && code <= 0xFF3F) // ITU-T.800
        || (0xFFD9 == code) // ITU-T.800
        || (0xFF70 <= code && code <= 0xFF79) // ITU-T.801
        || (0xFF5A <= code && code <= 0xFF5B) // ITU-T.801
        ;
  }

  @Override
  public String toString() {
    return name() + " (" + Integer.toHexString(code) + ", " + description + ")";
  }

  /**
   * Creates a corresponding {@link MarkerSegment} which is enable to read and write the marker segment. May return
   * <code>null</code> if the corresponding marker segment is currently not supported.
   *
   * @return a new instance of a {@link MarkerSegment}-implementation or <code>null</code> if currently not supported.
   */
  public MarkerSegment createMarkerSegment() {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Unsupported marker segment: " + name());
    }
    return null;
  }

  private static final QualifiedLogger LOGGER = LoggerFactory.getQualifiedLogger(Marker.class);

  private static final IntHashtable<Marker> markerByCode = new IntHashtable<>();

  static {
    for (Marker marker : Marker.values()) {
      markerByCode.put(marker.code, marker);
    }
  }

  /**
   * @param code the marker's code.
   * @return always one of the defined {@link org.jadice.jpeg2000.internal.marker.Marker}s. If the marker
   * code is unknown, not supported or not yet
   * implemented the return value will be {@link org.jadice.jpeg2000.internal.marker.Marker#RES}.
   */
  public static Marker byCode(int code) {
    final Marker marker = markerByCode.get(code);
    return marker != null ? marker : Marker.RES;
  }
}
