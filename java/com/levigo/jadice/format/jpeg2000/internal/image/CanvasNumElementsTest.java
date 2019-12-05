package com.levigo.jadice.format.jpeg2000.internal.image;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CanvasNumElementsTest {

  private final Region region;
  private final Region partition;
  private final Pair expected;

  @Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {new Region(0, 0, 8, 8),   new Region(0, 0, 8, 8),   new Pair(1, 1)},
        {new Region(0, 0, 16, 16), new Region(0, 0, 8, 8),   new Pair(2, 2)},
        {new Region(8, 8, 16, 16), new Region(0, 0, 8, 8),   new Pair(2, 2)},
        {new Region(8, 8, 16, 16), new Region(0, 0, 32, 32), new Pair(1, 1)},
        {new Region(2, 2, 6, 6),   new Region(1, 1, 8, 8),   new Pair(1, 1)},
        {new Region(16, 16, 0, 0), new Region(1, 1, 8, 8),   new Pair(0, 0)},
    });
  }

  public CanvasNumElementsTest(Region region, Region partition, Pair expected) {
    this.region = region;
    this.partition = partition;
    this.expected = expected;
  }

  @Test
  public void runTest() {
    final String message = region + " partitioned in " + partition + ": " + expected;
    Assert.assertEquals(message, expected, Canvas.numElements(region, partition));
  }
}