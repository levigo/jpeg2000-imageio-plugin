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
package com.levigo.jadice.format.jpeg2000.internal.codestream;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.stream.ImageInputStream;

import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;
import com.levigo.jadice.format.jpeg2000.internal.image.Canvas;
import com.levigo.jadice.format.jpeg2000.internal.image.Component;
import com.levigo.jadice.format.jpeg2000.internal.image.DefaultGridRegion;
import com.levigo.jadice.format.jpeg2000.internal.image.GridRegion;
import com.levigo.jadice.format.jpeg2000.internal.image.HasGridRegion;
import com.levigo.jadice.format.jpeg2000.internal.image.Pair;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.Regions;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.io.MarkerReader;
import com.levigo.jadice.format.jpeg2000.internal.marker.COM;
import com.levigo.jadice.format.jpeg2000.internal.marker.CRG;
import com.levigo.jadice.format.jpeg2000.internal.marker.Marker;
import com.levigo.jadice.format.jpeg2000.internal.marker.MarkerKey;
import com.levigo.jadice.format.jpeg2000.internal.marker.MarkerSegment;
import com.levigo.jadice.format.jpeg2000.internal.marker.PPM;
import com.levigo.jadice.format.jpeg2000.internal.marker.SIZ;
import com.levigo.jadice.format.jpeg2000.internal.marker.SOT;
import com.levigo.jadice.format.jpeg2000.internal.marker.TLM;
import com.levigo.jadice.format.jpeg2000.internal.param.Parameters;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;
import com.levigo.jadice.format.jpeg2000.msg.ValidationMessages;

/**
 * Entire JPEG2000 input stream representation.
 * <p>
 * Note: The PDF Spec calls this "JPXDecode Filter". However, this doesn't represent the file format
 * called JPX (JPEG 2000 Part 2 (Extensions) jpf (jpx) File Format).
 */
public class Codestream implements Pushable, HasGridRegion {

  private static final QualifiedLogger LOGGER = LoggerFactory.getQualifiedLogger(Codestream.class);

  public final ImageInputStream source;
  private final long sourceStart;

  /**
   * Holds the value for or from <code>Rsiz</code> parameter which specifies the capabilities a decoder or encoder
   * should be able to satisfy in order to process the codestream fully and as intended. To easy access the information
   * the {@link Capability} enumeration offers several methods to interact with this value and the codestream.
   */
  public int capabilities;

  /**
   * Describes the upper left tile region. This can serve as the key cell for tiles. Replications through the canvas
   * will form the tile grid.
   */
  public Region tilePartition;

  /**
   * Array holding references to tile representations. The array may be sparse. Tiles may be instantiated when needed 
   * and should be destroyed as soon as possible. If a {@link SIZ} marker was found and processed via 
   * {@link #constructCommon(SIZ)} this array is initialized to be able to keep all tiles defined in this codestream.
   */
  public Tile[] tiles;

  /**
   * Keeps the number of tiles in horizontal (<code>x</code>) and vertical (<code>y</code>) direction separately.
   */
  public Pair numTiles;

  /**
   * Keeps basic information about components.
   */
  public Component[] comps;

  /**
   * Number of components in the codestream. Should be tightly coupled with the {@link #comps} array.
   */
  public int numComps;

  public MarkerSegmentContainer markers;

  public PrecinctProvider precinctProvider;
  
  private TilePartPointerProvider tilePartPointerProvider;
  
  public PackedPacketHeaderProvider packedPacketHeaderProvider;

  /**
   * Keeps all {@link COM} marker segments for later access. Field may be <code>null</code> if no {@link COM} marker
   * segments have been signalled in the codestream's main header.
   */
  public LinkedList<COM> comSegments;

  /**
   * Position in the codestream's source which points to the first unread {@link SOT} marker segment. After the
   * initialization this position is pointing to the very first tile-part's {@link SOT} marker segment. If the reading
   * process advanced, this position is set to point to the next unread {@link SOT} marker segment.
   */
  public long nextTilePartAddress;

  /**
   * Position in the codestream's source which points to the very first {@link SOT} marker segment. This field should
   * never been altered as it is the point of return for each sequential (codestream-driven) decoder.
   */
  public long firstTilePartAddress;

