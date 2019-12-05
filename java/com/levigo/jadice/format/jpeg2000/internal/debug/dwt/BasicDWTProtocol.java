package com.levigo.jadice.format.jpeg2000.internal.debug.dwt;

import static com.levigo.jadice.format.jpeg2000.internal.debug.IntegerParameter.integer;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tcq.QuantizationToken.Finish;

import java.util.ArrayList;
import java.util.Collection;

import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolBase;
import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolToken;

public class BasicDWTProtocol extends ProtocolBase implements DWTProtocol {

  private static final Collection<ProtocolToken> DWT_TOKENS;

  static {
    DWT_TOKENS = new ArrayList<ProtocolToken>();
    for (DWTToken dwtToken : DWTToken.values()) {
      DWT_TOKENS.add(dwtToken);
    }
  }

  public BasicDWTProtocol() {
    super(DWT_TOKENS);
  }

  @Override
  public void finish() {
    createAndNotify(Finish, integer(getNumElements()));
  }

}
