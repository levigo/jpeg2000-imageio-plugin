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

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

public interface MarkerSegment {

  Marker getMarker();

  MarkerKey getMarkerKey();

  void read(ImageInputStream source, Codestream codestream, boolean validate) throws IOException, JPEG2000Exception;

  void read(ImageInputStream source, boolean validate) throws IOException, JPEG2000Exception;

  void write(ImageOutputStream sink, Codestream codestream, boolean validate) throws IOException, JPEG2000Exception;

}
