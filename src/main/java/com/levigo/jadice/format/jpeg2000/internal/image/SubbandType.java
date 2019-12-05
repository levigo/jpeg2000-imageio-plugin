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
 * This enumeration defines the four possible types of sub-bands. Each sub-band has a id and a gain
 * number.
 * <p>
 * For details regarding the {@link #id} and {@link #log2gain} number, see <i>Annex E (Quantization)</i>
 * and especially <i>ITU-T.800, Table E.1 – Sub-band gains</i>.
 */
public enum SubbandType {

  /** Defines the type for the vertical and horizontal low-pass sub-band. */
  LL(0, 0, 0, 0),

  /** Defines the type for the horizontal high-pass and vertical low-pass sub-band. */
  HL(1, 1, 1, 0),

  /** Defines the type for the horizontal low-pass and vertical high-pass sub-band. */
  LH(2, 1, 0, 1),

  /** Defines the type for the vertical and horizontal high-pass sub-band. */
  HH(3, 2, 1, 1);

  public final int id;

  /** The log2 of the sub-band gain as specified in Table E.1 */
  public final int log2gain;

  public final int branchX;
  public final int branchY;

  SubbandType(int id, int gain, int x, int y) {
    this.id = id;
    this.log2gain = gain;
    branchX = x;
    branchY = y;
  }
}
