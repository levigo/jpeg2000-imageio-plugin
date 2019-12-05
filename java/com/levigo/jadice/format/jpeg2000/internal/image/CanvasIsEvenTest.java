package com.levigo.jadice.format.jpeg2000.internal.image;

import static org.hamcrest.CoreMatchers.is;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

public class CanvasIsEvenTest {

  @Test
  public void testEvens() {
    testRange(0, Short.MAX_VALUE, true);
  }

  @Test
  public void testOdds() {
    testRange(1, Short.MAX_VALUE, false);
  }

  private void testRange(int start, int end, boolean expected) {
    for (int lengthUnderTest = start; lengthUnderTest <= end; lengthUnderTest += 2) {
      MatcherAssert.assertThat(Canvas.isEven(lengthUnderTest), is(expected));
    }
  }

}
