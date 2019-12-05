package com.levigo.jadice.format.jpeg2000.internal.tier1;

import org.junit.jupiter.api.Disabled;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@Disabled("DSGVO Commit 70063732a1dc8d26722657eb95b8325fd164ce83")
//@RunWith(Parameterized.class)
public class BlockDecodingSpyTest extends BlockDecodingSpyTestBase {

  @Parameters
  public static Object[][] parameters() {
    return new Object[][] {
        {"/com/levigo/jadice/format/jpeg2000/internal/tier1/porsche.j2k.mqtrace.json", "/files/porsche.j2k"}
    };
  }
  
  @Parameter(0)
  public String traceFileResourcePath;

  @Parameter(1)
  public String inputFileResourcePath;
  
  @Override
  protected String getTraceFileResourcePath() {
    return traceFileResourcePath;
  }

  @Override
  protected String getInputFileResourcePath() {
    return inputFileResourcePath;
  }
}
