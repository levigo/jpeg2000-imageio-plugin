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
package org.jadice.jpeg2000.internal.image;

import java.io.IOException;

import org.jadice.jpeg2000.internal.image.Pair;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class PairAdapter extends TypeAdapter<Pair> {
  @Override
  public void write(JsonWriter out, Pair value) throws IOException {
    out.beginArray();
    out.value(value.x);
    out.value(value.y);
    out.endArray();
  }

  @Override
  public Pair read(JsonReader in) throws IOException {
    in.beginArray();

    final Pair pair = new Pair();
    pair.x = in.nextInt();
    pair.y = in.nextInt();

    in.endArray();

    return pair;
  }
}
