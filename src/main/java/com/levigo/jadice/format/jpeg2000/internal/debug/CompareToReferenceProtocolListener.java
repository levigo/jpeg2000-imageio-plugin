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
package com.levigo.jadice.format.jpeg2000.internal.debug;

import static com.levigo.jadice.format.jpeg2000.internal.debug.BasicProtocolElement.newElement;
import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.levigo.jadice.document.internal.LogMessages;
import org.jadice.util.log.qualified.QualifiedLogger;

public class CompareToReferenceProtocolListener implements ProtocolListener {

  private static final QualifiedLogger LOGGER = getQualifiedLogger(CompareToReferenceProtocolListener.class);

  private final ImageInputStream referenceProtocolInput;
  private final Protocol protocol;

  private long numCompared;

  public CompareToReferenceProtocolListener(FileInputStream referenceInput, Protocol protocol)
      throws FileNotFoundException {
    this.referenceProtocolInput = new MemoryCacheImageInputStream(referenceInput);
    this.protocol = protocol;
    numCompared = 0;
  }

  @Override
  public void newProtocolElement(ProtocolElement myElement) {
    try {
      final long token = referenceProtocolInput.readBits(protocol.tokenLength());
      for (ProtocolToken protocolToken : protocol.knownTokens()) {
        if (protocolToken.matches(token)) {
          final ParameterFactory parameterFactory = protocolToken.getParameterFactory();
          final Parameter referenceParameter = parameterFactory.create();
          referenceParameter.read(referenceProtocolInput);
          final BasicProtocolElement referenceElement = newElement(protocolToken, referenceParameter);
          if (!myElement.matches(referenceElement)) {
            System.err.println(numCompared + ". element differs:\n    actual: " + myElement
                + "\n  expected: " + referenceElement);
          }
          numCompared++;
        }
      }

    } catch (IOException e) {
      LOGGER.fatal(LogMessages.IO_EXCEPTION, e);
    }
  }
}
