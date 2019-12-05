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

import static org.jadice.jpeg2000.internal.debug.BasicProtocolElement.newElement;

import java.util.ArrayList;
import java.util.Collection;

public abstract class ProtocolBase implements Protocol {

  private final Collection<ProtocolToken> knownTokens;

  private final Collection<ProtocolListener> listeners;

  private int numElements;

  protected ProtocolBase(Collection<ProtocolToken> knownTokens) {
    this.knownTokens = knownTokens;

    listeners = new ArrayList<ProtocolListener>(3);
    numElements = 0;
  }

  @Override
  public byte tokenLength() {
    return DEFAULT_TOKEN_LENGTH;
  }

  @Override
  public Collection<ProtocolToken> knownTokens() {
    return knownTokens;
  }

  @Override
  public void addProtocolListener(ProtocolListener listener) {
    listeners.add(listener);
  }

  protected void createAndNotify(ProtocolToken token, Parameter parameter) {
    final BasicProtocolElement protocolElement = newElement(token, parameter);
    for (ProtocolListener listener : listeners) {
      listener.newProtocolElement(protocolElement);
    }
    numElements++;
  }

  protected int getNumElements() {
    return numElements;
  }
}
