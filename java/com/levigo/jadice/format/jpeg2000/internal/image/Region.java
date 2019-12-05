package com.levigo.jadice.format.jpeg2000.internal.image;

import java.awt.Rectangle;

/**
 * Specifies a spatial area in respect of the reference grid (see <i>ITU-T.800, B.2</i>). The values
 * are based on the screen coordinate system with the origin at the upper-left expanding in
 * lower-right direction.
 */
public class Region {

  /**
   * The upper-left point of this region. Since the origin of the reference grid is defined to be
   * <code>(0,0)</code> this point defines the offset relative to the origin. In other words this
   * {@link Pair} specifies the x- and y-coordinate.
   */
  public Pair pos;

  /**
   * The lower-right point of this region relative to the point specified by {@link #pos}. The
   * values specify the width and height.
   */
  public Pair size;

  /**
   * Creates a new {@link Region}-object representing an empty region starting at the origin,
   * <code>(0,0) 0x0</code>.
   */
  public Region() {
    this(0, 0, 0, 0);
  }

  /**
   * Creates a new {@link Region}-object starting at origin <code>(0,0)</code> with the given width
   * and height.
   *
   * @param width the width of the region to create.
   * @param height the height of the region to create.
   */
  public Region(int width, int height) {
    this(0, 0, width, height);
  }

  /**
   * Creates a new {@link Region}-object with the given coordinates as its starting point and
   * dimensions.
   *
   * @param x the horizontal origin.
   * @param y the vertical origin.
   * @param width the horizontal dimension.
   * @param height the vertical dimension.
   */
  public Region(int x, int y, int width, int height) {
    this.pos = new Pair(x, y);
    this.size = new Pair(width, height);
  }

  /**
   * @return <code>true</code> if {@link #size} has a <code>0</code> or negative dimension,
   *         otherwise <code>false</code>.
   */
  public boolean isEmpty() {
    return size.x <= 0 || size.y <= 0;
  }

  public int x0() {
    return pos.x;
  }

  public int y0() {
    return pos.y;
  }

  public int x1() {
    return pos.x + size.x;
  }

  public int y1() {
    return pos.y + size.y;
  }

  public int area() {
    return size.x * size.y;
  }

  public int width() {
    return size.x;
  }

  public int height() {
    return size.y;
  }

  public Rectangle bounds() {
    return new Rectangle(pos.x, pos.y, size.x, size.y);
  }

  public void clampTo(Region limit) {
    final int x0 = Math.max(limit.x0(), x0());
    final int y0 = Math.max(limit.y0(), y0());
    size.x = Math.min(limit.x1(), x1()) - x0;
    size.y = Math.min(limit.y1(), y1()) - y0;
    pos.x = x0;
    pos.y = y0;
  }

  /**
   * Displaces the region by adding the given displacement from position coordinates.
   * 
   * @param displacement 
   */
  public void displaceBy(Pair displacement) {
    pos.add(displacement);
  }

  /**
   * Determines if <b>this</b> region covers the given {@code region} argument. Equal coordinates are defined as
   * covered.
   *
   * @param region The {@link Region} to compare.
   *
   * @return {@code true} if this region fully encloses the given {@code region}; otherwise {@code false}.
   */
  public boolean covers(Region region) {
    return x0() <= region.x0() && y0() <= region.y0() && x1() >= region.x1() && y1() >= region.y1();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Region region = (Region) o;

    if (!pos.equals(region.pos)) {
      return false;
    }
    if (!size.equals(region.size)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = pos.hashCode();
    result = 31 * result + size.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "[p" + pos + " s" + size + "]";
  }
}
