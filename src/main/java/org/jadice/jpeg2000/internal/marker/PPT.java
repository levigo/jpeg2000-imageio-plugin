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
import org.jadice.jpeg2000.internal.io.SectorInputStream;
import org.jadice.jpeg2000.internal.param.PropertiesParameterInfo;

import java.io.IOException;

public class PPT extends AbstractMarkerSegment implements Comparable<PPT> {

  public int Lppt;
  public int Zppt;
  public SectorInputStream Ippt;

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lppt = source.readUnsignedShort();
    Zppt = source.readUnsignedByte();
    Ippt = new SectorInputStream(source, source.getStreamPosition(), Lppt - 3);
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lppt);
    sink.writeByte(Zppt);
    int read = 0;
    final byte buffer[] = new byte[65535];
    while ((read = Ippt.read(buffer)) > 0) {
      sink.write(buffer, 0, read);
    }
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {

  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lppt", Lppt, "TODO"));
    markerInfo.add(new PropertiesParameterInfo("Zppt", Zppt, "TODO"));
    markerInfo.add(new PropertiesParameterInfo("Ippt (length)", Ippt.length(), "TODO"));
  }

  @Override
  public Marker getMarker() {
    return Marker.PPT;
  }

  @Override
  public int compareTo(PPT ppt) {
    return this.Zppt - ppt.Zppt;
  }
}
