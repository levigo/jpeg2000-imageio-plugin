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

import java.io.IOException;

/**
 * Defined in <i>ITU-T.800, A.4.4</i>
 * <p>
 * <b>Function:</b> Indicates the end of the codestream.
 * <p>
 * <i><b>Note 1:</b> This marker shares the same code as the EOI marker in <i>ITU-T Rec. T.81 | ISO/IEC
 * 10918-1</i>.</i>
 * <p>
 * <b>Usage:</b> Shall be the last marker in a codestream. There shall be one EOC per codestream.
 * <p>
 * <i><b>Note 2:</b> In the case a file has been corrupted, it is possible that a decoder could extract much useful
 * compressed image data without encountering an {@link EOC} marker.</i>
 * <p>
 * <b>Length:</b> Fixed.
 */
public class EOC implements MarkerSegment {

  @Override
  public Marker getMarker() {
    return Marker.EOC;
  }

  @Override
  public MarkerKey getMarkerKey() {
    // This won't be effectively called. Don't complain and provide a correct key anyway.
    return getMarker().key();
  }

  @Override
  public void read(ImageInputStream source, Codestream codestream, boolean validate)
      throws IOException, JPEG2000Exception {
    // no parameters to read
  }

  @Override
  public void read(ImageInputStream source, boolean validate) throws IOException, JPEG2000Exception {
    read(source, null, validate);
  }

  @Override
  public void write(ImageOutputStream sink, Codestream codestream, boolean validate)
      throws IOException, JPEG2000Exception {
    // no parameters to write
  }

}
