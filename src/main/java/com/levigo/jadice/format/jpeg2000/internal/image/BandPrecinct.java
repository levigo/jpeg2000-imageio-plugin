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

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.tier2.TagTreeDecoder;

public class BandPrecinct implements HasGridRegion {

  public Precinct precinct;
  public Subband subband;

  public Pair numBlocks;
  protected GridRegion gridRegion;

  private CodeBlock[] blocks;

  public TagTreeDecoder inclusionDecoder;
  public TagTreeDecoder bitDepthDecoder;

  protected BandPrecinct() {
    
  }
  
  public BandPrecinct(Precinct precinct, Subband subband) throws JPEG2000Exception {
    this.precinct = precinct;
    this.subband = subband;

    gridRegion = createGridRegion();

    numBlocks = createNumBlocks(subband);
    
    final Resolution resolution = precinct.resolution;
    resolution.numBlocks += (numBlocks.x * numBlocks.y);

    inclusionDecoder = new TagTreeDecoder(numBlocks.x, numBlocks.y);
    bitDepthDecoder = new TagTreeDecoder(numBlocks.x, numBlocks.y);
  }

  private GridRegion createGridRegion() throws JPEG2000Exception {
    final Resolution resolution = subband.resolution;
    final Region precinctRegion = precinct.region().absolute();

    if (resolution.resLevel == 0) {
      return new DefaultGridRegion(Regions.copyOf(precinctRegion));
    } else {
      final GridRegion subbandRegion = subband.region();
      final Region relativePrecinctRegion = precinct.region().relativeTo(resolution.region());
      final Region bandPrecinctRegion = Regions.createHalfSized(relativePrecinctRegion);
      bandPrecinctRegion.displaceBy(subbandRegion.absolute().pos);
      bandPrecinctRegion.clampTo(subbandRegion.absolute());
      return new DefaultGridRegion(bandPrecinctRegion);
    }
  }

  private Pair createNumBlocks(Subband subband) {
    final Region blockPartition = Regions.copyOf(subband.blockPartition);
    blockPartition.displaceBy(gridRegion.absolute().pos);
    return Canvas.numElements(gridRegion.relativeTo(subband.resolution.region()), blockPartition);
  }

  @Override
  public GridRegion region() {
    return gridRegion;
  }

  public CodeBlock accessCodeBlock(Pair indices) {
    if (blocks == null) {
      blocks = new CodeBlock[numBlocks.x * numBlocks.y];
    }

    final int codeBlockIndex = indices.y * numBlocks.x + indices.x;
    CodeBlock codeBlock = blocks[codeBlockIndex];
    if (codeBlock == null) {
      codeBlock = new CodeBlock(indices, this);
      blocks[codeBlockIndex] = codeBlock;
    }

    return codeBlock;
  }
  
}
