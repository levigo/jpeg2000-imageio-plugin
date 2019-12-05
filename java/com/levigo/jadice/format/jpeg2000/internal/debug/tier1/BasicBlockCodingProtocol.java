package com.levigo.jadice.format.jpeg2000.internal.debug.tier1;

import static com.levigo.jadice.format.jpeg2000.internal.debug.BitsParameter.bits;
import static com.levigo.jadice.format.jpeg2000.internal.debug.BooleanParameter.bool;
import static com.levigo.jadice.format.jpeg2000.internal.debug.IntegerParameter.integer;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier1.BlockCodingToken.ContextWord;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier1.BlockCodingToken.Finish;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier1.BlockCodingToken.Refinement;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier1.BlockCodingToken.RunLength;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier1.BlockCodingToken.RunMode;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier1.BlockCodingToken.Sample;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier1.BlockCodingToken.SegmentationMark;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier1.BlockCodingToken.Sign;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier1.BlockCodingToken.Significance;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tier1.BlockCodingToken.values;

import java.util.ArrayList;
import java.util.Collection;

import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolBase;
import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolToken;

public class BasicBlockCodingProtocol extends ProtocolBase implements BlockCodingProtocol {

  private static final Collection<ProtocolToken> BLOCK_CODING_TOKENS;

  static {
    BLOCK_CODING_TOKENS = new ArrayList<ProtocolToken>();
    for (BlockCodingToken blockCodingToken : values()) {
      BLOCK_CODING_TOKENS.add(blockCodingToken);
    }
  }

  public BasicBlockCodingProtocol() {
    super(BLOCK_CODING_TOKENS);
  }

  @Override
  public void finish() {
    createAndNotify(Finish, integer(getNumElements()));
  }

  @Override
  public void run(int symbol) {
    createAndNotify(RunMode, bool(symbol == 0));
  }

  @Override
  public void runlength(int symbol) {
    createAndNotify(RunLength, bits(symbol, 2));
  }

  @Override
  public void contextWord(int ctxWord) {
    createAndNotify(ContextWord, integer(ctxWord));
  }

  @Override
  public void significance(boolean symbol) {
    createAndNotify(Significance, bool(symbol));
  }

  @Override
  public void sign(int symbol) {
    createAndNotify(Sign, bits(symbol, 1));
  }

  @Override
  public void sample(int sample) {
    createAndNotify(Sample, integer(sample));
  }

  @Override
  public void segmentationMark(int sym) {
    createAndNotify(SegmentationMark, bits(sym, 4));
  }

  @Override
  public void refinement(int symbol) {
    createAndNotify(Refinement, bool(symbol == 1));
  }
}
