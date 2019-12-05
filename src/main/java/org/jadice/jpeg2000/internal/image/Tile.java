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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.stream.ImageInputStream;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.codestream.Codestreams;
import org.jadice.jpeg2000.internal.codestream.MarkerSegmentContainer;
import org.jadice.jpeg2000.internal.codestream.TilePartPointer;
import org.jadice.jpeg2000.internal.codestream.TilePartPointerProvider;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.Pushable;
import org.jadice.jpeg2000.internal.io.MarkerReader;
import org.jadice.jpeg2000.internal.marker.COD;
import org.jadice.jpeg2000.internal.marker.COx;
import org.jadice.jpeg2000.internal.marker.Marker;
import org.jadice.jpeg2000.internal.marker.MarkerKey;
import org.jadice.jpeg2000.internal.marker.MarkerSegment;
import org.jadice.jpeg2000.internal.marker.POC;
import org.jadice.jpeg2000.internal.marker.SOT;
import org.jadice.jpeg2000.internal.param.Parameters;
import org.jadice.jpeg2000.internal.tier2.DefaultPacketSequencer;
import org.jadice.jpeg2000.internal.tier2.PacketJigsaw;
import org.jadice.jpeg2000.internal.tier2.PacketSequencer;
import org.jadice.jpeg2000.internal.tier2.ProgressionOrders;
import org.jadice.jpeg2000.msg.CodestreamMessages;
import org.jadice.jpeg2000.msg.ValidationMessages;

public class Tile implements Pushable, HasGridRegion {

  public Codestream codestream;

  /** The tile index - derived from its location on the tile grid (in common raster order). */
  public int idx;

  /** The horizontal and vertical coordinate of this tile. */
  public Pair tileIndices;

  /** Designates the region on the reference grid occupied by this tile. */
  private GridRegion gridRegion;

  /**
   * Keeps references to {@link TileComponent} representations. Each tile-component has an index and its representation
   * is placed at corresponding index in this array.
   */
  protected TileComponent[] tileComps;

  /**
   * Total number of tile-parts which belong to this tile. The value is set in two ways:
   * <ul>
   * <li>Signalled by one of the corresponding {@link SOT#TNsot} parameters.</li>
   * <li>Derived from the amount of specified {@link org.jadice.jpeg2000.internal.marker.TLM} marker
   * segment entries.</li>
   * </ul>
   * <b>Note:</b> If the value is <code>0</code> it is currently unknown but may be updated later.
   */
  public int numTileParts;

  public boolean useSOP;
  public boolean useEPH;
  public boolean useMCT;

  /** Number of quality layers in the tile. */
  public int numLayers;

  /**
   * Defines the code-block anchor point (CBAP) introduced in <i>ITU-T.801, Annex I, Chapter I.2</i>.
   * <p>
   * The code-block anchor point is defined by <i>z<sub>x</sub></i> and <i>z<sub>y</sub></i> signalled by a extended
   * version of the {@link COD#Scod} parameter (see <i>ITU-T.801, A.2.3</i>). If they are both equal to zero, then no
   * modification need be made. If either of <i>z<sub>x</sub></i> and <i>z<sub>y</sub></i> is equal to 1, then the
   * image and compressed image data ordering as described in <i>ITU-T.800, Annex B</i> is modified by <i>ITU-T.801,
   * I.2</i>.
   */
  public Pair codeBlockAnchorPoint;

  public MarkerSegmentContainer markers;

//  public int maxRelevantLayers;
//  public int numPrecinctsTotal;
//  public int maxRelevantPackets;

  public PacketSequencer packetSequencer;

  public TileState state;

  protected Tile() {
    
  }
  
  public Tile(Pair tileIndices, Codestream codestream) throws JPEG2000Exception, IOException {
    this.tileIndices = tileIndices;
    this.codestream = codestream;

    idx = tileIndices.y * codestream.numTiles.x + tileIndices.x;
    gridRegion = createGridRegion();

    final TilePartPointerProvider tilePartPointerProvider = codestream.accessTilePartPointerProvider();
    List<TilePartPointer> tilePartPointers = tilePartPointerProvider.getTilePartPointers(idx);
    if (tilePartPointers == null || tilePartPointers.isEmpty()) {
      final SOT sot = tilePartPointerProvider.findTileHeader(idx, codestream);
      if (sot == null) {
        throw new JPEG2000Exception(CodestreamMessages.MISSING_TILE_HEADER, idx);
      }
      if (sot.TNsot > 0) {
        numTileParts = sot.TNsot;
      }
      tilePartPointers = tilePartPointerProvider.getTilePartPointers(idx);
    }

    if (tilePartPointerProvider.fromTLM) {
      numTileParts = tilePartPointers.size();
    }

    final TilePartPointer tilePartPointer = tilePartPointers.get(0);
    if (tilePartPointer == null) {
      throw new JPEG2000Exception(CodestreamMessages.MISSING_TILE_HEADER, idx);
    }

    initTile(tilePartPointer);    
    initCodingParameters();
    initPacketSequencing();
  }

