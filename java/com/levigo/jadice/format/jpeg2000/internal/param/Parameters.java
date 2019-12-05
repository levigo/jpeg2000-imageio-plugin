package com.levigo.jadice.format.jpeg2000.internal.param;

public class Parameters {

  public static boolean isSet(int parameterValue, int mask) {
    return isValue(parameterValue, mask, mask);
  }

  public static boolean isValue(int parameterValue, int mask, int expectedValue) {
    return (parameterValue & mask) == expectedValue;
  }

  public static int extract(int parameterValue, int mask) {
    return parameterValue & mask;
  }

  public static int extract(int parameterValue, int mask, int shift) {
    return (parameterValue & mask) >> shift;
  }

}
