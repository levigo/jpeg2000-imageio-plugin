package com.levigo.jadice.format.jpeg2000.internal.image;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CanvasTileComponentRegionTest {

  @Parameters
  public static Object[][] parameters() {
    return new Object[][]{
        {new Region(128, 128), new Pair(1, 1), new Region(128, 128)},
        {new Region(128, 128), new Pair(2, 2), new Region(64, 64)},
        {new Region(127, 126), new Pair(1, 1), new Region(127, 126)},
        {new Region(127, 126), new Pair(2, 1), new Region(64, 126)},
        {new Region(127, 126), new Pair(1, 2), new Region(127, 63)},
        {new Region(4, 4, 127, 126), new Pair(2, 1), new Region(2, 4, 64, 126)},
        {new Region(5, 4, 127, 126), new Pair(2, 1), new Region(3, 4, 63, 126)},
        {new Region(4, 4, 127, 126), new Pair(1, 2), new Region(4, 2, 127, 63)},
        {new Region(4, 5, 127, 126), new Pair(1, 2), new Region(4, 3, 127, 63)},
    };
  }

  @Parameter(0)
  public Region tileRegion;

  @Parameter(1)
  public Pair subsampling;

  @Parameter(2)
  public Region expected;

  @Test
  public void runTest() {
    assertEquals(expected, Canvas.tileComponentRegion(tileRegion, subsampling));
  }
}