  public boolean constructionFinalized;

  public CodestreamState state;

  /**
   * Describes the main reference grid where the region's <code>x</code>, <code>y</code>, <code>width</code> and
   * <code>height</code> corresponds to the parameters <code>XOsiz</code>, <code>YOsiz</code>, <code>Xsiz</code> and
   * <code>Ysiz</code> respectively.
   */
  protected GridRegion canvas;

  public Codestream(ImageInputStream source) throws IOException {
    this(source, source.getStreamPosition());
  }

  public Codestream(ImageInputStream source, long sourceStart) {
    this.source = source;
    this.sourceStart = sourceStart;
    markers = new MarkerSegmentContainer();
    constructionFinalized = false;
  }

  public Codestream() {
    source = null;
    sourceStart = 0;
  }

  public void init() throws IOException, JPEG2000Exception {
    synchronized (source) {
      source.seek(sourceStart);
      final MarkerReader markerReader = new MarkerReader();
      Marker marker = markerReader.next(source);
      if (marker != Marker.SOC) {
        throw new JPEG2000Exception(CodestreamMessages.EXPECTED_SOC_MARKER);
      }

      marker = markerReader.next(source);
      if (marker != Marker.SIZ) {
        throw new JPEG2000Exception(CodestreamMessages.EXPECTED_SIZ_MARKER);
      }

      constructCommon((SIZ) Codestreams.readSegment(source, this, marker));
    }
  }

