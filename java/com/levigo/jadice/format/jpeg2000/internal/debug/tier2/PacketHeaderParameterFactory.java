package com.levigo.jadice.format.jpeg2000.internal.debug.tier2;

import static com.levigo.jadice.format.jpeg2000.internal.debug.CompositeParameter.composite;
import static com.levigo.jadice.format.jpeg2000.internal.debug.IntegerParameter.integer;

import com.levigo.jadice.format.jpeg2000.internal.debug.CompositeParameter;
import com.levigo.jadice.format.jpeg2000.internal.debug.Parameter;
import com.levigo.jadice.format.jpeg2000.internal.debug.ParameterFactory;

class PacketHeaderParameterFactory implements ParameterFactory {

  private static final String formattedString = "c=%d p=%d r=%d l=%d";

  public static CompositeParameter packetHeader(int comp, int prec, int res, int lay) {
    return composite(formattedString, integer(comp), integer(prec), integer(res), integer(lay));
  }

  @Override
  public Parameter create() {
    return composite(formattedString, integer(), integer(), integer(), integer());
  }
}
