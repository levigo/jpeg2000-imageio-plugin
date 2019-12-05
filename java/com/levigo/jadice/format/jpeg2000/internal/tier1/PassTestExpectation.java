package com.levigo.jadice.format.jpeg2000.internal.tier1;

import com.levigo.jadice.format.jpeg2000.internal.image.Pair;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.SubbandType;

public class PassTestExpectation {
  
  public TraceTile[] tiles;

  public static class TraceTile {
    public int id;
    public TraceComp[] comps;
  }

  public static class TraceComp {
    public int id;
    public TraceResolution[] resolutions;
  }

  public static class TraceResolution {
    public int id;
    public TraceSubband[] subbands;
  }

  public static class TraceSubband {
    public SubbandType type;
    public TracePrecinct[] precincts;
  }

  public static class TracePrecinct {
    public int id;
    public TraceBlock[] blocks;
  }

  public static class TraceBlock {
    public Pair indices;
    public Region region;
    public int[] stripes;
    public int bitplane;
    public int ctxRowGap;
    public int[] ctxBuffer;
    public Pass[] passes;
    public int[][] decisionTrace;
    public int[][] sampleTrace;
    public int[][] ctxTrace;
    public CtxExpectation[][] stateTrace;
    public boolean segmentationSymbols;
    public boolean causalCtx;
  }
  
}
