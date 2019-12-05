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
package org.jadice.jpeg2000.internal.tier1;

import java.io.IOException;

import org.jadice.jpeg2000.internal.tier1.CleanupPass;
import org.jadice.jpeg2000.internal.tier1.Pass;
import org.jadice.jpeg2000.internal.tier1.RefinementPass;
import org.jadice.jpeg2000.internal.tier1.SignificancePass;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class PassAdapter extends TypeAdapter<Pass> {

  @Override
  public void write(JsonWriter out, Pass value) throws IOException {
    
  }

  @Override
  public Pass read(JsonReader in) throws IOException {
    final String s = in.nextString();
    if("Cleanup".equalsIgnoreCase(s)) {
      return new CleanupPass();
    } else if("Significance".equalsIgnoreCase(s)) {
      return new SignificancePass();
    } else if("Refinement".equalsIgnoreCase(s)) {
      return new RefinementPass();
    }

    throw new IllegalArgumentException("pass=" + s);
  }
}
