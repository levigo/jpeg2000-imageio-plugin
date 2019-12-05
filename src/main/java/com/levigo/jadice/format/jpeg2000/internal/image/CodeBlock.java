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

import static com.levigo.jadice.format.jpeg2000.internal.image.Regions.copyOf;
import static com.levigo.jadice.format.jpeg2000.internal.image.Regions.replicate;
import static com.levigo.jadice.format.jpeg2000.internal.tier1.States.OOB_MARKER;

import java.util.Arrays;

import com.levigo.jadice.format.jpeg2000.internal.Constants;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codeword;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;
import com.levigo.jadice.format.jpeg2000.internal.marker.COx;
import com.levigo.jadice.format.jpeg2000.internal.param.Parameters;
import com.levigo.jadice.format.jpeg2000.internal.tier1.ContextContainer;
import com.levigo.jadice.format.jpeg2000.internal.tier1.States;

public class CodeBlock implements Pushable, HasGridRegion {

  /** The vertical and horizontal index of this code-block in the precinct-domain. */
  public Pair indices;

  /** Designates the area on the reference grid which is covered by this code-block. */
  protected GridRegion gridRegion;

  /** The logical parent to be able to navigate in the element hierarchy. */
  public BandPrecinct bandPrecinct;

  /** This field indicates and preserves two kind of states used in packet header decoding. */
  public volatile byte beta;

  /** Total amount of passes recorded for this code-block. */
  public volatile int numPassesTotal;

  /**
   * The height, {<i>h</i> | 1 ≤ <i>h</i> ≤ {@value Constants#STRIPE_HEIGHT} , of each stripe,
   * indexed from 0 to <i>{@linkplain #numStripes}</i>-1, used in <i>Tier 1</i> decoding. Typically
   * all columns should have the default amount of {@value Constants#STRIPE_HEIGHT} coefficients per
   * column per stripe, except of the last stripe that might be potentially smaller than
   * {@value Constants#STRIPE_HEIGHT}.
   */
  public int[] stripeHeights;
  public int numStripes;

  public ContextContainer mqCtx;

  public int bitplaneMin;
  public int bitplaneMax;

  /**
   * An identifier set by the multi-threading system. The assignment should be done immediately
   * after the certain thread which is responsible for the decoding is known. At this point we have
   * to avoid synchronization as it would be expensive because of two major facts. First, each layer
   * is able to contribute data to a code-block. Each contribution has to be scheduled in the
   * defined order. Second and in combination with the first fact, external managing of the
   * potentially huge amount of code-blocks might be very expensive.
   */
  public volatile byte threadId;

  /**
   * Whether selective arithmetic coding bypass is used (<code>true</code>) or not (
   * <code>false</code>).
   */
  public boolean bypass;

  /**
   * Whether context probabilities are reset on coding pass boundaries (<code>true</code>) or not (
   * <code>false</code>).
   */
  public boolean resetCtx;

  /** Whether each coding pass is terminated (<code>true</code>) or not (<code>false</code>). */
  public boolean restart;

  /** Whether vertically causal context is used (<code>true</code>) or not (<code>false</code>). */
  public boolean causalCtx;

  /** Whether predictable termination is used (<code>true</code>) or not (<code>false</code>). */
  public boolean errorCheck;

  /** Whether segmentation symbols are used (<code>true</code>) or not (<code>false</code>). */
  public boolean segmentationMarks;

  /** Currently experimental. */
  public int missingMSBs;

  public CodeBlockState state;

  public Codeword codeword;

  protected CodeBlock() {

  }

  public CodeBlock(Pair indices, BandPrecinct bandPrecinct) {
    this.indices = indices;
    this.bandPrecinct = bandPrecinct;

    gridRegion = createGridElement();

    final Region region = gridRegion.absolute();
    numStripes = region.size.y + 3 >> 2;

    final int modes = getModes();
    bypass = Parameters.isSet(modes, COx.MASK_MODE_ARITHMETIC_BYPASS);
    resetCtx = Parameters.isSet(modes, COx.MASK_MODE_RESET_CONTEXT);
    restart = Parameters.isSet(modes, COx.MASK_MODE_PASS_TERMINATION);
    causalCtx = Parameters.isSet(modes, COx.MASK_MODE_CAUSAL);
    errorCheck = Parameters.isSet(modes, COx.MASK_MODE_PREDICTABLE_TERMINATION);
    segmentationMarks = Parameters.isSet(modes, COx.MASK_MODE_SEGMENTATION_SYMBOLS);

    stripeHeights = new int[numStripes];
    final int lastStripeHeight = region.size.y & 0x3;
    if (lastStripeHeight != 0) {
      final int lastStripeIndex = numStripes - 1;
      Arrays.fill(stripeHeights, 0, lastStripeIndex, 4);
      stripeHeights[lastStripeIndex] = lastStripeHeight;
    } else {
      Arrays.fill(stripeHeights, 4);
    }
  }

