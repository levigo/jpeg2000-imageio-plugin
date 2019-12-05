package com.levigo.jadice.format.jpeg2000.internal.image;

import static com.levigo.jadice.format.jpeg2000.internal.Functions.isPower2;

import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.marker.COC;
import com.levigo.jadice.format.jpeg2000.internal.marker.COD;
import com.levigo.jadice.format.jpeg2000.internal.marker.SIZ;
import com.levigo.jadice.format.jpeg2000.msg.ValidationMessages;

/**
 * imageable area
 */
public class Canvas {

  /**
   * Creates a {@link Region}-object that describes the basic reference grid signalled by the {@link SIZ} marker
   * segment. Basically, the parameters used to form the canvas that describes the imageable region are
   * <code>XOsiz</code>, <code>YOsiz</code>, <code>Xsiz</code> and <code>Ysiz</code>.
   *
   * @param siz a populated {@link SIZ} marker segment.
   * @return a new {@link Region}-object based on signalled or populated {@link SIZ} marker segment parameters.
   */
  public static Region canvasRegion(SIZ siz) {
    final Region canvas = new Region();
    canvas.pos = new Pair(siz.XOsiz, siz.YOsiz);
    canvas.size = new Pair(siz.Xsiz - canvas.pos.x, siz.Ysiz - canvas.pos.y);
    return canvas;
  }

  /**
   * Creates a {@link Region} object that describes the upper-left tile as signalled by the {@link SIZ} marker segment.
   * The parameters used to form this region are <code>XTOsiz</code>, <code>YTOsiz</code>, <code>XTsiz</code> and
   * <code>Ysiz</code>.
   *
   * @param siz a populated {@link SIZ} marker segment.
   * @return a new {@link Region}-object based on signalled or populated {@link SIZ} marker segment parameters.
   */
  public static Region tilePartitionRegion(SIZ siz) {
    final Region tilePartition = new Region();
    tilePartition.pos = new Pair(siz.XTOsiz, siz.YTOsiz);
    tilePartition.size = new Pair(siz.XTsiz, siz.YTsiz);
    return tilePartition;
  }

  public static final int BRANCH_LOW_PASS = 0;
  public static final int BRANCH_HIGH_PASS = 1;
  public static final int BRANCH_NO_SPLIT = 2;

  /**
   * Converts a region in the parent node into a region for one of its children, given the branch indices.
   *
   * @param parent  the parent region to be converted into a region for one of its children.
   * @param branchX takes a value of {@link #BRANCH_LOW_PASS} ({@value #BRANCH_LOW_PASS} for low-pass, {@link
   *                #BRANCH_HIGH_PASS} ({@value #BRANCH_HIGH_PASS} for high-pass and {@link #BRANCH_NO_SPLIT} ({@value
   *                #BRANCH_NO_SPLIT}) for no splitting in horizontal direction at all.
   * @param branchY takes a value of {@link #BRANCH_LOW_PASS} ({@value #BRANCH_LOW_PASS} for low-pass, {@link
   *                #BRANCH_HIGH_PASS} ({@value #BRANCH_HIGH_PASS} for high-pass and {@link #BRANCH_NO_SPLIT} ({@value
   *                #BRANCH_NO_SPLIT}) no splitting in vertical direction at all.
   * @param lowMin  used for low-pass subbands to extend the region in the parent node before reducing it to a subband
   *                region (lower bounds). Non-zero values for this argument should be used when mapping a region of
   *                interest from a parent node into its children.
   * @param lowMax  used for low-pass subbands to extend the region in the parent node before reducing it to a subband
   *                region (upper bounds). Non-zero values for this argument should be used when mapping a region of
   *                interest from a parent node into its children.
   * @param highMin used for high-pass subbands to extend the region in the parent node before reducing it to a subband
   *                region (lower bounds). Non-zero values for this argument should be used when mapping a region of
   *                interest from a parent node into its children.
   * @param highMax used for high-pass subbands to extend the region in the parent node before reducing it to a subband
   *                region (upper bounds). Non-zero values for this argument should be used when mapping a region of
   *                interest from a parent node into its children.
   * @return a new instance of {@link Region} describing the region of requested child.
   */
  public static Region createChild(Region parent,
      int branchX, int branchY,
      int lowMin, int lowMax,
      int highMin, int highMax) {

    final Pair min = parent.pos;
    final Pair limit = new Pair(min.x + parent.size.x, min.y + parent.size.y);

    final boolean splitHorizontally = branchX != BRANCH_NO_SPLIT;
    if (splitHorizontally) {
      min.x -= splitHorizontally ? highMax : lowMax;
      limit.x -= splitHorizontally ? highMin : lowMin;
      min.x = (min.x + 1 - branchX) >> 1;
      limit.x = (limit.x + 1 - branchX) >> 1;
    }

    final boolean splitVertically = branchY != BRANCH_NO_SPLIT;
    if (splitVertically) {
      min.y -= splitVertically ? highMax : lowMax;
      limit.y -= splitVertically ? highMin : lowMin;
      min.y = (min.y + 1 - branchY) >> 1;
      limit.y = (limit.y + 1 - branchY) >> 1;
    }

    final Region result = new Region();

    result.pos = min;
    result.size = new Pair(limit.x - min.x, limit.y - min.y);

    return result;
  }

