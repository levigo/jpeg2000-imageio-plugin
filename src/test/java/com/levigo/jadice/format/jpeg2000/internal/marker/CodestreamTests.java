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

import com.levigo.jadice.document.io.RandomAccessFileInputStream;

import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class CodestreamTests {

  public static ImageInputStream createSource(String path) throws URISyntaxException, IOException {
    return new RandomAccessFileInputStream(new File(CodestreamTests.class.getResource(path).toURI()));
  }

  public static SIZ createBasicSIZ() {
    final SIZ siz = new SIZ();
    siz.Rsiz = 0;
    siz.Xsiz = 128;
    siz.Ysiz = 128;
    siz.XOsiz = 0;
    siz.YOsiz = 0;
    siz.XTsiz = 128;
    siz.YTsiz = 128;
    siz.XTOsiz = 0;
    siz.YTOsiz = 0;
    siz.Csiz = 1;
    siz.Ssiz = new int[]{7};
    siz.XRsiz = new int[]{1};
    siz.YRsiz = new int[]{1};
    return siz;
  }

  public static COD createBasicCOD() {
    final COD cod = new COD();
    cod.Scod = 0;

    cod.SGcod_MCT = 0;
    cod.SGcod_layers = 1;
    cod.SGcod_order = 1;

    cod.SP_NL = 2;
    cod.SP_kernel = 0;
    cod.SP_modes = 0;
    cod.SP_xcb = 4;
    cod.SP_ycb = 4;

    return cod;
  }
}
