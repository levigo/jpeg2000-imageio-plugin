package com.levigo.jadice.format.jpeg2000.internal.image;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class RegionAdapter extends TypeAdapter<Region> {
  @Override
  public void write(JsonWriter out, Region value) throws IOException {
    out.beginArray();
    out.value(value.pos.x);
    out.value(value.pos.y);
    out.value(value.size.x);
    out.value(value.size.y);
    out.endArray();
  }

  @Override
  public Region read(JsonReader in) throws IOException {
    in.beginArray();

    final Region region = new Region();
    region.pos.x = in.nextInt();
    region.pos.y = in.nextInt();
    region.size.x = in.nextInt();
    region.size.y = in.nextInt();

    in.endArray();

    return region;
  }
}