  /**
   * Convenience function calling {@link #createChild(Region, int, int, int, int, int, int)} with limits set to
   * <code>0</code>.
   * <p>
   * Call this function is equal to a call of <code>Regions.createChild(parent, branchX, branchY, 0, 0, 0, 0)</code>.
   *
   * @param parent  the parent region to be converted into a region for one of its children.
   * @param branchX takes a value of {@link #BRANCH_LOW_PASS} ({@value #BRANCH_LOW_PASS} for low-pass, {@link
   *                #BRANCH_HIGH_PASS} ({@value #BRANCH_HIGH_PASS} for high-pass and {@link #BRANCH_NO_SPLIT} ({@value
   *                #BRANCH_NO_SPLIT}) for no splitting in horizontal direction at all.
   * @param branchY takes a value of {@link #BRANCH_LOW_PASS} ({@value #BRANCH_LOW_PASS} for low-pass, {@link
   *                #BRANCH_HIGH_PASS} ({@value #BRANCH_HIGH_PASS} for high-pass and {@link #BRANCH_NO_SPLIT} ({@value
   *                #BRANCH_NO_SPLIT}) no splitting in vertical direction at all.
   * @return a new instance of {@link Region} describing the region of requested child.
   */
  public static Region createChild(Region parent, int branchX, int branchY) {
    return createChild(parent, branchX, branchY, 0, 0, 0, 0);
  }

  /**
   * Convenience function calculating the amount of tiles in horizontal and vertical direction.
   *
   * @param canvas        the region representing the reference grid.
   * @param tilePartition the region designating the upper left tile.
   * @return a new instance of {@link Pair} with the amount of tiles in horizontal and vertical direction.
   * @see #numTilesX(Region, Region)
   * @see #numTilesY(Region, Region)
   */
  public static Pair numTiles(final Region canvas, final Region tilePartition) {
    return new Pair(numTilesX(canvas, tilePartition), numTilesY(canvas, tilePartition));
  }

  /**
   * Calculates the amount of tiles in horizontal direction.
   * <p>
   * <i>ITU-T.800, Equation (B-5):</i> <code> numXtiles = ceil( (Xsiz - XTOsiz) / XTsiz)</code>.
   *
   * @param canvas        the region representing the reference grid.
   * @param tilePartition the region designating the upper left tile.
   * @return amount of tiles in horizontal direction.
   */
  public static int numTilesX(final Region canvas, final Region tilePartition) {
    return (int) Math.ceil((canvas.size.x - tilePartition.pos.x) / (double) tilePartition.size.x);
  }

