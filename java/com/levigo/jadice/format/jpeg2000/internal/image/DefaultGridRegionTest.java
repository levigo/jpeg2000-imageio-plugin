package com.levigo.jadice.format.jpeg2000.internal.image;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

public class DefaultGridRegionTest {

  @Test
  public void testDefaultCreation() {
    final Region region = new Region(0, 0, 128, 128);
    final GridRegion gridElement = new DefaultGridRegion(region);
    MatcherAssert.assertThat(gridElement.absolute(), is(equalTo(region)));
  }
  
  @Test
  public void testRelativeTo() {
    final Region absoluteRegionParent = new Region(32, 32, 128, 128);
    final DefaultGridRegion parent = new DefaultGridRegion(absoluteRegionParent);
    
    final Region absoluteRegion = new Region(64, 64, 96, 96);
    final DefaultGridRegion underTest = new DefaultGridRegion(absoluteRegion);

    final Region expectedAbsolute = Regions.copyOf(absoluteRegion);
    final Region expectedRelative = new Region(32, 32, 96, 96);

    MatcherAssert.assertThat(underTest.absolute(), is(equalTo(expectedAbsolute)));
    MatcherAssert.assertThat(underTest.relativeTo(parent), is(equalTo(expectedRelative)));
  }
  
}
