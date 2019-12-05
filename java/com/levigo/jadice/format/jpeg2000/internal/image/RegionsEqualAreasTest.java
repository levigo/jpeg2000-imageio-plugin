package com.levigo.jadice.format.jpeg2000.internal.image;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class RegionsEqualAreasTest {

  private final Region a;
  private final Region b;
  private final boolean expected;

  @Parameterized.Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {new Region(100, 100),       new Region(100, 100),           true},
        {new Region(10, 10),         new Region(0, 0, 10, 10),       true},
        {new Region(10, 10, 10, 10), new Region(10, 10, 10, 10),     true},
        {new Region(100, 100),       new Region(100, 100, 100, 100), false},
        {new Region(10, 10, 10, 10), new Region(10, 0, 10, 10),      false},
        {new Region(10, 10, 10, 10), new Region(0, 10, 10, 10),      false},
        {new Region(10, 10, 10, 10), new Region(0, 0, 100, 10),      false},
        {new Region(10, 10, 10, 10), new Region(0, 0, 10, 100),      false}
    });
  }

  public RegionsEqualAreasTest(Region a, Region b, boolean expected) {
    this.a = a;
    this.b = b;
    this.expected = expected;
  }

  @Test
  public void runTest() {
    if (expected) {
      assertTrue(Regions.equalAreas(a, b));
    } else {
      assertFalse(Regions.equalAreas(a, b));
    }
  }
}