  /**
   * Calculates the amount of tiles in vertical direction.
   * <p>
   * <i>ITU-T.800, Equation (B-5)</i>: <code> numYtiles = ceil( (Ysiz - YTOsiz) / YTsiz)</code>.
   *
   * @param canvas        the region representing the reference grid.
   * @param tilePartition the region designating the upper left tile.
   * @return amount of tiles in vertical direction.
   */
  public static int numTilesY(final Region canvas, final Region tilePartition) {
    return (int) Math.ceil((canvas.size.y - tilePartition.pos.y) / (double) tilePartition.size.y);
  }

  /**
   * Calculates a resolution level coordinate as defined int <i>ITU-T.800, Equation B-14</i>.
   *
   * @param tcc a previously calculated tile-component coordinate that is associated with the requested resolution
   *            level coordinate.
   * @param r   the resolution level index, where 0 represents the <i>N<sub>L</sub>LL</i> band.
   * @param nl  the total number of decomposition levels in the current tile-component domain
   * @return a new resolution level coordinate corresponding to the given tile-component coordinate.
   */
  public static int resolutionCoordinate(final int tcc, final int r, final int nl) {
    return subsampleCoordinate(tcc, (1 << (nl - r)));
  }

  public static int subsampleCoordinate(final int value, final double factor) {
    return factor > 1 ? (int) Math.ceil(value / factor) : value;
  }

  /**
   * Calculates the horizontal amount of precincts of a specific resolution level. This is a convenience method calling
   * {@link #numPrecincts(int, int, int, int)} internally.
   *
   * @param trx0 the upper left x-coordinate of the resolution level
   * @param trx1 the upper right x-coordinate of the resolution level
   * @param zx   the anchor location in horizontal direction signalled in extended {@link COD} or {@link COC} marker
   *             segments.
   * @param ppx  precinct width exponent signaled in {@link COD} or {@link COC} marker segments. This value may only
   *             equal to zero at the resolution level corresponding to the <i>N<sub>L</sub></i>LL band.
   * @return the calculated number of precincts in horizontal direction.
   * @throws JPEG2000Exception if the specified coordinates (<code>trx0</code>, <code>trx1</code>) seem invalid.
   */
  public static int numPrecinctsX(final int trx0, final int trx1, final int zx, final int ppx)
      throws JPEG2000Exception {
    return numPrecincts(trx0, trx1, zx, ppx);
  }

  /**
   * Calculates the vertical amount of precincts of a specific resolution level. This is a convenience method calling
   * {@link #numPrecincts(int, int, int, int)} internally.
   *
   * @param try0 the upper left y-coordinate of the resolution level
   * @param try1 the lower left x-coordinate of the resolution level
   * @param zy   the anchor location in vertical direction signalled in extended {@link COD} or {@link COC} marker
   *             segments.
   * @param ppy  precinct height exponent signaled in {@link COD} or {@link COC} marker segments. This value may only
   *             equal to zero at the resolution level corresponding to the <i>N<sub>L</sub></i>LL band.
   * @return the calculated number of precincts in vertical direction.
   * @throws JPEG2000Exception if the specified coordinates (<code>try0</code>,
   *                           <code>try1</code>) seem invalid.
   */
  public static int numPrecinctsY(final int try0, final int try1, final int zy, final int ppy)
      throws JPEG2000Exception {
    return numPrecincts(try0, try1, zy, ppy);
  }

