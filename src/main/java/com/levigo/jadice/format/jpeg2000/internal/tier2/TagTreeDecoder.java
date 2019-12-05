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
package com.levigo.jadice.format.jpeg2000.internal.tier2;

import java.io.IOException;
import java.util.Arrays;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.format.jpeg2000.internal.CorruptBitstuffingException;
import com.levigo.jadice.format.jpeg2000.internal.io.PacketHeaderInput;

/**
 * This class provides tag tree decoding support. A tag tree codes a 2D matrix of integer elements in an efficient way.
 * The decoding procedure {@link #update(PacketHeaderInput, int, int, int)} updates a value of the matrix from a stream
 * of coded data, given a threshold. This procedure decodes enough information to identify whether or not the value is
 * greater than or equal to the threshold, and updates the value accordingly.
 * <p>
 * In general the decoding procedure must follow the same sequence of elements and thresholds as the encoded one.
 * <p>
 * Tag trees that have one dimension, or both, as 0 are allowed for convenience. Of course no values can be set or
 * coded in such cases.
 */
@Refer(to = Spec.J2K_CORE, page = 54, section = "B.10.2", called = "Tag Trees")
public class TagTreeDecoder {

  /** The horizontal dimension of the base level */
  public int width;

  /** The vertical dimensions of the base level */
  public int height;

  /** The number of levels in the tag tree */
  protected int levels;

  /**
   * The tag tree values. The first index is the level, starting at level 0 (leafs). The second index is the element
   * within the level, in lexicographical order.
   */
  protected int values[][];

  /**
   * The tag tree state. The first index is the level, starting at level 0 (leafs). The second index is the element
   * within the level, in lexicographical order.
   */
  protected int states[][];

  public int state;

  public int value;

  /**
   * Creates a tag tree decoder with {@link #width} elements along the horizontal dimension and {@link #height}
   * elements along the vertical direction. The total number of elements is thus 'vdim' x 'hdim'.
   * <p>
   * The values of all elements are initialized to {@link Integer#MAX_VALUE} (i.e. no information decoded so far). The
   * states are initialized all to 0.
   *
   * @param height The number of elements along the vertical direction.
   * @param width The number of elements along the horizontal direction.
   */
  public TagTreeDecoder(int width, int height) {
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Width and height shouldn't be '< 0', but was width=" + width + ", height="
          + height);
    }

    // Initialize dimensions
    this.width = width;
    this.height = height;

    this.levels = getLevels();

    // Allocate tree values and codes
    allocate();
  }

  private int getLevels() {
    if (width == 0 || height == 0) {
      return 0; // Empty tree
    } else {
      int levels = 1;
      int w = width;
      int h = height;
      while (h != 1 || w != 1) { // Loop until we reach root
        w = (w + 1) >> 1;
        h = (h + 1) >> 1;
        levels++;
      }
      return levels;
    }
  }

  private void allocate() {
    values = new int[levels][];
    states = new int[levels][];

    int w = width;
    int h = height;
    for (int i = 0; i < levels; i++) {
      int levelSize = w * h;
      values[i] = new int[levelSize];

      // Initialize to infinite value
      Arrays.fill(values[i], Integer.MAX_VALUE);

      // (no need to initialize to 0 since it's the default)
      states[i] = new int[levelSize];
      w = (w + 1) >> 1;
      h = (h + 1) >> 1;
    }
  }

  /**
   * Decodes information for the specified element of the tree, given the threshold, and updates its value. The
   * information that can be decoded is whether or not the value of the element is greater than, or equal to, the value
   * of the threshold.
   *
   * @param input the source on which the update can request read operations
   * @param x The horizontal index of the element.
   * @param y The vertical index of the element.
   * @param threshold The threshold to use in decoding. It must be non-negative.
   *
   * @return The updated value at index [y][x].
   *
   * @throws IOException If an I/O error occurs while reading.
   */
  public int update(PacketHeaderInput input, int x, int y, int threshold)
      throws IOException, CorruptBitstuffingException {

    // Sanity checks for arguments
    if (y >= height || x >= width || threshold < 0) {
      throw new IllegalArgumentException();
    }

    // Initialize
    int level = levels - 1;
    int minTreshold = states[level][0];

    // Loop on levels
    int idx = (y >> level) * ((width + (1 << level) - 1) >> level) + (x >> level);

    while (true) {
      // Cache state and value
      state = states[level][idx];
      value = values[level][idx];

      if (state < minTreshold) {
        state = minTreshold;
      }

      while (threshold > state) {
        if (value >= state) {
          // We are not done yet

          if (input.readBit() == 0) { // '0' bit
            // We know that the value is greater than 'states[level][idx]'
            state++;
          } else { // '1' bit
            // We know that the value is equal to 'states[level][idx]'
            value = state++;
          }
        } else {
          // We are done, we can set cached state and get out
          state = threshold;
          break;
        }
      }

      // Update state and value
      states[level][idx] = state;
      values[level][idx] = value;

      // Update minimum or terminate
      if (level > 0) {
        minTreshold = state < value ? state : value;
        level--;
        // Index of element for next iteration
        idx = (y >> level) * ((width + (1 << level) - 1) >> level) + (x >> level);
      } else {
        // Return the updated value
        return value;
      }
    }
  }

  /**
   * Returns the current value of the specified element in the tag tree. This is the value as last updated by {@link
   * #update(PacketHeaderInput, int, int, int)}.
   *
   * @param verticalIndex   The vertical index of the element.
   * @param horizontalIndex The horizontal index of the element.
   * @return The current value of the element.
   * @see #update(PacketHeaderInput, int, int, int)
   */
  public int getValue(int verticalIndex, int horizontalIndex) {
    // Check arguments
    if (verticalIndex >= height) {
      throw new IllegalArgumentException("vertical index must not be greater than or equal to internal height");
    }

    if (horizontalIndex >= width) {
      throw new IllegalArgumentException("horizontal index must not be greater than or equal to internal width");
    }

    return values[0][verticalIndex * width + horizontalIndex];
  }
}