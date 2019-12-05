package com.levigo.jadice.format.jpeg2000.internal.tier1;

import java.util.Arrays;

/**
 * This class manages the contexts used for arithmetic decoding and arithmetic integer decoding. It selects the
 * probability estimate and statistics used during decoding procedure.
 */
public final class ContextContainer {

  /** Initial states for all contexts. Defined in <i>ITU-T.800, Table D.7</i>. */
  private static final byte[] INITIAL_STATES;

  static {
    INITIAL_STATES = new byte[19];
    INITIAL_STATES[States.KAPPA_SIG_BASE] = 4;
    INITIAL_STATES[States.KAPPA_RUN] = 3;
    INITIAL_STATES[States.KAPPA_UNI] = 46;
  }

  int index;

  private byte cx[];

  private final byte mps[];

  public ContextContainer() {
    this(Arrays.copyOf(INITIAL_STATES, INITIAL_STATES.length), States.KAPPA_RUN);
  }

  /**
   * @param size  the amount of context values, all initialized with 0.
   * @param index the start index.
   */
  public ContextContainer(int size, int index) {
    this.index = index;
    this.cx = new byte[size];
    this.mps = new byte[size];
  }

  /**
   * @param initial the array with initial context values
   * @param index   the start index.
   */
  public ContextContainer(byte[] initial, int index) {
    this.index = index;
    this.cx = initial;
    this.mps = new byte[initial.length];
  }

  protected int cx() {
    return cx[index] & 0x7f;
  }

  protected void setCx(int value) {
    cx[index] = (byte) (value & 0x7f);
  }

  /**
   * @return The decision. Possible values are {@code 0} or {@code 1}.
   */
  protected byte mps() {
    return mps[index];
  }

  /** Flips the bit in actual "more predictable symbol" array element. */
  protected void toggleMps() {
    mps[index] ^= 1;
  }

  public void setIndex(int index) {
    this.index = index;
  }
}