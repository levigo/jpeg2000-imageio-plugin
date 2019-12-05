package com.levigo.jadice.format.jpeg2000.internal;

public class Functions {

  public static boolean isPower2(int val) {
    while (val > 1) {
      if ((val & 1) != 0) {
        return false;
      }
      val >>= 1;
    }
    return (val == 1);
  }
}
