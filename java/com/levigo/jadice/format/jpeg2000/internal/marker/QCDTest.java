package com.levigo.jadice.format.jpeg2000.internal.marker;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;

@RunWith(Parameterized.class)
public class QCDTest extends MarkerTestBase<QCD> {

  @Parameter(1)
  public int Lqcd;

  @Parameter(2)
  public int Sqcd_guardbits;
  
  @Parameter(3)
  public int Sqcd_style;

  @Parameter(4)
  public int[] SPqcd_exp;
  
  @Parameter(5)
  public int[] SPqcd_man;
  
  @Parameter(6)
  public int[] SPqcd_dzone;
  
  @Parameters(name = "{0}")
  public static Object[][] data() {
    return new Object[][]{
        // @formatter:off
        {"qcd/qcd_p0_03", 5, 2, 1, new int[]{0}, new int[]{0}, null}
        // @formatter:on
    };
  }

  @Override
  protected Marker marker() {
    return Marker.QCD;
  }
  
  @Override
  protected void inspect(QCD underTest) {
    assertEquals("Lqcd", Lqcd, underTest.Lqcd);
    assertEquals("Sqcd_guardbits", Sqcd_guardbits, underTest.Sqxx_guardbits);
    assertEquals("Sqcd_style", Sqcd_style, underTest.Sqxx_style);
    assertArrayEquals("SPqcc_exp", SPqcd_exp, underTest.SPqxx_exp);
    assertArrayEquals("SPqcc_man", SPqcd_man, underTest.SPqxx_man);
    assertArrayEquals("SPqcc_dzone", SPqcd_dzone, underTest.SPqxx_dzone);
  }
  
  @Override
  protected QCD prepareMarker() {
    final QCD qcd = new QCD();
    qcd.Lqcd = Lqcd;
    qcd.Sqxx_guardbits = (byte) Sqcd_guardbits;
    qcd.Sqxx_style = (byte) Sqcd_style;
    qcd.SPqxx_exp = SPqcd_exp;
    qcd.SPqxx_man = SPqcd_man;
    qcd.SPqxx_dzone = SPqcd_dzone;
    return qcd;
  }
}
