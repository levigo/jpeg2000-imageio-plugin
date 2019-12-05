package com.levigo.jadice.format.jpeg2000.internal;

import com.levigo.jadice.format.jpeg2000.internal.marker.COx;

public class Constants {

  /** Default stripe height, as defined in <i>ITU-T.800, D.1</i>. */
  public static final byte STRIPE_HEIGHT = 4;

  /** Default precinct size as described by <i>ITU-T.800, Table A.13</i>. */
  public static final int DEFAULT_PRECINCT_SIZE = 1 << COx.PP_DEFAULT;

  /** Protects against an amount of precincts which blows memory capacity. */
  public static final long NUM_PRECINCTS_THRESHOLD = 1 << 30;

  /** Default starting bitplane based on 32-bit integer. The most significant bit is reserved for the sign. */
  public static final byte INTEGER_COEFFICIENT_START = 30;

}
