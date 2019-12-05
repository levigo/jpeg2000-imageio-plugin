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

import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.buffer.Buffers;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.Pushable;
import org.jadice.jpeg2000.internal.marker.COC;
import org.jadice.jpeg2000.internal.marker.COD;
import org.jadice.jpeg2000.internal.marker.COx;
import org.jadice.util.log.qualified.QualifiedLogger;

public class TileComponent implements Pushable, HasGridRegion {

  private static final QualifiedLogger LOGGER = getQualifiedLogger(TileComponent.class);

  /**
   * Describes the area on the high resolution reference grid which is occupied by this tile-component. For details,
   * see chapter <i>ITU-T.800, B.3</i>.
   */
  protected GridRegion gridRegion;

  /** Reference to the enclosing {@link Codestream}. */
  public Codestream codestream;

  /** Reference to parent (enclosing) {@link Tile}. */
  public Tile tile;

  /** Reference to parent {@link Component}. */
  public Component comp;

  /** References to all child {@link Resolution}s. */
  protected Resolution[] resolutions;

  /** Number of decomposition levels as signalled by either {@link COC#SP_NL} or {@link COD#SP_NL}. */
  public int dwtLevels;

  public int kernelId;

  /** Indicates whether there is a reversible or irreversible kernel. */
  public boolean reversible;

  public int xcb;
  public int ycb;
  public Pair blockSize;

  /**
   * Style of the code-block coding passes. For details see ITU-T.800, Table A.23. To access flags
   * use integer masks with the 'MASK_MODE_' prefix defined in {@link COx}.
   */
  public int modes;

  public int[] userPPs;

  public TileComponentState state;

  public int layers;

  protected TileComponent() {
    
  }
  
  public TileComponent(Codestream codestream, Tile tile, Component comp) throws JPEG2000Exception {
    this.codestream = codestream;
    this.tile = tile;
    this.comp = comp;

    gridRegion = createGridRegion();

    // Get the component-specific coding (COC or COD) parameters
    final COx cox = COx.accessCOx(codestream, tile, comp.idx);
    final COD cod = COx.accessCOD(codestream, tile);

    if (cox == null) {
      throw new IllegalArgumentException("coding parameters providing marker segment must not be null");
    }

    // init basic coding parameters as signalled in COD/COx marker segments
    layers = cod.SGcod_layers;
    dwtLevels = cox.SP_NL;
    kernelId = cox.SP_kernel;
    xcb = cox.SP_xcb;
    ycb = cox.SP_ycb;
    modes = cox.SP_modes;
    userPPs = cox.SP_precincts;
    
    blockSize = new Pair(1 << xcb, 1 << ycb);

    // TODO Retrieve ATK marker segment
    if (kernelId == COx.VALUE_KERNEL_5_3) {
      reversible = true;
    } else if (kernelId == COx.VALUE_KERNEL_9_7) {
      reversible = false;
    }
  }

  private GridRegion createGridRegion() {
    final Region absoluteTileRegion = tile.region().absolute();
    final Region absoluteTileCompRegion = Canvas.tileComponentRegion(absoluteTileRegion, comp.subsampling);
    return new DefaultGridRegion(absoluteTileCompRegion);
  }

  public Resolution accessResolution(int resolutionLevel) throws JPEG2000Exception {
    if (resolutionLevel > dwtLevels) {
      throw new IllegalArgumentException("Index of resolution level may not exceed the number of decomposition levels");
    }

    if (resolutions == null) {
      resolutions = new Resolution[dwtLevels + 1];
    }

    Resolution resolution = resolutions[resolutionLevel];
    if (resolution == null) {
      final int dwtLevel = dwtLevels - resolutionLevel;
      resolution = new Resolution(codestream, this, dwtLevel, resolutionLevel);
      resolutions[resolutionLevel] = resolution;
    }
    return resolution;
  }

  public int numResolutions() {
    return dwtLevels + 1;
  }

  @Override
  public GridRegion region() {
    return gridRegion;
  }

  @Override
  public void start(DecoderParameters parameters) {
    state = new TileComponentState();
    state.sampleBuffer = Buffers.createTileComponentBuffer(this);
  }

  @Override
  public void free() {
    state = null;
  }

  @Override
  public String toString() {
    return "TileComponent [region=" + gridRegion + ", tile=" + tile + ", comp=" + comp + "]";
  }
}