  /**
   * Calculates the amount of precincts of a specific resolution level dimension. The function embodies the equation
   * that computes the number of precincts which span the tile-component at resolution level, <i>r</i>, defined in
   * <i>ITU-T.800, Equation (B-16)</i>. This function is called only by the {@link #numPrecinctsX(int, int, int, int)}
   * and {@link #numPrecinctsY(int, int, int, int)} corresponding to the equation part <i>numprecinctswide</i> and
   * <i>numprecinctshigh</i> respectively.
   *
   * @param tr0 the upper left x- or y-coordinate of the resolution level of a tile-component.
   * @param tr1 the upper right x- or lower left y-coordinate of the resolution level of a tile-component.
   * @param z   the anchor location in horizontal or vertical direction corresponding to the signalled value by an
   *            extended {@link COD} or {@link COC} marker segment.
   * @param pp  precinct width or height exponent signaled in {@link COD} or {@link COC} marker segments. This value
   *            may only equal to zero at the resolution level corresponding to the <i>N<sub>L</sub></i>LL band.
   * @return the calculated number of precincts in vertical direction.
   * @throws JPEG2000Exception if the specified coordinates (<code>tr0</code>, <code>tr1</code>) seem to be invalid.
   */
  private static int numPrecincts(final int tr0, final int tr1, final int z, final int pp) throws JPEG2000Exception {
    if (tr1 == tr0) {
      return 0;
    }

    if (tr1 > tr0) {
      final double precinctSize = 1 << pp;
      return (int) (Math.ceil((tr1 - z) / precinctSize) - Math.floor((tr0 - z) / precinctSize));
    }

    throw new JPEG2000Exception(ValidationMessages.ILLEGAL_RESOLUTION_COORDINATES, tr0, tr1);
  }

  public static Region mapPrecinct(Region precinctRegion, Pair anchor, Pair quantity) {
    final int pbx0 = mapPrecinct(precinctRegion.pos.x, quantity.x, anchor.x);
    final int pby0 = mapPrecinct(precinctRegion.pos.y, quantity.y, anchor.y);
    final int pbx1 = mapPrecinct(precinctRegion.pos.x + precinctRegion.size.x, quantity.x, anchor.x);
    final int pby1 = mapPrecinct(precinctRegion.pos.y + precinctRegion.size.y, quantity.y, anchor.y);
    return new Region(pbx0, pby0, pbx1 - pbx0, pby1 - pby0);
  }

  private static int mapPrecinct(int p, int o, int z) {
    return (int) (Math.ceil((p - o) / 2) + (1 - o) * z);
  }

  /**
   * Function implementing the equation defined by <i>ITU-T.800, (B-6)</i>. It converts the absolute tile index into
   * a horizontal and vertical part. These indices describe the horizontal and vertical position of a tile and can also
   * be used as factors for the replication in combination with the tile-partition.
   *
   * @param t the absolute tile index (not divided into dimensional parts).
   * @param numTilesHorizontal the amount of tiles in horizontal direction.
   *
   * @return a new {@link Pair}-object containing the horizontal and vertical replication factors of the requested tile
   * index.
   */
  public static Pair tileIndices(int t, int numTilesHorizontal) {
    return new Pair(t % numTilesHorizontal, t / numTilesHorizontal);
  }

  /**
   * Computes the {@link Region} of a particular tile on the canvas as described by the following equations:
   * <ol>
   * <li><i>ITU-T.800, (B-7): <code>tx<sub>0</sub>(p,q) = max(XTOsiz+p⋅XTsiz, XOsiz)<code></li>
   * <li><i>ITU-T.800, (B-8): <code>ty<sub>0</sub>(p,q) = max(YTOsiz+q⋅YTsiz, YOsiz)<code></li>
   * <li><i>ITU-T.800, (B-9): <code>tx<sub>1</sub>(p,q) = min(XTOsiz+(p+1)⋅XTsiz, Xsiz)<code></li>
   * <li><i>ITU-T.800, (B-10): <code>ty<sub>1</sub>(p,q) = min(YTOsiz+(q+1)⋅YTsiz, Ysiz)<code></li>
   * </ol>
   * where <code>tx<sub>0</sub>(p,q)</code> and <code>ty<sub>0</sub>(p,q)</code> are the coordinates of the upper
   * left corner of the tile, <code>tx<sub>1</sub>(p,q)–1</code> and <code>ty<sub>1</sub>(p,q)–1</code> are the
   * coordinates of the lower right corner of the tile. The dimensions (width and height) of a tile in the reference
   * grid are <code>(tx<sub>1</sub>−tx<sub>0</sub>, ty<sub>1</sub>−ty<sub>0</sub>)</code> as described by equation
   * <i>ITU-T.800, (B-11)</i>.
   *
   * @param canvas The region describing the high resolution reference grid.
   * @param tilePartition The region describing the upper left tile's region. In particular the region of
   * tile<sub>0,0</sub>.
   * @param p Horizontal index of a tile, ranging from <code>0</code> to <code>numXtiles–1</code>.
   * @param q Vertical index of a tile, ranging from <code>0</code> to <code>numYtiles–1</code>.
   *
   * @return A new {@link Region} object describing the position and dimension of tile<sub>p,q</sub>.
   */
  public static Region tileRegion(Region canvas, Region tilePartition, int p, int q) {
    final int tx0 = Math.max(tilePartition.pos.x + p * tilePartition.size.x, canvas.pos.x);
    final int ty0 = Math.max(tilePartition.pos.y + q * tilePartition.size.y, canvas.pos.y);
    final int tx1 = Math.min(tx0 + tilePartition.size.x, canvas.pos.x + canvas.size.x);
    final int ty1 = Math.min(ty0 + tilePartition.size.y, canvas.pos.y + canvas.size.y);
    return new Region(tx0, ty0, tx1 - tx0, ty1 - ty0);
  }

