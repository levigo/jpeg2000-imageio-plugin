package com.levigo.jadice.format.jpeg2000.internal.kernel;

// part of an experiment. Currently not in use.
public class KernelDescription {
  public boolean symmetric;
  public boolean symmetricExtension;
  public boolean reversible;
  public int numSteps;
  public KernelStepInfo[] stepInfo;
  public float[] coefficients;
}
