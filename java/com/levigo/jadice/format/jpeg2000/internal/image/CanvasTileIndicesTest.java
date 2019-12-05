package com.levigo.jadice.format.jpeg2000.internal.image;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CanvasTileIndicesTest {
  
  @Parameters
  public static Object[][] parameters() {
    return new Object[][]{
        {0, 2, new Pair(0, 0)},
        {2, 2, new Pair(0, 1)}
    };
  }

  @Parameter(0)
  public int t;

  @Parameter(1)
  public int numTilesHorizontal;
  
  @Parameter(2)
  public Pair expected;
  
  @Test
  public void tileIndicesTest() {
    assertEquals(expected, Canvas.tileIndices(t, numTilesHorizontal));
  }
  
}
