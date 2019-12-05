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
package com.levigo.jadice.format.jpeg2000.internal.tier1;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class CtxExpectationAdapter extends TypeAdapter<CtxExpectation> {
  @Override
  public void write(JsonWriter out, CtxExpectation value) throws IOException {
    
  }

  @Override
  public CtxExpectation read(JsonReader in) throws IOException {
    if(in.peek() != JsonToken.STRING) {
      throw new IllegalArgumentException("token must be STRING, but was " + in.peek().name());
    }

    final String s = in.nextString();
    final String[] parts = s.split("#");
    if("RUN".equalsIgnoreCase(parts[0])) {
      return new CtxExpectation(States.KAPPA_RUN);
    } else if("SIG".equalsIgnoreCase(parts[0])) {
      return new CtxExpectation(States.KAPPA_SIG_BASE + Integer.parseInt(parts[1]));
    } else if("SIGN".equalsIgnoreCase(parts[0])) {
      return new CtxExpectation(States.KAPPA_SIGN_BASE + Integer.parseInt(parts[1]));
    } else if("MAG".equalsIgnoreCase(parts[0])) {
      return new CtxExpectation(States.KAPPA_MAG_BASE + Integer.parseInt(parts[1]));
    } else if("UNI".equalsIgnoreCase(parts[0])) {
      return new CtxExpectation(States.KAPPA_UNI);
    }

    throw new IllegalArgumentException("state=" + s);
  }
}
