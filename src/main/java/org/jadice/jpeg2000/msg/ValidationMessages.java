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
package org.jadice.jpeg2000.msg;

import org.jadice.util.log.message.LogMessage;
import org.jadice.util.log.qualified.MessageID;

import com.levigo.jadice.document.ProductInformation;

public enum ValidationMessages implements MessageID {

  @LogMessage("Value {0} for {1} is not in range [{2},{3}].")
  ILLEGAL_RANGE,

  @LogMessage("Parameter {0} must not be null")
  ILLEGAL_NULL_VALUE,

  @LogMessage("Canvas width or height may not be smaller than 1, but was {0}.")
  ILLEGAL_CANVAS_SIZE,

  @LogMessage("Maximum allowed number of tiles is 65535 for any JPEG2000 codestream. Value {0} exceeds the limit.")
  ILLEGAL_TILE_COUNT,

  @LogMessage("Subsampling factors must be greater or equal to 1. Actual x={0}, y={1}.")
  ILLEGAL_SUBSAMPLING_FACTORS,

  @LogMessage("Illegal values for tr0={0} and/or tr1={1}. tr1 has to be greater than or equal to tr0.")
  ILLEGAL_RESOLUTION_COORDINATES,

  @LogMessage("Illegal value for {0}: {1}. Allowed value: {2}.")
  ILLEGAL_VALUE,

  @LogMessage("Coding partitions {0} (code-blocks and precinct partitions) must have origin coordinates equal to 1 or 0 only!")
  ILLEGAL_CODING_PARTITION_ORIGINS,

  @LogMessage("Coding partitions {0} (code-block and precinct partitions) must have exact power-of-2 dimensions!")
  ILLEGAL_CODING_PARTITION_DIMENSIONS,

  @LogMessage("Profile-0 violation detected (codestream is technically illegal).")
  PROFILE0_VIOLATION,

  @LogMessage("Profile-1 violation detected (codestream is technically illegal).")
  PROFILE1_VIOLATION,

  @LogMessage("Profile-0 codestreams must either be untiled or the tile dimensions must be exactly 128x128.")
  PROFILE0_VIOLATION_TILE_SIZE,

  @LogMessage("Profile-0 codestreams must have image and tiling origins (anchor points) set to zero.)")
  PROFILE0_VIOLATION_OFFSET,

  @LogMessage("Component sub-sampling factors for Profile-0 code-streams are restricted to the values 1, 2 and 4.")
  PROFILE0_VIOLATION_SUBSAMPLING,

  @LogMessage("Profile-1 codestreams must either be untiled or the horizontal and vertical tile dimensions must be identical (square tiles on the hi-res canvas).")
  PROFILE1_VIOLATION_TILE_SIZE,

  @LogMessage("If a Profile-1 codestream has multiple tiles, the width and height of its tiles, projected onto any given image component, may not exceed 1024.")
  PROFILE1_VIOLATION_COMPONENT_SIZE,

  @LogMessage("PPM marker segments may not appear within a Profile-0 codestream.")
  PROFILE0_VIOLATION_PPM_DISALLOWED,

  @LogMessage("Illegal value for CEpoc[{0}].")
  ILLEGAL_POC_ENTRY_VALUE_FOR_LAST_COMPONENT,

  @LogMessage("Tile-component-resolution encountered in the codestream contains way too many precincts.")
  ILLEGAL_NUMBER_OF_PRECINCT,

  @LogMessage("Illegal number of components specified.")
  ILLEGAL_NUMBER_OF_COMPS;

  private static final String COMPONENT_ID = ProductInformation.getProductId() + ".FORMAT.JPEG2000.VALIDATION";

  @Override
  public String getComponentID() {
    return COMPONENT_ID;
  }

}
