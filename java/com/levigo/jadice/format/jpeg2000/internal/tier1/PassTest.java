package com.levigo.jadice.format.jpeg2000.internal.tier1;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PassTest extends PassTestBase {

  @Parameters(name = "{0}")
  public static String[] data() {
    return new String[] { 
        "/com/levigo/jadice/format/jpeg2000/internal/tier1/p0_02.j2k.trace.json",
        "/com/levigo/jadice/format/jpeg2000/internal/tier1/p0_03.j2k.trace.json",
        "/com/levigo/jadice/format/jpeg2000/internal/tier1/crusoe01.trace.json", // Block with causal context
// "DSGVO Commit 70063732a1dc8d26722657eb95b8325fd164ce83"
//        "/com/levigo/jadice/format/jpeg2000/internal/tier1/porsche.j2k.trace.json"
    };
  }
  
  @Parameter(0)
  public String resourcePath;
  
  @Override
  protected String getTraceFileResourcePath() {
    return resourcePath;
  }
}
