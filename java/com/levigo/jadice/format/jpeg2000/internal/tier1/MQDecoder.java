package com.levigo.jadice.format.jpeg2000.internal.tier1;

import java.io.IOException;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;

@Refer(to = Spec.J2K_CORE, page = 64, section = "Annex C", called = "Arithmetic Entropy Coding")
public interface MQDecoder {

  int decode(ContextContainer cx) throws IOException;

  int decodeRaw() throws IOException;

  boolean checkPredictableTermination() throws IOException;

}
