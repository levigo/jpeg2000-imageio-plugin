package com.levigo.jadice.format.jpeg2000.internal.tier1;

public class PassState {
  public int symbol;
  public int ctxWord;
  public int cp;
  public int sp;
  public int value;

  @Override
  public String toString() {
    return "cp=" + cp + "; sp=" + sp + "; sym=" + symbol + "; ctxWord=" + ctxWord + "; val=" + value;
  }
}
