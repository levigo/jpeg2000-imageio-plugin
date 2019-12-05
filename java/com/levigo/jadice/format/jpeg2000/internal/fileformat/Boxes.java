package com.levigo.jadice.format.jpeg2000.internal.fileformat;

import java.util.Collection;

public class Boxes {
  public static Box findBox(BoxType boxType, Collection<Box> boxes) {
    for (Box box : boxes) {
      if(box.TBox == boxType.type) {
        return box;
      }
    }

    return null;
  }
}