  /**
   * Computes the {@link Region} of a particular tile-component on the canvas.
   * <p>
   * Within the domain of an image component, the coordinates of the upper left hand sample are given by
   * <code>(tcx<sub>0</sub>,tcy<sub>0</sub>)</code> and the coordinates of the lower right hand sample are given by
   * <code>(tcx<sub>1</sub>–1,tcy<sub>1</sub>–1)</code>. These values are computed as defined by equation <i>ITU-T.800,
   * (B-12)</i>:
   * <ul>
   * <li><code>tcx<sub>0</sub> = ceil(tx<sub>0</sub>/XRsiz<sup>c</sup>)</code></li>
   * <li><code>tcy<sub>0</sub> = ceil(ty<sub>0</sub>/YRsiz<sup>c</sup>)</code></li>
   * <li><code>tcx<sub>1</sub> = ceil(tx<sub>1</sub>/XRsiz<sup>c</sup>)</code></li>
   * <li><code>tcy<sub>1</sub> = ceil(ty<sub>1</sub>/YRsiz<sup>c</sup>)</code></li>
   * </ul>
   * where <code>c</code> is the index of the component.
   * <p>
   * Thus the size (width and height) of a tile-component are <code>(tcx<sub>1</sub>-tcx<sub>0</sub>,
   * tcy<sub>1</sub>-tcy<sub>0</sub>)</code> as described by equation <i>ITU-T.800, (B-13)</i>.
   *
   * @param tileRegion  Designates the region of a tile for which the tile-component region is requested.
   * @param subsampling Subsampling factors to be applied while computing the tile-component's region.
   * @return a new {@link Region} object describing the position and size of a tile-component.
   */
  public static Region tileComponentRegion(Region tileRegion, Pair subsampling) {
    final float x = (float) subsampling.x;
    final float y = (float) subsampling.y;
    final int tcx0 = (int) Math.ceil(tileRegion.x0() / x);
    final int tcy0 = (int) Math.ceil(tileRegion.y0() / y);
    final int tcx1 = (int) Math.ceil(tileRegion.x1() / x);
    final int tcy1 = (int) Math.ceil(tileRegion.y1() / y);
    return new Region(tcx0, tcy0, tcx1 - tcx0, tcy1 - tcy0);
  }