  private GridRegion createGridRegion() {
    final GridRegion parent = codestream.region();
    final Region canvas = parent.absolute();
    final Region tilePartition = codestream.tilePartition;
    final Region tileRegion = Canvas.tileRegion(canvas, tilePartition, tileIndices.x, tileIndices.y);
    return new DefaultGridRegion(tileRegion);
  }

  private void initTile(TilePartPointer tilePartPointer) throws IOException, JPEG2000Exception {
    markers = new MarkerSegmentContainer();
    final MarkerReader markerReader = new MarkerReader();
    final ImageInputStream source = codestream.source;
    source.seek(tilePartPointer.tilePartStart);

    final Marker marker = markerReader.next(source);
    if (marker != Marker.SOT) {
      throw new JPEG2000Exception(CodestreamMessages.EXPECTED_SOT_MARKER, marker);
    }
    marker.createMarkerSegment().read(source, codestream, false);

    Codestreams.readTilePartHeader(source, codestream, markers);

    // Update the pointer into codestream's source where the tile's first tile-part data starts.
    tilePartPointer.tilePartDataStart = source.getStreamPosition();
    tilePartPointer.tile = this;
  }

  private void initCodingParameters() throws JPEG2000Exception {
    final COD cod = COx.accessCOD(codestream, this);
    useSOP = Parameters.isSet(cod.Scod, COx.MASK_CODING_SOP_MARKER);
    useEPH = Parameters.isSet(cod.Scod, COx.MASK_CODING_EPH_MARKER);
    useMCT = Parameters.isSet(cod.SGcod_MCT, COx.MASK_MCT_YCC);
    numLayers = cod.SGcod_layers;
    codeBlockAnchorPoint = new Pair(
        Parameters.extract(cod.Scod, COx.MASK_HOR_CODING_ORIGIN, COx.SHIFT_HOR_CODING_ORIGIN),
        Parameters.extract(cod.Scod, COx.MASK_VERT_CODING_ORIGIN, COx.SHIFT_VERT_CODING_ORIGIN));

    if (useMCT && codestream.numComps < 3) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_NUMBER_OF_COMPS);
    }
  }

  public TileComponent accessTileComp(int c) throws JPEG2000Exception {
    if (c > (codestream.numComps - 1)) {
      throw new IllegalArgumentException("component index exceeds amount of available components.");
    }
    
    if(tileComps == null) {
      tileComps = new TileComponent[codestream.numComps];
    }

    TileComponent tileComp = tileComps[c];
    if(tileComp == null) {
      final Component comp = codestream.comps[c];
      tileComp = new TileComponent(codestream, this, comp);
      tileComps[c] = tileComp;
    }

    return tileComp;
  }

  private void initPacketSequencing() throws JPEG2000Exception {
    packetSequencer = new DefaultPacketSequencer();

    final MarkerKey pocKey = new MarkerKey(Marker.POC);
    POC poc = (POC) markers.access(pocKey);
    if (poc == null) {
      poc = (POC) codestream.markers.access(pocKey);
    }

    if (poc != null) {
      final PacketJigsaw[] jigsaws = ProgressionOrders.create(poc);
      for (final PacketJigsaw order : jigsaws) {
        order.solve(this, packetSequencer);
      }
    } else {
      final PacketJigsaw jigsaw = ProgressionOrders.create(codestream, this);
      jigsaw.solve(this, packetSequencer);
    }
  }

  public void readTilePartHeader(TilePartPointer tilePartPointer) throws IOException, JPEG2000Exception {
    tilePartPointer.tile = this;
    
    final ImageInputStream source = codestream.source;
    source.seek(tilePartPointer.tilePartStart);

    final MarkerReader markerReader = new MarkerReader();
    final Marker marker = markerReader.next(source);
    if (marker != Marker.SOT) {
      throw new JPEG2000Exception(CodestreamMessages.EXPECTED_SOT_MARKER);
    }

    final MarkerSegment markerSegment = Codestreams.readSegment(source, codestream, Marker.SOT);
    if (!(markerSegment instanceof SOT)) {
      throw new JPEG2000Exception(CodestreamMessages.EXPECTED_SOT_MARKER);
    }

    final SOT sot = (SOT) markerSegment;
    if (sot.TPsot == 0) {
      source.seek(tilePartPointer.tilePartDataStart);
      return;
    }

    final MarkerSegmentContainer tilePartMarkers = new MarkerSegmentContainer();
    Codestreams.readTilePartHeader(source, codestream, tilePartMarkers);
  }

  @Override
  public void start(DecoderParameters parameters) {
    state = new TileState();
    state.remainingComps = new AtomicInteger(tileComps.length);
  }

  @Override
  public void free() {

  }

  @Override
  public GridRegion region() {
    return gridRegion;
  }

  @Override
  public String toString() {
    return "Tile [region=" + gridRegion.absolute() + "]";
  }
}
