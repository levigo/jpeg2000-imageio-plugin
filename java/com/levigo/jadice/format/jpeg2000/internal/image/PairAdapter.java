package com.levigo.jadice.format.jpeg2000.internal.image;

import java.io.IOException;

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
