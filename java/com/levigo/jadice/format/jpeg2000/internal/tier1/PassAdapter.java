package com.levigo.jadice.format.jpeg2000.internal.tier1;

import java.io.IOException;

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
