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
package org.jadice.jpeg2000;


import java.io.IOException;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import com.levigo.jadice.document.Format;
import com.levigo.jadice.document.FormatReader;
import com.levigo.jadice.document.ProductInformation;
import com.levigo.jadice.document.io.SeekableInputStream;

import org.jadice.jpeg2000.internal.JPEG2000FormatReader;
import org.jadice.util.log.Logger;
import org.jadice.util.log.LoggerFactory;

public class JPEG2000Format extends Format {

  private static final String JPEG2000 = "JPEG2000";

  private static final Logger LOGGER = LoggerFactory.getLogger(JPEG2000Format.class);

  @Override
  public String[] getDefaultExtensions() {
    return new String[]{
        "jp2", "j2k", "JP2", "J2K", "jpf", "jpg2", "jpx"
    };
  }

  @Override
  public String getDescription() {
    return JPEG2000;
  }

  @Override
  public String getName() {
    return JPEG2000;
  }

  @Override
  public String getVersion() {
    return ProductInformation.getVersion();
  }

  @Override
  public MimeType getMimeType() {
    try {
      return new MimeType("image", "jpx");
    } catch (MimeTypeParseException e) {
      // This never happens, as the MimeType parser cannot be as dumb.
      LOGGER.error(e.getMessage(), e);
    }

    return null;
  }

  @Override
  public boolean matches(SeekableInputStream stream) throws IOException {
    return JPEG2000Matcher.isFileFormat(stream) || JPEG2000Matcher.isCodestream(stream);
  }

  @Override
  public FormatReader createReader() {
    return new JPEG2000FormatReader();
  }

}
