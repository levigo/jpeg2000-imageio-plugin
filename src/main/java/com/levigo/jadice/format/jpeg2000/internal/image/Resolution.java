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
package com.levigo.jadice.format.jpeg2000.internal.image;

import static com.levigo.jadice.format.jpeg2000.internal.image.SubbandType.HH;
import static com.levigo.jadice.format.jpeg2000.internal.image.SubbandType.HL;
import static com.levigo.jadice.format.jpeg2000.internal.image.SubbandType.LH;
import static com.levigo.jadice.format.jpeg2000.internal.image.SubbandType.LL;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Constants;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;
import com.levigo.jadice.format.jpeg2000.internal.marker.COx;
import com.levigo.jadice.format.jpeg2000.internal.param.Parameters;
import com.levigo.jadice.format.jpeg2000.msg.ValidationMessages;

/**
 * Each tile-component is wavelet transformed with <code>N<sub>L</sub></code> decomposition levels as explained in
 * <i>ITU-T.800, Annex F</i>. Thus, there are <code>N<sub>L</sub> + 1</code> distinct resolution levels, denoted
 * <code>r = 0,1,...,N<sub>L</sub></code>. The lowest resolution level, <code>r = 0</code>, is represented by the
 * <code>N<sub>L</sub>LL</code> band. In general, a reduced resolution version of a tile-component with resolution
 * level, <code>r</code>, is the sub-band <code>nLL</code>, where <code>n = N<sub>L</sub> – r</code>.
 * 
 * @see Canvas#resolutionCoordinate(int, int, int) 
 */
public class Resolution implements Pushable, HasGridRegion {

  public Codestream codestream;
  public TileComponent tileComp;

  public int dwtLevel;
  public int resLevel;
  
  protected GridRegion gridRegion;

  public Region precinctPartition;
  public int numPrecinctsX;

  public int numPrecinctsY;
  public int numPrecinctsTotal;

  private Precinct[] precincts;

  public int numBlocks;

  public Region subbandPartition;
  
  private Subband[] subbands;
  
  public ResolutionState state;
  
  protected Resolution() {
    
  }

  public Resolution(Codestream codestream, TileComponent tileComp, int dwtLevel, int resLevel)
      throws JPEG2000Exception {
    
    this.codestream = codestream;
    this.tileComp = tileComp;
    this.dwtLevel = dwtLevel;
    this.resLevel = resLevel;
    
    gridRegion = createGridRegion();
    
    final Region region = gridRegion.absolute();
    
    final Pair pp = getPP();
    precinctPartition = new Region();
    precinctPartition.pos = tileComp.tile.codeBlockAnchorPoint.clone();
    precinctPartition.size = new Pair(1 << pp.x, 1 << pp.y);

    numPrecinctsX = Canvas.numPrecinctsX(region.x0(), region.x1(), precinctPartition.x0(), pp.x);
    numPrecinctsY = Canvas.numPrecinctsY(region.y0(), region.y1(), precinctPartition.y0(), pp.y);
    numPrecinctsTotal = numPrecinctsX * numPrecinctsY;

    if (numPrecinctsTotal > Constants.NUM_PRECINCTS_THRESHOLD) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_NUMBER_OF_PRECINCT);
    }

    final Pair first = Regions.getFirstChildIdx(region, precinctPartition);
    final Pair last = Regions.getLastChildIdx(region, precinctPartition);

    if (last.x - first.x + 1 != numPrecinctsX || last.y - first.y + 1 != numPrecinctsY) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_NUMBER_OF_PRECINCT);
    }

    subbandPartition = Canvas.subbandPartition(region, resLevel);
  }

  private GridRegion createGridRegion() {
    final Region tileCompRegion = tileComp.region().absolute();
    final Region resolutionRegion = Canvas.resolutionRegion(tileCompRegion, tileComp.dwtLevels, resLevel);
    return new DefaultGridRegion(resolutionRegion);
  }

  private Pair getPP() {
    final int[] userPPs = tileComp.userPPs;
    if (userPPs != null) {
      final int idx = tileComp.dwtLevels - resLevel;
      final int PP = userPPs[idx];
      return new Pair(Parameters.extract(PP, COx.MASK_PPX), Parameters.extract(PP, COx.MASK_PPY, COx.SHIFT_PPY));
    }

    return new Pair(COx.PP_DEFAULT, COx.PP_DEFAULT);
  }
  
  public Precinct accessPrecinct(int p) {
    return accessPrecinct(p, p % numPrecinctsX, p / numPrecinctsX);
  }
  
  public Precinct accessPrecinct(int px, int py) {
    return accessPrecinct(py * numPrecinctsX + px, px, py);
  }
  
  private Precinct accessPrecinct(int p, int px, int py) {
    if (precincts == null) {
      precincts = new Precinct[numPrecinctsTotal];
    }

    Precinct precinct = precincts[p];
    if (precinct == null) {
      precinct = new Precinct(p, px, py, this);
      precincts[p] = precinct;
    }

    return precinct;
  }
  
  public Subband accessSubband(SubbandType subbandType) throws JPEG2000Exception {
    if (subbands == null) {
      subbands = new Subband[numSubbands(resLevel)];
    }

    final int subbandIndex = subbandArrayIndex(subbandType);
    Subband subband = subbands[subbandIndex];
    if(subband == null) {
      final int index = Math.max(resLevel - 1, 0) * 3 + subbandType.id;
      subband = new Subband(index, subbandType, this);

      subbands[subbandIndex] = subband;
    }
    
    return subband;
  }

  public SubbandType[] subbandTypes() {
    return resLevel == 0 ? new SubbandType[]{LL} : new SubbandType[]{HL, LH, HH};
  }
  
  public static int numSubbands(int r) {
    return r == 0 ? 1 : 3;
  }

  public static int subbandArrayIndex(SubbandType subbandType) {
    switch (subbandType){
      case LL:
      case HL:
        return 0;
      case LH:
        return 1;
      case HH:
        return 2;
    }

    throw new IllegalStateException("illegal subband type");
  }

  @Override
  public void start(DecoderParameters parameters) {
    state = new ResolutionState();
    state.blocksLeft = new AtomicInteger(numBlocks);
  }

  @Override
  public void free() throws JPEG2000Exception {
    state = null;
    Arrays.fill(subbands, null);
  }

  @Override
  public String toString() {
    return "Resolution [region=" + gridRegion + ", tileComp=" + tileComp + ", dwtLevel=" + dwtLevel
        + ", resLevel=" + resLevel + "]";
  }

  @Override
  public GridRegion region() {
    return gridRegion;
  }
}