  private int getModes() {
    return bandPrecinct.subband.resolution.tileComp.modes;
  }

  private GridRegion createGridElement() {
    final Subband subband = bandPrecinct.subband;
    final Resolution resolution = subband.resolution;
    final Region bandPrecinctRegion = copyOf(bandPrecinct.region().absolute());
    final Region blockPartition = copyOf(subband.blockPartition);

    final Region codeBlockRegion;
    if (resolution.resLevel == 0) {
      codeBlockRegion = replicate(blockPartition, indices.x, indices.y, bandPrecinctRegion);
    } else {
      final Pair relativeSubbandPos = subband.region().relativeTo(resolution.region()).pos;
      bandPrecinctRegion.displaceBy(relativeSubbandPos.inverse());
      codeBlockRegion = replicate(blockPartition, indices.x, indices.y, bandPrecinctRegion);
      bandPrecinctRegion.displaceBy(relativeSubbandPos);
      codeBlockRegion.displaceBy(relativeSubbandPos);
    }

    codeBlockRegion.clampTo(bandPrecinctRegion);
    return new DefaultGridRegion(codeBlockRegion);
  }

  private static int oobMarker(int size) {
    switch (size){
      case 1 : // Last 3 numRows of last stripe unoccupied
        return (OOB_MARKER << 3) | (OOB_MARKER << 6) | (OOB_MARKER << 9);
      case 2 : // Last 2 numRows of last stripe are empty
        return (OOB_MARKER << 6) | (OOB_MARKER << 9);
      default :
        return (OOB_MARKER << 9);
    }
  }

  @Override
  public void start(DecoderParameters parameters) {
    final Region region = gridRegion.absolute();

    final int ctxRowGap = region.size.x + States.EXTRA_CONTEXT_WORDS;
    final int numSamples = (numStripes << 2) * region.size.x;
    final int numCtxWords = (numStripes + 2) * ctxRowGap + 1;

    final int[] ctxBuffer = new int[numCtxWords];
    final int ctxIdxInitial = ctxRowGap + 1;

    if ((region.size.y & 3) != 0) {
      final int oobMarker = oobMarker(region.size.y & 3);
      int ctxIdx = ctxIdxInitial + (numStripes - 1) * ctxRowGap;
      for (int k = region.size.x; k > 0; k--) {
        ctxBuffer[ctxIdx++] = oobMarker;
      }
    }

    // Initialize the extra context words between lines to OOB
    final int oobMarker = OOB_MARKER | (OOB_MARKER << 3) | (OOB_MARKER << 6) | (OOB_MARKER << 9);
    assert (ctxRowGap >= (region.size.x + 3));
    int ctxIdx = ctxIdxInitial + region.size.x;

    for (int k = numStripes; k > 0; k--, ctxIdx += ctxRowGap) {
      // Need 3 OOB words after line
      ctxBuffer[ctxIdx] = ctxBuffer[ctxIdx + 1] = ctxBuffer[ctxIdx + 2] = oobMarker;
    }

    // Initialize the stripe heights
    stripeHeights = new int[numStripes];
    final int lastStripeHeight = region.size.y & 0x3;
    if (lastStripeHeight != 0) {
      final int lastStripeIndex = numStripes - 1;
      Arrays.fill(stripeHeights, 0, lastStripeIndex, 4);
      stripeHeights[lastStripeIndex] = lastStripeHeight;
    } else {
      Arrays.fill(stripeHeights, 4);
    }

    state = new CodeBlockState();
    state.sampleBuffer = new int[numSamples];

    if (Debug.VISUALIZE_BLOCK_RESULT) {
      Arrays.fill(state.sampleBuffer, 128);
    }

    state.ctxBuffer = ctxBuffer;
    state.bitplane = bitplaneMax;
  }

  @Override
  public void free() {
    state = null;
  }

  @Override
  public GridRegion region() {
    return gridRegion;
  }
}
