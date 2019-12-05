package com.levigo.jadice.format.jpeg2000.internal.tier2;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PacketHeaderReaderTest extends PacketHeaderReaderTestBase {

  @Parameters(name = "{index}: {1} with trace data: {0}")
  public static String[][] data() {
    return new String[][]{
        {"/com/levigo/jadice/format/jpeg2000/internal/tier2/p0_01.j2k.packet.trace.json",
            "/codestreams/profile0/p0_01.j2k"
        },
        {"/com/levigo/jadice/format/jpeg2000/internal/tier2/p0_02.j2k.packet.trace.json",
            "/codestreams/profile0/p0_02.j2k"
        },
        /*{"/com/levigo/jadice/format/jpeg2000/internal/tier2/p0_03.j2k.packet.trace.json",
            "/codestreams/profile0/p0_03.j2k"
        },*/
        {"/com/levigo/jadice/format/jpeg2000/internal/tier2/p0_04.j2k.packet.trace.json",
            "/codestreams/profile0/p0_04.j2k"
        },
        {"/com/levigo/jadice/format/jpeg2000/internal/tier2/p0_16.j2k.packet.trace.json",
            "/codestreams/profile0/p0_16.j2k"
        },
        {"/com/levigo/jadice/format/jpeg2000/internal/tier2/virgindigital_50.j2k.packet.trace.json",
            "/files/virgindigital_50.j2k"
        },
        {"/com/levigo/jadice/format/jpeg2000/internal/tier2/crusoe01.packet.trace.json",
            "/files/Robinson_Crusoe-page03-CTi0.jp2"
        },
// "DSGVO Commit 70063732a1dc8d26722657eb95b8325fd164ce83"
//        {"/com/levigo/jadice/format/jpeg2000/internal/tier2/porsche.j2k.packet.trace.json",
//            "/files/porsche.j2k"
//        },
        
    };
  }

  @Parameter(0)
  public String traceDataPath;

  @Parameter(1)
  public String subjectPath;

  @Override
  protected String getTraceFileResourcePath() {
    return traceDataPath;
  }
  
  @Override
  protected String getSubjectPath() {
    return subjectPath;
  }
}
