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

import static org.jadice.util.base.Objects.requireNotNull;

public class BasicProtocolElement implements ProtocolElement {

  private final ProtocolToken token;
  private final Parameter parameter;

  public static BasicProtocolElement newElement(ProtocolToken token, Parameter parameter) {
    return new BasicProtocolElement(token, parameter);
  }

  public BasicProtocolElement(ProtocolToken token, Parameter parameter) {
    this.token = requireNotNull("token", token);
    this.parameter = requireNotNull("parameter", parameter);
  }

  @Override
  public ProtocolToken token() {
    return token;
  }

  @Override
  public Parameter parameter() {
    return parameter;
  }

  @Override
  public boolean matches(Object o) {
    if(!(o instanceof ProtocolElement)) {
      return false;
    }

    final ProtocolElement other = (ProtocolElement) o;
    return token.matches(other.token()) && parameter.matches(other.parameter());
  }

  @Override
  public String toString() {
    return token + " {" + parameter + "}";
  }
}
