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

/**
 * This class holds codestream-wide information for a single image component.
 */
public class Component {

  /**
   * Identifies the index (starting from 0) of the present codestream image component.
   */
  public int idx;

  /**
   * Component sub-sampling factors in both horizontal <code>x</code> and vertical <code>y</code> directions.
   */
  public Pair subsampling;

  /**
   * Component registration offset in horizontal direction.
   */
  public float crgX;

  /**
   * Component registration offset in vertical direction.
   */
  public float crgY;

  /**
   * Flag defining if original component samples are signed or unsigned.
   */
  public boolean isSigned;

  /**
   * Original component bit-depth.
   */
  public int precision;

  /**
   * Contain information which might be affected by {@link com.levigo.jadice.format.jpeg2000.internal.marker.DFS}
   * marker segments in the codestream's main header. Identifies the total number of horizontal low-pass filtering and
   * downsampling stages which are involved in creating the resolution level which is <code>d</code> levels below the
   * original full image resolution. Neither value may exceed <code>d</code>. Thus, {@link #horizontalDepth}[0] is
   * guaranteed to equal zero. These entries exist only to make the implementation more regular. Codestreams conforming
   * to JPEG2000 Part-1 must have <code>horizontalDepth[d] = verticalDepth[d] = d</code> for all <code>d</code>. Part-2
   * codestreams, however, may have custom downsampling factor styles (represented by {@link
   * com.levigo.jadice.format.jpeg2000.internal.marker.DFS} marker segments in the main header).
   */
  public byte[] horizontalDepth;

  /**
   * Contain information which might be affected by {@link com.levigo.jadice.format.jpeg2000.internal.marker.DFS}
   * marker segments in the codestream's main header. Identifies the total number of vertical low-pass filtering and
   * downsampling stages which are involved in creating the resolution level which is <code>d</code> levels below the
   * original full image resolution. Neither value may exceed <code>d</code>. Thus, {@link #verticalDepth}[0] is
   * guaranteed to equal zero. These entries exist only to make the implementation more regular. Codestreams conforming
   * to JPEG2000 Part-1 must have <code>horizontalDepth[d] = verticalDepth[d] = d</code> for all <code>d</code>. Part-2
   * codestreams, however, may have custom downsampling factor styles (represented by {@link
   * com.levigo.jadice.format.jpeg2000.internal.marker.DFS} marker segments in the main header).
   */
  public byte[] verticalDepth;

  public Component() {
  }

  @Override
  public String toString() {
    return "Component [idx=" + idx + "]";
  }
}
