package com.levigo.jadice.format.jpeg2000.internal.image;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CanvasSubbandPartitionTest {

  @Parameters(name= "{index}: resolution[{1}]={0}")
  public static Object[][] data() {
    return new Object[][]{
        {new Region(16, 16), 0, new Region(16, 16)},
        {new Region(32, 32), 1, new Region(16, 16)},
        {new Region(32, 32, 32, 32), 0, new Region(32, 32, 32, 32)},
        {new Region(32, 32, 32, 32), 1, new Region(32, 32, 16, 16)},
        {new Region(32, 32, 33, 32), 1, new Region(32, 32, 17, 16)}
    };
  }
  
  @Parameter(0)
  public Region resRegion;

  @Parameter(1)
  public int resLevel;

  @Parameter(2)
  public Region expected;
  
  @Test
  public void test() {
    assertEquals(expected, Canvas.subbandPartition(resRegion, resLevel));
  }
}
