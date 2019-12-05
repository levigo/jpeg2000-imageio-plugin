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
import com.levigo.jadice.format.jpeg2000.internal.codestream.MarkerAccessGraph;
import com.levigo.jadice.format.jpeg2000.internal.codestream.MarkerAccessNode;
import com.levigo.jadice.format.jpeg2000.internal.codestream.MarkerSegmentContainer;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

public abstract class Qxx extends AbstractMarkerSegment {
  /** No quantization. */
  public static final int VALUE_NO_QUANTIZATION = 0;

  /**
   * Scalar derived (values signalled for <i>N<sub>L</sub>LL</i> sub-band only). Use <i>ITU-T.800, Equation (E-5)</i>.
   */
  public static final int VALUE_SCALAR_DERIVED = 1 << 0;

  /**
   * Scalar expounded (values signalled for each sub-band). There are as many step sizes signaled as there are
   * sub-bands.
   */
  public static final int VALUE_SCALAR_EXPOUNDED = 1 << 1;

  /**
   * Variable deadzone and scalar derived (values signaled for <i>N<sub>L</sub>LL</i> sub-band only). Use <i>ITU-T.800,
   * Equation E-5</i>.
   */
  public static final int VALUE_DEADZONE_AND_SCALAR_DERIVED = 1 << 1 | 1 << 0;

  /**
   * Variable deadzone derived and scalar expounded (values signaled for each sub-band). There are as many step sizes
   * signaled as there are sub-bands.
   */
  public static final int VALUE_DEADZONE_DERIVED_AND_SCALAR_EXPOUNDED = 1 << 2;

  /**
   * Variable deadzone and scalar expounded (values signaled for each sub-band). There are as many step sizes signaled
   * as there are sub-bands.
   */
  public static final int VALUE_DEADZONE_AND_SCALAR_EXPOUNDED = 1 << 2 | 1 << 1;

  /**
   * Trellis coded quantization derived (values signaled for <i>N<sub>L</sub>LL</i> sub-band only). Use <i>ITU-T.800,
   * Equation E-5</i>.
   */
  public static final int VALUE_TRELLIS_DERIVED = 1 << 3 | 1 << 0;

  /**
   * Trellis coded quantization expounded (values signaled for each sub-band). There are as many step sizes signaled as
   * there are sub-bands.
   */
  public static final int VALUE_TRELLIS_EXPOUNDED = 1 << 3 | 1 << 1;

  /** Number of guard bits: 0 to 7. */
  public byte Sqxx_guardbits;

  /** Quantization style for one or all components depending on the marker segment's scope. */
  public byte Sqxx_style;

  /** Deadzone adjustment. */
  public int[] SPqxx_dzone;

  /** Exponent ε<sub><i>b</i></sub> of the quantization step size value (see <i>ITU-T.800, Equation (E-3)</i>). */
  public int[] SPqxx_exp;

  /** Mantissa μ<sub><i>b</i></sub> of the quantization step size value (see <i>ITU-T.800, Equation (E-3)</i>). */
  public int[] SPqxx_man;

  protected static int readMantissa(ImageInputStream source) throws IOException {
    return (int) (source.readBits(11) & 0x7FF);
  }

  protected static int readExponent(ImageInputStream source) throws IOException {
    return (int) (source.readBits(5) & 0x1F);
  }

  public static Qxx accessQxx(Codestream codestream, Tile tile, int compIdx) throws JPEG2000Exception {
    final MarkerSegmentContainer mainSegments = codestream.markers;
    final MarkerSegmentContainer tileSegments = tile.markers;

    final MarkerAccessGraph accessGraph = new MarkerAccessGraph();
    accessGraph.add(new MarkerAccessNode(tileSegments, Marker.QPC.key(compIdx)));
    accessGraph.add(new MarkerAccessNode(tileSegments, Marker.QPD.key()));
    accessGraph.add(new MarkerAccessNode(tileSegments, Marker.QCC.key(compIdx)));
    accessGraph.add(new MarkerAccessNode(tileSegments, Marker.QCD.key()));
    accessGraph.add(new MarkerAccessNode(mainSegments, Marker.QPC.key(compIdx)));
    accessGraph.add(new MarkerAccessNode(mainSegments, Marker.QPD.key()));
    accessGraph.add(new MarkerAccessNode(mainSegments, Marker.QCC.key(compIdx)));
    accessGraph.add(new MarkerAccessNode(mainSegments, Marker.QCD.key()));

    final MarkerSegment markerSegment = accessGraph.access();
    if (markerSegment == null) {
      throw new JPEG2000Exception(CodestreamMessages.MISSING_CODING_MARKER_SEGMENT);
    }

    return (Qxx) markerSegment;
  }

  public static Qxx accessQCx(Codestream codestream, Tile tile, int compIdx) throws JPEG2000Exception {
    final MarkerSegmentContainer mainSegments = codestream.markers;
    final MarkerSegmentContainer tileSegments = tile.markers;

    final MarkerAccessGraph qcGraph = new MarkerAccessGraph();
    qcGraph.add(new MarkerAccessNode(tileSegments, Marker.QCC.key(compIdx)));
    qcGraph.add(new MarkerAccessNode(tileSegments, Marker.QCD.key()));
    qcGraph.add(new MarkerAccessNode(mainSegments, Marker.QCC.key(compIdx)));
    qcGraph.add(new MarkerAccessNode(mainSegments, Marker.QCD.key()));

    final MarkerSegment markerSegment = qcGraph.access();
    if (markerSegment == null) {
      throw new JPEG2000Exception(CodestreamMessages.MISSING_CODING_MARKER_SEGMENT);
    }

    return (Qxx) markerSegment;
  }
}
