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
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Defined in <i>ITU-T.800, A.9.2</i>.
 * <p>
 * <b>Function:</b> Allows unstructured data in the main and tile-part header.
 * <p>
 * <b>Usage:</b> Main and tile-part headers. Repeatable as many times as desired in either or both the main or
 * tile-part headers. This marker segment has no effect on decoding the codestream.
 * <p>
 * <b>Length:</b> Variable depending on the length of the message.
 */
public class COM extends AbstractMarkerSegment {

  public static final int REGISTRATION_ENCODING_BINARY = 0;
  public static final int REGISTRATION_ENCODING_ISO_8859_15 = 1;

  /** Length of marker segment in bytes (not including the marker). */
  public int Lcom;

  /** Registration value of the marker segment. In other words this parameter specifies the encoding of {@link #Ccom}. */
  public int Rcom;

  /** Bytes of unstructured data. */
  public byte[] Ccom;

  @Override
  public Marker getMarker() {
    return Marker.COM;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lcom = source.readUnsignedShort();
    Rcom = source.readUnsignedShort();
    Ccom = new byte[Lcom - 4];
    source.readFully(Ccom);
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lcom);
    sink.writeShort(Rcom);
    sink.write(Ccom);
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    Validate.inRange("Lcom", Lcom, 5, 65535);
    Validate.inRange("Rcom", Rcom, 0, 1);
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lcom", Lcom, "COM.L"));
    markerInfo.add(new PropertiesParameterInfo("Rcom", Rcom, "COM.R"));
    switch(Rcom) {
      case REGISTRATION_ENCODING_ISO_8859_15:
        markerInfo.add(new PropertiesParameterInfo("Ccom", new String(Ccom), "COM.C"));
        break;
      case REGISTRATION_ENCODING_BINARY:
        markerInfo.add(new PropertiesParameterInfo("Ccom", Arrays.toString(Ccom), "COM.C"));
        break;
    }
  }

  @Override
  public String toString() {
    if (Ccom != null) {
      if (Rcom == REGISTRATION_ENCODING_ISO_8859_15) {
        try {
          return new String(Ccom, "ISO-8859-15");
        } catch (UnsupportedEncodingException e) {
          // Fall through.
        }
      }
      return Arrays.toString(Ccom); // Either ISO/IEC 8859-15 is not present or no encoding specified.
    }
    return super.toString();
  }
}
