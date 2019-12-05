package com.levigo.jadice.format.jpeg2000.internal.image;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CanvasTileRegionTest {

  @Parameters
  public static Object[][] parameters() {
    return new Object[][]{
        {new Region(128, 128), new Region(128, 128), 0, 0, new Region(128, 128)},
        {new Region(128, 128), new Region(64, 64), 0, 0, new Region(0, 0, 64, 64)},
        {new Region(128, 128), new Region(64, 64), 1, 0, new Region(64, 0, 64, 64)},
        {new Region(128, 128), new Region(64, 64), 0, 1, new Region(0, 64, 64, 64)},
        {new Region(128, 128), new Region(64, 64), 1, 1, new Region(64, 64, 64, 64)},
        {new Region(127, 126), new Region(64, 64), 0, 0, new Region(0, 0, 64, 64)},
        {new Region(127, 126), new Region(64, 64), 1, 0, new Region(64, 0, 63, 64)},
        {new Region(127, 126), new Region(64, 64), 0, 1, new Region(0, 64, 64, 62)},
        {new Region(127, 126), new Region(64, 64), 1, 1, new Region(64, 64, 63, 62)},
    };
  }

  @Parameter(0)
  public Region canvas;

  @Parameter(1)
  public Region tilePartition;

  @Parameter(2)
  public int p;

  @Parameter(3)
  public int q;

  @Parameter(4)
  public Region expected;

  @Test
  public void runTest() {
    assertEquals(expected, Canvas.tileRegion(canvas, tilePartition, p, q));
  }
}
