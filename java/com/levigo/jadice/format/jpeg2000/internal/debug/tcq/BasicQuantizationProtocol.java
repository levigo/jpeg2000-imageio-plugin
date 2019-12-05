package com.levigo.jadice.format.jpeg2000.internal.debug.tcq;

import static com.levigo.jadice.format.jpeg2000.internal.debug.IntegerParameter.integer;
import static com.levigo.jadice.format.jpeg2000.internal.debug.NullParameter.noParam;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tcq.QuantizationToken.Downshift;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tcq.QuantizationToken.FillWithZeros;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tcq.QuantizationToken.Finish;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tcq.QuantizationToken.ValueAfter;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tcq.QuantizationToken.ValueBefore;

import java.util.ArrayList;
import java.util.Collection;

import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolBase;
import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolToken;

public class BasicQuantizationProtocol extends ProtocolBase implements QuantizationProtocol {

  private static final Collection<ProtocolToken> QUANTIZATION_TOKENS;

  static {
    QUANTIZATION_TOKENS = new ArrayList<ProtocolToken>();
    for (QuantizationToken quantizationToken : QuantizationToken.values()) {
      QUANTIZATION_TOKENS.add(quantizationToken);
    }
  }

  public BasicQuantizationProtocol() {
    super(QUANTIZATION_TOKENS);
  }

  @Override
  public void downshift(int downshift) {
    createAndNotify(Downshift, integer(downshift));
  }

  @Override
  public void valueBefore(int value) {
    createAndNotify(ValueBefore, integer(value));
  }

  @Override
  public void valueAfter(int value) {
    createAndNotify(ValueAfter, integer(value));
  }

  @Override
  public void fillWithZeros() {
    createAndNotify(FillWithZeros, noParam());
  }

  @Override
  public void finish() {
    createAndNotify(Finish, integer(getNumElements()));
  }
}
