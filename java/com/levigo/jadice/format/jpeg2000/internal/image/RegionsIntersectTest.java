package com.levigo.jadice.format.jpeg2000.internal.image;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class RegionsIntersectTest {

  private Region r0;
  private Region r1;
  private boolean expectedResult;

  @Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {new Region(0, 0, 128, 128), new Region(0, 0, 128, 128), true},
        {new Region(0, 0, 128, 128), new Region(60, 60, 60, 60), true},
        {new Region(0, 0, 10, 10), new Region(10, 10, 10, 10), false},
        {new Region(0, 0, 10, 10), new Region(9, 9, 1, 1), true},
        {new Region(0, 0, 10, 10), new Region(10, 0, 10, 10), false},
        {new Region(0, 0, 10, 10), new Region(0, 10, 10, 10), false},
        {new Region(10, 10, 10, 10), new Region(11, 11, 8, 8), true},
        {new Region(10, 10, 10, 10), new Region(9, 9, 1, 1), false},
        {new Region(50, 50, 50, 50), new Region(25, 25, 25, 25), false},
        {new Region(50, 50, 50, 50), new Region(25, 25, 26, 26), true},
        {new Region(25, 25, 26, 26), new Region(50, 50, 50, 50), true}
    });
  }

  public RegionsIntersectTest(Region r0, Region r1, boolean expectedResult) {
    this.r0 = r0;
    this.r1 = r1;
    this.expectedResult = expectedResult;
  }

  @Test
  public void runTest() {
    if (expectedResult) {
      Assert.assertTrue(r0 + " and " + r1 + " should intersect (r0 -> r1)", Regions.intersect(r0, r1));
      Assert.assertTrue(r1 + " and " + r0 + " should intersect (r1 -> r0)", Regions.intersect(r1, r0));
    } else {
      Assert.assertFalse(r0 + " and " + r1 + " should not intersect (r0 -> r1)", Regions.intersect(r0, r1));
      Assert.assertFalse(r1 + " and " + r0 + " should not intersect (r1 -> r0)", Regions.intersect(r1, r0));
    }
  }

}