  /**
   * Computes the {@link Region} of a particular resolution on the canvas in a specific tile-component domain.
   * <p>
   * The given tile-component's coordinates with respect to the reference grid (canvas) at a particular resolution
   * level, {@code r}, yield upper left hand sample coordinates, <code>(trx<sub>0</sub>, try<sub>0</sub>)</code> and
   * lower right hand sample coordinates, <code>(trx<sub>1</sub> – 1, try<sub>1</sub> – 1)</code>, where the following
   * equations apply.
   * <ol>
   * <li><code>trx<sub>0</sub> = ceil(tcx<sub>0</sub>/2<sup><i>NL</i>-r</sup>)</code></li>
   * <li><code>try<sub>0</sub> = ceil(tcy<sub>0</sub>/2<sup><i>NL</i>-r</sup>)</code></li>
   * <li><code>trx<sub>1</sub> = ceil(tcx<sub>1</sub>/2<sup><i>NL</i>-r</sup>)</code></li>
   * <li><code>try<sub>1</sub> = ceil(tcy<sub>1</sub>/2<sup><i>NL</i>-r</sup>)</code></li>
   * </ol>
   *
   * @param tileCompRegion The absolute region of a specific tile-component on the canvas.
   * @param dwtLevels The total amount of DWT levels (<code>NL</code>) of the tile-component.
   * @param resLevel The resolution level (<code>r</code>) of the requested resolution region.
   *
   * @return A new {@link Region} object describing the position and size of a particular resolution.
   */
  public static Region resolutionRegion(Region tileCompRegion, int dwtLevels, int resLevel) {
    return new Region(
        resolutionCoordinate(tileCompRegion.pos.x, resLevel, dwtLevels),
        resolutionCoordinate(tileCompRegion.pos.y, resLevel, dwtLevels),
        resolutionCoordinate(tileCompRegion.size.x, resLevel, dwtLevels),
        resolutionCoordinate(tileCompRegion.size.y, resLevel, dwtLevels)
    );
  }

  public static Region subbandPartition(Region resolutionRegion, int resLevel) {
    final int x = resolutionRegion.pos.x;
    final int y = resolutionRegion.pos.y;
    
    final int width = resolutionRegion.size.x;
    final int height = resolutionRegion.size.y;

    if (resLevel == 0) {
      return new Region(x, y, width, height);
    } else {
      return new Region(x, y, (int) Math.ceil(width / 2f), (int) Math.ceil(height / 2f));
    }
  }

  public static Region inducePrecinctPartition(Region region, SubbandType type) {
    final int x0 = inducePrecinctPartitionCoordinate(region.x0(), type.branchX);
    final int x1 = inducePrecinctPartitionCoordinate(region.x1(), type.branchX);
    final int y0 = inducePrecinctPartitionCoordinate(region.y0(), type.branchY);
    final int y1 = inducePrecinctPartitionCoordinate(region.y1(), type.branchY);
    return new Region(x0, y0, x1 - x0, y1 - y0);
  }

  private static int inducePrecinctPartitionCoordinate(int c, int b) {
    return (int) Math.ceil((c - b) / 2d);
  }

  public static void checkCodingPartition(Region partition) {
    if (partition.pos.x != (partition.pos.x & 1) || partition.pos.y != (partition.pos.y & 1)) {
      final QualifiedLogger LOG = LoggerFactory.getQualifiedLogger(Canvas.class);
      LOG.error(ValidationMessages.ILLEGAL_CODING_PARTITION_ORIGINS, partition);
    }

    if (!isPower2(partition.size.x) || !isPower2(partition.size.y)) {
      final QualifiedLogger LOG = LoggerFactory.getQualifiedLogger(Canvas.class);
      LOG.error(ValidationMessages.ILLEGAL_CODING_PARTITION_DIMENSIONS, partition);
    }
  }

  public static Pair numElements(Region region, Region partition) {
    if (region.isEmpty()) {
      return new Pair(0, 0);
    }

    final int numElementsX = numElements(region.x0(), region.x1(), partition.x0(), partition.size.x);
    final int numElementsY = numElements(region.y0(), region.y1(), partition.y0(), partition.size.y);
    return new Pair(numElementsX, numElementsY);
  }

  private static int numElements(int start, int end, int origin, double size) {
    return (int) (Math.ceil((end - origin) / size) - Math.floor((start - origin) / size));
  }

  /**
   * Determines if a given integer has an even or odd number of samples.
   * 
   * @param number the integer to examine.
   * @return {@code true} if given integer is even or {@code false} if it is odd.
   */
  public static boolean isEven(int number) {
    return (number & 0x1) == 0;
  }
}
