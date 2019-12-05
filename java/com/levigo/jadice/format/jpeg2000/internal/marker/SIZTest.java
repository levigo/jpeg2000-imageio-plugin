package com.levigo.jadice.format.jpeg2000.internal.marker;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class SIZTest extends MarkerTestBase<SIZ> {

  @Parameter(1)
  public int Lsiz;

  @Parameter(2)
  public int Rsiz;

  @Parameter(3)
  public int Xsiz;

  @Parameter(4)
  public int Ysiz;

  @Parameter(5)
  public int XOsiz;

  @Parameter(6)
  public int YOsiz;

  @Parameter(7)
  public int XTsiz;

  @Parameter(8)
  public int YTsiz;

  @Parameter(9)
  public int XTOsiz;

  @Parameter(10)
  public int YTOsiz;

  @Parameter(11)
  public int Csiz;

  @Parameter(12)
  public int[] Ssiz;

  @Parameter(13)
  public int[] XRsiz;

  @Parameter(14)
  public int[] YRsiz;

  @Parameters(name = "{0}")
  public static Object[][] data() {
    return new Object[][]{
        // @formatter:off
        {"siz/siz_p0_01", 41, 1, 128, 128, 0, 0, 128, 128, 0, 0, 1, new int[]{7}, new int[]{1}, new int[]{1}},
        {"siz/siz_p0_02", 41, 1, 127, 126, 0, 0, 127, 126, 0, 0, 1, new int[]{7}, new int[]{2}, new int[]{1}},
        {"siz/siz_p0_03", 41, 1, 256, 256, 0, 0, 128, 128, 0, 0, 1, new int[]{131}, new int[]{1}, new int[]{1}},
        {"siz/siz_p0_04", 47, 1, 640, 480, 0, 0, 640, 480, 0, 0, 3, new int[]{7, 7, 7}, new int[]{1, 1, 1}, new int[]{1, 1, 1}},
        {"siz/siz_p0_05", 50, 1, 1024, 1024, 0, 0, 1024, 1024, 0, 0, 4, new int[]{7, 7, 7, 7}, new int[]{1, 1, 2, 2}, new int[]{1, 1, 2, 2}},
        {"siz/siz_p0_06", 50, 2, 513, 129, 0, 0, 513, 129, 0, 0, 4, new int[]{11, 11, 11, 11}, new int[]{1, 2, 1, 2}, new int[]{1, 1, 2, 2}},
        {"siz/siz_p0_07", 47, 1, 2048, 2048, 0, 0, 128, 128, 0, 0, 3, new int[]{139, 139, 139}, new int[]{1, 1, 1}, new int[]{1, 1, 1}},
        {"siz/siz_p0_08", 47, 1, 513, 3072, 0, 0, 513, 3072, 0, 0, 3, new int[]{139, 139, 139}, new int[]{1, 1, 1}, new int[]{1, 1, 1}},
        {"siz/siz_p0_09", 41, 0, 17, 37, 0, 0, 17, 37, 0, 0, 1, new int[]{7}, new int[]{1}, new int[]{1}},
        {"siz/siz_p0_10", 47, 1, 256, 256, 0, 0, 128, 128, 0, 0, 3, new int[]{7, 7, 7}, new int[]{4, 4, 4}, new int[]{4, 4, 4}},
        {"siz/siz_p0_11", 41, 1, 128, 1, 0, 0, 128, 128, 0, 0, 1, new int[]{7}, new int[]{1}, new int[]{1}},
        {"siz/siz_p0_12", 41, 1, 3, 5, 0, 0, 3, 5, 0, 0, 1, new int[]{7}, new int[]{1}, new int[]{1}},
        {"siz/siz_p0_14", 47, 0, 49, 49, 0, 0, 49, 49, 0, 0, 3, new int[]{7, 7, 7}, new int[]{1, 1, 1}, new int[]{1, 1, 1}},
        {"siz/siz_p0_15", 41, 1, 256, 256, 0, 0, 128, 128, 0, 0, 1, new int[]{131}, new int[]{1}, new int[]{1}},
        {"siz/siz_p0_16", 41, 0, 128, 128, 0, 0, 128, 128, 0, 0, 1, new int[]{7}, new int[]{1}, new int[]{1}},
        {"siz/siz_p1_01", 41, 2, 127, 227, 5, 128, 127, 126, 1, 101, 1, new int[]{7}, new int[]{2}, new int[]{1}},
        {"siz/siz_p1_02", 47, 2, 640, 480, 0, 0, 640, 480, 0, 0, 3, new int[]{7, 7, 7}, new int[]{1, 1, 1}, new int[]{1, 1, 1}},
        {"siz/siz_p1_03", 50, 2, 1024, 1024, 0, 0, 1024, 1024, 0, 0, 4, new int[]{7, 7, 7, 7}, new int[]{1, 1, 2, 2}, new int[]{1, 1, 2, 2}},
        {"siz/siz_p1_04", 41, 2, 1024, 1024, 0, 0, 128, 128, 0, 0, 1, new int[]{11}, new int[]{1}, new int[]{1}},
        {"siz/siz_p1_05", 47, 2, 529, 524, 17, 12, 37, 37, 8, 2, 3, new int[]{7, 7, 7}, new int[]{1, 1, 1}, new int[]{1, 1, 1}},
        {"siz/siz_p1_06", 47, 2, 12, 12, 0, 0, 3, 3, 0, 0, 3, new int[]{7, 7, 7}, new int[]{1, 1, 1}, new int[]{1, 1, 1}},
        {"siz/siz_p1_07", 44, 2, 12, 12, 4, 0, 12, 12, 4, 0, 2, new int[]{7, 7}, new int[]{4, 1}, new int[]{1, 1}},
        {"siz/siz_16bit", 41, 0, 512, 512, 0, 0, 512, 512, 0, 0, 1, new int[]{15}, new int[]{1}, new int[]{1}},
        {"siz/siz_greyscale", 41, 0, 760, 249, 0, 0, 760, 249, 0, 0, 1, new int[]{7}, new int[]{1}, new int[]{1}},
        {"siz/siz_google-logo", 47, 0, 1800, 750, 0, 0, 1800, 750, 0, 0, 3, new int[]{7, 7, 7}, new int[]{1, 1, 1}, new int[]{1, 1, 1}},
        {"siz/siz_house", 50, 0, 100, 100, 0, 0, 100, 100, 0, 0, 4, new int[]{7, 7, 7, 7}, new int[]{1, 1, 1, 1}, new int[]{1, 1, 1, 1}},
        {"siz/siz_PSP", 41, 0, 31848, 77994, 0, 0, 31848, 77994, 0, 0, 1, new int[]{9}, new int[]{1}, new int[]{1}},
        {"siz/siz_p0_13", 809, 1, 1, 1, 0, 0, 1, 1, 0, 0, 257,
            new int[]{7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
            },
            new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            },
            new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            }
        }
        // @formatter:on
    };
  }

  @Override
  protected Marker marker() {
    return Marker.SIZ;
  }
  
  @Override
  protected void inspect(SIZ underTest) {
    assertEquals("Lsiz", Lsiz, underTest.Lsiz);
    assertEquals("Rsiz", Rsiz, underTest.Rsiz);
    assertEquals("Xsiz", Xsiz, underTest.Xsiz);
    assertEquals("Ysiz", Ysiz, underTest.Ysiz);
    assertEquals("XOsiz", XOsiz, underTest.XOsiz);
    assertEquals("YOsiz", YOsiz, underTest.YOsiz);
    assertEquals("XTsiz", XTsiz, underTest.XTsiz);
    assertEquals("YTsiz", YTsiz, underTest.YTsiz);
    assertEquals("XTOsiz", XTOsiz, underTest.XTOsiz);
    assertEquals("YTOsiz", YTOsiz, underTest.YTOsiz);
    assertEquals("Csiz", Csiz, underTest.Csiz);

    assertArrayEquals("Ssiz", Ssiz, underTest.Ssiz);
    assertArrayEquals("XRsiz", XRsiz, underTest.XRsiz);
    assertArrayEquals("YRsiz", YRsiz, underTest.YRsiz);
  }

  @Override
  protected SIZ prepareMarker() {
    final SIZ siz = new SIZ();
    siz.Lsiz = Lsiz;
    siz.Rsiz = Rsiz;
    siz.Xsiz = Xsiz;
    siz.Ysiz = Ysiz;
    siz.XOsiz = XOsiz;
    siz.YOsiz = YOsiz;
    siz.XTsiz = XTsiz;
    siz.YTsiz = YTsiz;
    siz.XTOsiz = XTOsiz;
    siz.YTOsiz = YTOsiz;
    siz.Csiz = Csiz;
    siz.Ssiz = Ssiz;
    siz.XRsiz = XRsiz;
    siz.YRsiz = YRsiz;
    return siz;
  }
}
