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
package com.levigo.jadice.format.jpeg2000.internal.image;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class GridRegionAdapter extends TypeAdapter<GridRegion> {
  private static final RegionAdapter REGION_ADAPTER = new RegionAdapter();
  
  @Override
  public void write(JsonWriter out, GridRegion value) throws IOException {
    REGION_ADAPTER.write(out, value.absolute());
  }

  @Override
  public GridRegion read(JsonReader in) throws IOException {
    final Region region = REGION_ADAPTER.read(in);
    return new DefaultGridRegion(region);
  }
}
