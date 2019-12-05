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
import com.levigo.jadice.format.jpeg2000.internal.Validate;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.param.DirectParameterInfo;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Defined in <i>ITU-T.800, A.9.1</i>.
 * <p>
 * <b>Function:</b> Allows specific registration of components with respect to each other. For coding purposes the
 * samples of components are considered to be located at reference grid points that are integer multiples of {@link
 * SIZ#XRsiz} and {@link SIZ#YRsiz} (see <i>ITU-T.800, A.5.1</i>). However, this may be inappropriate for rendering the
 * image. The {@link CRG} marker segment describes the "centre of mass" of each component's samples with respect to the
 * separation. This marker segment has no effect on decoding the codestream.
 * <p>
 * <i>Note:</i> This component registration offset is with respect to the image offset ({@link SIZ#XOsiz} and {@link
 * SIZ#YOsiz}) and the component separation ({@link SIZ#XRsiz}<sup>i</sup> and {@link SIZ#YRsiz}<sup>i</sup>). For
 * example, the horizontal reference grid point for the left-most samples of component c is
 * <p>
 * <code>XRsiz<sup>c</sup> ceil(XOsiz / XRsiz<sup>c</sup>)</code>
 * <p>(Likewise for the vertical direction.) The horizontal offset denoted in this marker segment is in addition to
 * this offset.
 * <p>
 * <b>Usage:</b> Main header only. Only one {@link CRG} may be used in the main header and is applicable for all tiles.
 * <p>
 * <b>Length:</b> Variable depending on the number of components.
 * <p>
 */
public class CRG extends AbstractMarkerSegment {

  /** Length of marker segment in bytes (not including the marker). */
  public int Lcrg;

  /**
   * Value of the horizontal offset, in units of <code>1/65536</code> of the horizontal separation
   * <code>XRsiz<sup>i</sup></code>, for the <i>i</i>th component.
   */
  public int[] Xcrg;

  /**
   * Value of the vertical offset, in units of <code>1/65536</code> of the vertical separation
   * <code>YRsiz<sup>i<sup></code>, for the <i>i</i>th component.
   */
  public int[] Ycrg;

  @Override
  public Marker getMarker() {
    return Marker.CRG;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lcrg = source.readUnsignedShort();

    Xcrg = new int[codestream.numComps];
    Ycrg = new int[codestream.numComps];
    for (int c = 0; c < codestream.numComps; c++) {
      Xcrg[c] = source.readUnsignedShort();
      Ycrg[c] = source.readUnsignedShort();
    }
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lcrg);
    for (int c = 0; c < codestream.numComps; c++) {
      sink.writeShort(Xcrg[c]);
      sink.writeShort(Ycrg[c]);
    }
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    Validate.inRange("Lcrg", Lcrg, 6, 65534);

    for (int i = 0; i < Xcrg.length; i++) {
      Validate.inRange("Xcrg", Xcrg[i], 0, 65535);
    }
    for (int i = 0; i < Ycrg.length; i++) {
      Validate.inRange("Ycrg", Ycrg[i], 0, 65535);
    }
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lcrg", Lcrg, "CRG.L"));

    markerInfo.add(new PropertiesParameterInfo("Xcrg", "CRG.X"));
    for (int i = 0; i < Xcrg.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", Xcrg[i]));
    }

    markerInfo.add(new PropertiesParameterInfo("Ycrg", "CRG.Y"));
    for (int i = 0; i < Ycrg.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", Ycrg[i]));
    }
  }
}
