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

import java.awt.Rectangle;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.msg.ValidationMessages;

public class Regions {

  /**
   * Creates a copy of the given region.
   *
   * @param region the region which should be represented by new region.
   */
  public static Region copyOf(Region region) {
    return new Region(region.pos.x, region.pos.y, region.size.x, region.size.y);
  }

  /**
   * Creates a region based on the given rectangle.
   *
   * @param rectangle the rectangle which should be represented by new region.
   */
  public static Region createFromRectangle(Rectangle rectangle) {
    return new Region(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
  }

  /**
   * Creates a new {@link Region}-instance based on the given region with applied subsampling.
   *
   * @param region      the region to subsample
   * @param subsampling the subsampling factors
   * @return a new instance of {@link Region} with subsampled values
   * @throws JPEG2000Exception if at least one of the subsampling factors were lower than <code>1</code>.
   */
  public static Region createSubsampled(Region region, Pair subsampling) throws JPEG2000Exception {
    if (subsampling.x == 1 && subsampling.y == 1) {
      return copyOf(region);
    }

    if (subsampling.x < 1 || subsampling.y < 1) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_SUBSAMPLING_FACTORS, subsampling.x, subsampling.y);
    }

    return new Region(
        region.pos.x / subsampling.x, region.pos.y / subsampling.y,
        region.size.x / subsampling.x, region.size.y / subsampling.y
    );
  }

  /**
   * Special subsampling method which is equivalent to a call of {@link #createSubsampled(Region, Pair)} with 
   * subsampling of two in horizontal and vertical dimension.
   * 
   * @param region
   * @return a new instance of {@link Region} with halfsized values.
   * @throws JPEG2000Exception
   */
  public static Region createHalfSized(Region region) throws JPEG2000Exception {
    return new Region(
        (int) Math.floor(region.pos.x / 2f), (int) Math.floor(region.pos.y / 2f),
        (int) Math.ceil(region.size.x / 2f), (int) Math.ceil(region.size.y / 2f)
    );
  }

  /**
   * Computes the first indexes of a replicated partition region that has an intersection with the parent region. The
   * identified child might not be fully covered by the parent region. Each intersection is marked as included.
   * <p>
   * This method is useful to determine precinct and code-block indexes which are contributing to their parent region.
   * The given partition region depicts the so called 'key cell'. This key cell is always starting at the top- and
   * left-most corner. <i>ITU-T.800</i> specifies, that precincts and code-blocks always start at the origin <code>
   * (0,0)</code>. Other origins might be defined in later standards.
   * <p>
   * <b>Note:</b> Currently, this implementation does not support displacement of the origin.
   *
   * @param region the region for which the first indexes of replication should be computed.
   * @param partition the 'key cell' depicting the first (top- and left-most) region.
   *
   * @return a new instance of {@link Pair} containing the indexes in horizontal and vertical direction of the first
   * contributing region.
   */
  public static Pair getFirstChildIdx(Region region, Region partition) {
    return new Pair(
        region.pos.x / partition.x1(),
        region.pos.y / partition.y1()
    );
  }

  /**
   * Computes the last indexes of a replicated partition region that has an intersection with the parent region. The
   * identified child might not be fully covered by the parent region. Each intersection is marked as included.
   * <p>
   * This method is useful to determine precinct and code-block indexes contributing to their parent region. The given
   * partition region depicts the so called 'key cell'. This key cell is always starting at the top- and left-most
   * corner. <i>ITU-T.800</i> specifies, that precincts and code-blocks always start at the origin <code> (0,0)</code>.
   * Other origins might be defined in later standards.
   * <p>
   * <b>Note:</b> Currently, this implementation does not support displacement of the origin.
   *
   * @param parent    the region for which the first indexes of replication should be computed.
   * @param partition the 'key cell' depicting the first (top- and left-most) region.
   * @return a new instance of {@link Pair} containing the indexes in horizontal and vertical direction of the last
   * contributing region.
   */
  public static Pair getLastChildIdx(Region parent, Region partition) {
    return new Pair(
        (parent.pos.x + parent.size.x - 1) / partition.size.x,
        (parent.pos.y + parent.size.y - 1) / partition.size.y
    );
  }

  /**
   * Creates a new {@link Region} by replicating the given key cell ('partition') <code>x</code>-times in horizontal
   * and <code>y</code>-times in vertical direction.
   *
   * @param partition the 'key cell' depicting the top- and left-most region.
   * @param x         the index of requested cell replicated in horizontal direction.
   * @param y         the index of requested cell replicated in vertical direction
   * @return a new {@link Region} which replicates the basic region by the given factors.
   */
  public static Region replicate(Region partition, int x, int y) {
    return replicate(partition, x, y, null);
  }

  /**
   * Creates a new {@link Region} by replicating the given key cell ('partition') <code>x</code>-times in horizontal
   * and <code>y</code>-times in vertical direction. If the parameter space is given, the region created is bounded by
   * it.
   *
   * @param partition the 'key cell' depicting the top- and left-most region.
   * @param x the index of requested cell replicated in horizontal direction.
   * @param y the index of requested cell replicated in vertical direction
   * @param space used to map and bound (clamp) a replicated {@link Region}. May be <code>null</code>.
   *
   * @return a new {@link Region} which replicates the basic region by the given factors.
   */
  public static Region replicate(Region partition, int x, int y, Region space) {
    if (space != null) {
      final Pair firstChildIndices = getFirstChildIdx(space, partition);
      x += firstChildIndices.x;
      y += firstChildIndices.y;
    }

    final Region region = new Region(
        partition.pos.x + (partition.size.x * x),
        partition.pos.y + (partition.size.y * y),
        partition.size.x, partition.size.y);

    if (space != null) {
      clamp(space, region);
    }

    return region;
  }

  public static boolean equalAreas(Region region, Rectangle rectangle) {
    return equalAreas(region, createFromRectangle(rectangle));
  }

  public static boolean equalAreas(Region r0, Region r1) {
    return r0.pos.x == r1.pos.x && r0.pos.y == r1.pos.y
        && r0.size.x == r1.size.x && r0.size.y == r1.size.y;
  }

  public static boolean intersect(Region r0, Region r1) {
    final int r0w = r0.pos.x + r0.size.x;
    final int r0h = r0.pos.y + r0.size.y;
    final int r1w = r1.pos.x + r1.size.x;
    final int r1h = r1.pos.y + r1.size.y;

    // @formatter:off
    return ( //   overflow || intersect
           (r1w < r1.pos.x || r1w > r0.pos.x)
        && (r1h < r1.pos.y || r1h > r0.pos.y)
        && (r0w < r0.pos.x || r0w > r1.pos.x)
        && (r0h < r0.pos.y || r0h > r1.pos.y)
    );
    // @formatter:on
  }

  public static void clamp(Region limit, Region toLimit) {
    final int x0 = Math.max(limit.x0(), toLimit.x0());
    final int y0 = Math.max(limit.y0(), toLimit.y0());
    toLimit.size.x = Math.min(limit.x1(), toLimit.x1()) - x0;
    toLimit.size.y = Math.min(limit.y1(), toLimit.y1()) - y0;
    toLimit.pos.x = x0;
    toLimit.pos.y = y0;
  }

  public static Rectangle asRectangle(Region region) {
    return new Rectangle(region.pos.x, region.pos.y, region.size.x, region.size.y);
  }
  
  public static Region dispose(Region region, Pair disposition) {
    final Region disposedRegion = new Region();
    disposedRegion.pos = region.pos.minus(disposition);
    disposedRegion.size = region.size.clone();
    return disposedRegion;
  }
  
}
