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
package com.levigo.jadice.format.jpeg2000.internal;

import java.io.IOException;
import java.util.Map;

import com.levigo.jadice.document.DocumentLayer;
import com.levigo.jadice.document.FormatReader;
import com.levigo.jadice.document.FormatReaderProcessor;
import com.levigo.jadice.document.JadiceException;
import com.levigo.jadice.document.PageSegmentSource;
import com.levigo.jadice.document.internal.read.DefaultPageSegmentSource;
import com.levigo.jadice.document.read.ReaderParameters;
import com.levigo.jadice.format.jpeg2000.JPEG2000Format;

public final class JPEG2000FormatReader extends FormatReader {

  @Override
  protected void doRead(ReaderParameters parameters, FormatReaderProcessor processor)
      throws IOException, JadiceException {
    processor.readStarted(0);

    final Map<String, Object> properties = parameters.getProperties();
    if (properties != null) {
      processor.processPageProperties(0, properties);
    }

    final PageSegmentSource source = new DefaultPageSegmentSource(parameters, new JPEG2000Format(), 0);
    final JPEG2000PageSegment pageSegment = new JPEG2000PageSegment(source);
    processor.processPageSegment(pageSegment, 0, DocumentLayer.DEFAULT);
  }

}
