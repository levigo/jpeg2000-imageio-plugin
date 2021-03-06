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

import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

import java.io.IOException;
import java.io.InputStream;

import com.levigo.jadice.document.io.IOUtils;
import com.levigo.jadice.document.io.SeekableInputStream;
import org.jadice.util.log.qualified.QualifiedLogger;

public class Tests {

  private static final QualifiedLogger LOGGER = getQualifiedLogger(Tests.class);

  public static SeekableInputStream openResource(String fullQualifiedResourcePath) throws IOException {
    LOGGER.info("Searching for " + fullQualifiedResourcePath);
    final InputStream inputStream = Tests.class.getResourceAsStream(fullQualifiedResourcePath);
    LOGGER.info("Found " + inputStream);
    return IOUtils.wrap(inputStream);
  }

}
