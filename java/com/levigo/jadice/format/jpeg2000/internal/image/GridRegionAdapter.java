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
