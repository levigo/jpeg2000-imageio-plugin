/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
