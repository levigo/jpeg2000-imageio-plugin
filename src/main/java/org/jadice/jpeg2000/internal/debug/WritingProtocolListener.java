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
package org.jadice.jpeg2000.internal.debug;

import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.levigo.jadice.document.internal.LogMessages;
import org.jadice.util.log.qualified.QualifiedLogger;

public class WritingProtocolListener implements ProtocolListener, Closeable {

  private static final QualifiedLogger LOGGER = getQualifiedLogger(WritingProtocolListener.class);

  private final ImageOutputStream stream;

  public WritingProtocolListener(OutputStream stream) throws IOException {
    this.stream = new MemoryCacheImageOutputStream(stream);
  }

  @Override
  public void newProtocolElement(ProtocolElement element) {
    try {
      element.token().write(stream);
      element.parameter().write(stream);
    } catch (IOException e) {
      LOGGER.fatal(LogMessages.IO_EXCEPTION, e);
    }
  }

  public void close() {
    try {
      stream.close();
    } catch (IOException e) {
      LOGGER.fatal(LogMessages.IO_EXCEPTION, e);
    }
  }
}