  /**
   * If a {@link SIZ} marker segment was found or created, we are able to extract basic information and build up the
   * fundamental infrastructure as the base for further processing.
   *
   * @param siz {@link SIZ} marker segment as signalled by the codestream's source.
   *
   * @throws JPEG2000Exception
   */
  public void constructCommon(SIZ siz) throws JPEG2000Exception, IOException {
    if (canvas != null) {
      throw new JPEG2000Exception(CodestreamMessages.ALREADY_FOUND_SIZ);
    }

    capabilities = siz.Rsiz;
    numComps = siz.Csiz;

    final Region canvas = Canvas.canvasRegion(siz);
    if (canvas.size.y <= 0 || canvas.size.x <= 0) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_CANVAS_SIZE, canvas);
    }
    this.canvas = new DefaultGridRegion(canvas);

    tilePartition = Canvas.tilePartitionRegion(siz);
    if (!Regions.intersect(canvas, tilePartition)) {
      throw new JPEG2000Exception(CodestreamMessages.EMPTY_TILE_INTERSECTION);
    }

    comps = new Component[numComps];
    for (int compIdx = 0; compIdx < numComps; compIdx++) {
      final Pair subsampling = new Pair();
      subsampling.x = siz.XRsiz[compIdx];
      subsampling.y = siz.YRsiz[compIdx];

      final Component comp = new Component();
      comp.idx = compIdx;
      comp.precision = Parameters.extract(siz.Ssiz[compIdx], SIZ.MASK_BIT_DEPTH) + 1;
      comp.isSigned = Parameters.isSet(siz.Ssiz[compIdx], SIZ.MASK_SAMPLE_SIGN);
      comp.subsampling = subsampling;
      comps[compIdx] = comp;
    }

    numTiles = Canvas.numTiles(canvas, tilePartition);
    final int numTilesTotal = numTiles.x * numTiles.y;
    if (numTilesTotal > 65535) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_TILE_COUNT, numTilesTotal);
    }

    tiles = new Tile[numTilesTotal];

    validateProfile(numTilesTotal);

    // Set up common services which we definitely going to need.
    precinctProvider = new PrecinctProvider();
    tilePartPointerProvider = new TilePartPointerProvider();

    readMainHeader();
    finalizeConstruction();
  }

  private void validateProfile(int numTilesTotal) throws JPEG2000Exception {
    if (Capability.T800_PROFILE_0.isUsedBy(this)) {
      final Region canvas = this.canvas.absolute();
      if ((tilePartition.size.x != 128 || tilePartition.size.y != 128) && numTilesTotal > 1) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error(ValidationMessages.PROFILE0_VIOLATION);
          LOGGER.error(ValidationMessages.PROFILE0_VIOLATION_TILE_SIZE);
        }
        throw new JPEG2000Exception(ValidationMessages.PROFILE0_VIOLATION_TILE_SIZE);
      } else if (tilePartition.pos.x != 0 || tilePartition.pos.y != 0 || canvas.pos.x != 0 || canvas.pos.y != 0) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error(ValidationMessages.PROFILE0_VIOLATION);
          LOGGER.error(ValidationMessages.PROFILE0_VIOLATION_OFFSET);
        }
        throw new JPEG2000Exception(ValidationMessages.PROFILE0_VIOLATION_OFFSET);
      } else {
        for (int compIdx = 0; compIdx < numComps; compIdx++) {
          final Component comp = comps[compIdx];
          final int x = comp.subsampling.x;
          final int y = comp.subsampling.y;

          // @formatter:off
          if ((x != 1 && x != 2 && x != 4)
            ||(y != 1 && y != 2 && y != 4)) { // @formatter:on

            if (LOGGER.isErrorEnabled()) {
              LOGGER.error(ValidationMessages.PROFILE0_VIOLATION);
              LOGGER.error(ValidationMessages.PROFILE0_VIOLATION_SUBSAMPLING);
            }
            throw new JPEG2000Exception(ValidationMessages.PROFILE0_VIOLATION_SUBSAMPLING);
          }
        }
      }
    } else if (Capability.T800_PROFILE_1.isUsedBy(this)) {
      if (numTilesTotal > 1) {
        if (tilePartition.size.x != tilePartition.size.y) {
          if (LOGGER.isErrorEnabled()) {
            LOGGER.error(ValidationMessages.PROFILE1_VIOLATION);
            LOGGER.error(ValidationMessages.PROFILE1_VIOLATION_TILE_SIZE);
          }
          throw new JPEG2000Exception(ValidationMessages.PROFILE1_VIOLATION_TILE_SIZE);
        } else {
          for (int compIdx = 0; compIdx < numTilesTotal; compIdx++) {
            final Component comp = comps[compIdx];
            
            // @formatter:off
            if (tilePartition.size.x > (comp.subsampling.x << 10)
              || tilePartition.size.y > (comp.subsampling.y << 10)) { // @formatter:on
              
              if (LOGGER.isErrorEnabled()) {
                LOGGER.error(ValidationMessages.PROFILE1_VIOLATION);
                LOGGER.error(ValidationMessages.PROFILE1_VIOLATION_COMPONENT_SIZE);
              }
              throw new JPEG2000Exception(ValidationMessages.PROFILE1_VIOLATION_COMPONENT_SIZE);
            }
          }
        }
      }
    }
  }

  private void readMainHeader() throws JPEG2000Exception, IOException {
    if (source == null) {
      throw new JPEG2000Exception(CodestreamMessages.MISSING_SOURCE_FOR_READING);
    }

    final MarkerReader markerReader = new MarkerReader();
    Marker marker = markerReader.next(source);

    while (marker != Marker.SOT) {
      switch (marker){
        case PPM:
          readPPM(marker);
          break;

        case TLM:
          readTLM(marker);
          break;

        case COM:
          readCOM(marker);
          break;

        default:
          final MarkerSegment markerSegment = Codestreams.readSegment(source, this, marker);
          if (markerSegment == null) {
            LOGGER.warn(CodestreamMessages.UNSUPPORTED_MARKER, marker);
            break;
          }
          final MarkerKey markerKey = markerSegment.getMarkerKey();
          markers.register(markerKey, markerSegment);
      }

      marker = markerReader.next(source);
    }

    // Push back between the last byte of the main header and the first byte of the first tile-part.
    nextTilePartAddress = source.getStreamPosition() - 2;
    firstTilePartAddress = nextTilePartAddress;
  }

  private void readCOM(Marker marker) throws JPEG2000Exception, IOException {
    if (comSegments == null) {
      comSegments = new LinkedList<>();
    }
    comSegments.add((COM) Codestreams.readSegment(source, this, marker));
  }

  private void readTLM(Marker marker) throws JPEG2000Exception, IOException {
    tilePartPointerProvider.registerTLM((TLM) Codestreams.readSegment(source, this, marker));
  }

  private void readPPM(Marker marker) throws JPEG2000Exception, IOException {
    if (Capability.T800_PROFILE_0.isUsedBy(this)) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error(ValidationMessages.PROFILE0_VIOLATION);
        LOGGER.error(ValidationMessages.PROFILE0_VIOLATION_PPM_DISALLOWED);
      }
      throw new JPEG2000Exception(ValidationMessages.PROFILE0_VIOLATION_PPM_DISALLOWED);
    }

    if (packedPacketHeaderProvider == null) {
      packedPacketHeaderProvider = new PackedPacketHeaderProvider();
    }
    packedPacketHeaderProvider.register((PPM) Codestreams.readSegment(source, this, marker));
  }

  private void finalizeConstruction() throws JPEG2000Exception, IOException {
    if (tilePartPointerProvider.hasTLM()) {
      tilePartPointerProvider.finish(firstTilePartAddress);
    }

    // Obtain codestream registration information if present.
    final CRG crg = (CRG) markers.access(Marker.CRG.key());
    if (crg != null) {
      for (int c = 0; c < numComps; c++) {
        final Component comp = comps[c];
        comp.crgX = crg.Xcrg[c];
        comp.crgY = crg.Ycrg[c];
      }
    }

    // TODO
    // Obtain the downsampling factor structure if present.

    // TODO
    // Obtain MCC and MCO marker segments and handle the signalled information.

    constructionFinalized = true;
  }

  /**
   * Equivalent to a call of {@link #accessTile(Pair, boolean)} with the corresponding tile indices and {@code true}
   * for the creation flag.
   *
   * @param tileIndices the requested tile's horizontal and vertical id.
   *
   * @return a cached or freshly created {@link Tile}.
   *
   * @throws IOException if something went wrong while reading this {@link Codestream}'s {@link #source}.
   */
  public Tile accessTile(Pair tileIndices) throws IOException, JPEG2000Exception {
    return accessTile(tileIndices, true);
  }

  /**
   * @param tileIndices the requested tile's horizontal and vertical id.
   * @param create {@code true} if a missing tile should be created, {@code false} if the caller attends to handle a 
   * missing tile by himself.
   *
   * @return a cached {@link Tile} or {@code null} if tile is not yet created and the creation is not allowed.
   *
   * @throws IOException
   * @throws JPEG2000Exception
   */
  public Tile accessTile(Pair tileIndices, boolean create) throws IOException, JPEG2000Exception {
    final int idx = tileIndices.y * numTiles.x + tileIndices.x;
    Tile tile = tiles[idx];

    if (tile == null && create) {
      tile = new Tile(tileIndices, this);
      tiles[tile.idx] = tile;
    }

    return tile;
  }

  public TilePartPointerProvider accessTilePartPointerProvider() {
    if (tilePartPointerProvider == null) {
      tilePartPointerProvider = new TilePartPointerProvider();
    }
    return tilePartPointerProvider;
  }

  @Override
  public void start(DecoderParameters parameters) {
    state = new CodestreamState();
    final Region region = parameters.region;
    if (region == null || region.equals(canvas.absolute())) {
      state.remainingTiles = new AtomicInteger(numTiles.x * numTiles.y);
    } else {
      final Pair firstChildIdx = Regions.getFirstChildIdx(region, tilePartition);
      final Pair lastChildIdx = Regions.getLastChildIdx(region, tilePartition);
      final int numRequestedTilesX = lastChildIdx.x - firstChildIdx.x + 1;
      final int numRequestedTilesY = lastChildIdx.y - firstChildIdx.y + 1;

      state.remainingTiles = new AtomicInteger(numRequestedTilesX * numRequestedTilesY);
    }
  }

  @Override
  public void free() {
    state = null;
    tilePartPointerProvider = new TilePartPointerProvider();
    Arrays.fill(tiles, null);  
  }

  @Override
  public GridRegion region() {
    return canvas;
  }
}