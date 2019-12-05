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
package org.jadice.jpeg2000.internal.image;

import static org.jadice.jpeg2000.internal.image.Regions.copyOf;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.marker.Qxx;

public class Subband implements HasGridRegion {

  public int idx;
  public SubbandType type;
  public Resolution resolution;
  protected GridRegion gridRegion;  

  public Region blockPartition;
  public Region precinctPartition;
  public int exponent;

  public int mantissa;
  public float delta;
  public byte guardBits;
  public int Kmax;
  
  private BandPrecinct[] bandPrecincts;
  
  protected Subband() {
    
  }

  public Subband(int idx, SubbandType type, Resolution resolution) throws JPEG2000Exception {
    this.idx = idx;
    this.type = type;
    this.resolution = resolution;

    gridRegion = createGridRegion();

    precinctPartition = createPrecinctPartition();
    blockPartition = createBlockPartition();

    final TileComponent tileComp = resolution.tileComp;
    final Codestream codestream = tileComp.codestream;
    final Tile tile = tileComp.tile;
    final Component comp = tileComp.comp;

    final Qxx qxx = Qxx.accessQCx(codestream, tile, comp.idx);

    if (tileComp.reversible) {
      exponent = qxx.SPqxx_exp[idx];
      delta = 1.0F / (1 << tileComp.comp.precision);
    } else if (qxx.Sqxx_style == Qxx.VALUE_SCALAR_DERIVED) {
      if (resolution.resLevel > 0) {
        final Resolution res0 = tileComp.accessResolution(0);
        final Subband ll = res0.accessSubband(SubbandType.LL);
        exponent = ll.exponent - ll.resolution.dwtLevel + resolution.dwtLevel;
        mantissa = ll.mantissa;
      } else {
        exponent = qxx.SPqxx_exp[0];
        mantissa = qxx.SPqxx_man[0];
      }
    } else {
      // Scalar expounded -> one exponent/mantissa element per subband
      exponent = qxx.SPqxx_exp[idx];
      mantissa = qxx.SPqxx_man[idx];
    }

    guardBits = qxx.Sqxx_guardbits;
    Kmax = guardBits + exponent - 1;
  }

  private GridRegion createGridRegion() {
    final Region subbandPartition = resolution.subbandPartition;
    final Region resolutionRegion = resolution.region().absolute();
    final Region subbandRegion = Regions.replicate(subbandPartition, type.branchX, type.branchY, resolutionRegion);
    return new DefaultGridRegion(subbandRegion);
  }

  private Region createPrecinctPartition() throws JPEG2000Exception {
    if (type == SubbandType.LL) {
      // Precinct partition of the LL subband of resolution level 0 is equal to the one computed for the resolution.
      // As we usually don't create LL subbands for resolution level > 0 the LL type indicates the lowest resolution
      // level.
      return copyOf(resolution.precinctPartition);
    } else {
      return Regions.createHalfSized(resolution.precinctPartition);
    }
  }

  private Region createBlockPartition() {
    final Region blockPartition = new Region();
    blockPartition.pos = precinctPartition.pos;
    blockPartition.size = resolution.tileComp.blockSize.clone();
    Regions.clamp(precinctPartition, blockPartition);
    return blockPartition;
  }

  public BandPrecinct accessBandPrecinct(int p) throws JPEG2000Exception {
    if (bandPrecincts == null) {
      bandPrecincts = new BandPrecinct[resolution.numPrecinctsTotal];
    }

    BandPrecinct bandPrecinct = bandPrecincts[p];
    if (bandPrecinct == null) {
      final Precinct precinct = resolution.accessPrecinct(p);
      bandPrecinct = new BandPrecinct(precinct, this);

      bandPrecincts[p] = bandPrecinct;
    }

    return bandPrecinct;
  }

  @Override
  public GridRegion region() {
    return gridRegion;
  }

  //  private void initBandPrecincts() throws JPEG2000Exception {
//    final Precinct[] precincts = resolution.precincts;
//    bandPrecincts = new BandPrecinct[precincts.length];
//    if (type == SubbandType.LL) {
//      // Precinct partition of the LL subband of resolution level 0 is equal to the one computed for the resolution.
//      // As we usually don't create LL subbands for resolution level > 0 the LL type indicates the lowest resolution
//      // level.
//      precinctPartition = Regions.createCopy(resolution.precinctPartition);
//      for (final Precinct precinct : precincts) {
//        bandPrecincts[precinct.idx] = new BandPrecinct(precinct, this, precinct.region);
//      }
//    } else {
//      precinctPartition = Regions.createHalfSized(resolution.precinctPartition);
//      for (final Precinct precinct : precincts) {
//        final Region subsampledRegion = Regions.createHalfSized(precinct.region);
//        bandPrecincts[precinct.idx] = new BandPrecinct(precinct, this, subsampledRegion);
//      }
//    }
//  }
}
