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
