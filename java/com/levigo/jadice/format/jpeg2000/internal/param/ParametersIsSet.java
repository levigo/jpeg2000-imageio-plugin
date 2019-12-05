package com.levigo.jadice.format.jpeg2000.internal.param;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ParametersIsSet {

  private final int parameter;
  private final int mask;
  private final int value;
  private final boolean expectedIndirect;
  private final boolean expectedDirect;

  @Parameterized.Parameters(name = "{index}: Parameters.isSet({0}, {1}, {2}) => {3}; Parameters.isSet({0}, {1}) => {4}")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {0x1, 0x1, 0x1, true, true}, //
        {0x1, 0x1, 0x0, false, true}, //
        {0x0, 0x1, 0x1, false, false}, //
        {0x88, 0x80, 0x80, true, true}, //
        {0xFFFFFFFF, 0x70, 0x70, true, true}, //
        {0xFFFFFFFF, 0x80000000, 0x80000000, true, true}, //
        {0xFFFFFFFF, 0x70, 0x0, false, true}, //
        {0xFFFFFFFF, 0x80000000, 0x0, false, true}, //
    });
  }

  public ParametersIsSet(int parameter, int mask, int value, boolean expectedIndirect, boolean expectedDirect) {
    this.parameter = parameter;
    this.mask = mask;
    this.value = value;
    this.expectedIndirect = expectedIndirect;
    this.expectedDirect = expectedDirect;
  }

  @Test
  public void runTest() {
    assertEquals("isSet with mask and value", expectedIndirect, Parameters.isValue(parameter, mask, value));
    assertEquals("isSet with mask", expectedDirect, Parameters.isSet(parameter, mask));
  }
}
