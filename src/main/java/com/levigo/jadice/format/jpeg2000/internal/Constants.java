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
package com.levigo.jadice.format.jpeg2000.internal;

import com.levigo.jadice.format.jpeg2000.internal.marker.COx;

public class Constants {

  /** Default stripe height, as defined in <i>ITU-T.800, D.1</i>. */
  public static final byte STRIPE_HEIGHT = 4;

  /** Default precinct size as described by <i>ITU-T.800, Table A.13</i>. */
  public static final int DEFAULT_PRECINCT_SIZE = 1 << COx.PP_DEFAULT;

  /** Protects against an amount of precincts which blows memory capacity. */
  public static final long NUM_PRECINCTS_THRESHOLD = 1 << 30;

  /** Default starting bitplane based on 32-bit integer. The most significant bit is reserved for the sign. */
  public static final byte INTEGER_COEFFICIENT_START = 30;

}
