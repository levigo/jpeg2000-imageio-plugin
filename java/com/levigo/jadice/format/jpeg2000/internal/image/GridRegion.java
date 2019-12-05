package com.levigo.jadice.format.jpeg2000.internal.image;

public interface GridRegion {

  Region absolute();

  Region relativeTo(GridRegion base);
  
}
