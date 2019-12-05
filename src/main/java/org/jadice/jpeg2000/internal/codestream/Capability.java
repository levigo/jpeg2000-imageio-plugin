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
package org.jadice.jpeg2000.internal.codestream;

import java.util.Collection;
import java.util.HashSet;

import org.jadice.jpeg2000.internal.marker.SIZ;
import org.jadice.jpeg2000.internal.param.Parameters;

public enum Capability {

  T800_FULL(SIZ.VALUE_RSIZ_T800_FULL),
  T800_PROFILE_0(SIZ.VALUE_RSIZ_PROFILE_0),
  T800_PROFILE_1(SIZ.VALUE_RSIZ_PROFILE_1),
  //
  T801_BASED(SIZ.MASK_RSIZ_T801, SIZ.VALUE_RSIZ_T801),
  T801_VARIABLE_DC_OFFSET(SIZ.MASK_RSIZ_VARIABLE_DC_OFFSET),
  T801_VARIABLE_SCALAR_QUANTIZATION(SIZ.MASK_RSIZ_VARIABLE_SCALAR_QUANTIZATION),
  T801_TRELLIS_CODED_QUANTIZATION(SIZ.MASK_RSIZ_TRELLIS_CODED_QUANTIZATION),
  T801_VISUAL_MASKING(SIZ.MASK_RSIZ_VISUAL_MASKING),
  T801_SINGLE_SAMPLE_OVERLAP(SIZ.MASK_RSIZ_SINGLE_SAMPLE_OVERLAP),
  T801_ARBITRARY_DECOMPOSITION(SIZ.MASK_RSIZ_ARBITRARY_DECOMPOSITION_STYLE),
  T801_ARBITRARY_TRANSFORMATION(SIZ.MASK_RSIZ_ARBITRARY_TRANSFORMATION_KERNEL),
  T801_WHOLE_SAMPLE_SYMMETRIC_TRANSFORMATION(SIZ.MASK_RSIZ_WHOLE_SAMPLE_SYMMETRIC_TRANSFORMATION_KERNEL),
  T801_MULTIPLE_COMPONENT_TRANSFORMATION(SIZ.MASK_RSIZ_MULTIPLE_COMPONENT_TRANSFORMATION),
  T801_NONLINEAR_POINT_TRANSFORMATION(SIZ.MASK_RSIZ_NONLINEAR_POINT_TRANSFORMATION),
  T801_ARBITRARY_SHAPED_ROI(SIZ.MASK_RSIZ_ARBITRARY_SHAPED_ROI),
  T801_PRECINCT_DEPENDENT_QUANTIZATION(SIZ.MASK_RSIZ_PRECINCT_DEPENDENT_QUANTIZATION);

  private final int mask;
  private final int value;

  private Capability(int value) {
    this(value, value);
  }

  private Capability(int mask, int value) {
    this.mask = mask;
    this.value = value;
  }

  public boolean isUsedBy(Codestream codestream) {
    return Parameters.isValue(codestream.capabilities, mask, value);
  }

  public void set(Codestream codestream) {
    codestream.capabilities |= value;
  }

  public static Collection<Capability> fromRsiz(int Rsiz) {
    final Collection<Capability> capabilities = new HashSet<>();

    if (Parameters.isSet(Rsiz, SIZ.VALUE_RSIZ_T800_FULL)) {
      capabilities.add(Capability.T800_FULL);

    } else if (Parameters.isSet(Rsiz, SIZ.VALUE_RSIZ_PROFILE_0)) {
      capabilities.add(Capability.T800_PROFILE_0);

    } else if (Parameters.isSet(Rsiz, SIZ.VALUE_RSIZ_PROFILE_1)) {
      capabilities.add(Capability.T800_PROFILE_1);

    } else if (Parameters.isValue(Rsiz, SIZ.MASK_RSIZ_T801, SIZ.VALUE_RSIZ_T801)) {

      capabilities.add(Capability.T801_BASED);

      if (Parameters.isValue(Rsiz, SIZ.MASK_RSIZ_VARIABLE_DC_OFFSET,
          SIZ.VALUE_RSIZ_VARIABLE_DC_OFFSET)) {
        capabilities.add(Capability.T801_VARIABLE_DC_OFFSET);
      }

      if (Parameters.isSet(Rsiz, SIZ.MASK_RSIZ_VARIABLE_SCALAR_QUANTIZATION)) {
        capabilities.add(Capability.T801_VARIABLE_SCALAR_QUANTIZATION);
      }

      if (Parameters.isSet(Rsiz, SIZ.MASK_RSIZ_TRELLIS_CODED_QUANTIZATION)) {
        capabilities.add(Capability.T801_TRELLIS_CODED_QUANTIZATION);
      }

      if (Parameters.isSet(Rsiz, SIZ.MASK_RSIZ_VISUAL_MASKING)) {
        capabilities.add(Capability.T801_VISUAL_MASKING);
      }

      if (Parameters.isSet(Rsiz, SIZ.MASK_RSIZ_SINGLE_SAMPLE_OVERLAP)) {
        capabilities.add(Capability.T801_SINGLE_SAMPLE_OVERLAP);
      }

      if (Parameters.isSet(Rsiz, SIZ.MASK_RSIZ_ARBITRARY_DECOMPOSITION_STYLE)) {
        capabilities.add(Capability.T801_ARBITRARY_DECOMPOSITION);
      }

      if (Parameters.isSet(Rsiz, SIZ.MASK_RSIZ_ARBITRARY_TRANSFORMATION_KERNEL)) {
        capabilities.add(Capability.T801_ARBITRARY_TRANSFORMATION);
      }

      if (Parameters.isSet(Rsiz, SIZ.MASK_RSIZ_WHOLE_SAMPLE_SYMMETRIC_TRANSFORMATION_KERNEL)) {
        capabilities.add(Capability.T801_WHOLE_SAMPLE_SYMMETRIC_TRANSFORMATION);
      }

      if (Parameters.isSet(Rsiz, SIZ.MASK_RSIZ_MULTIPLE_COMPONENT_TRANSFORMATION)) {
        capabilities.add(Capability.T801_MULTIPLE_COMPONENT_TRANSFORMATION);
      }

      if (Parameters.isSet(Rsiz, SIZ.MASK_RSIZ_NONLINEAR_POINT_TRANSFORMATION)) {
        capabilities.add(Capability.T801_NONLINEAR_POINT_TRANSFORMATION);
      }

      if (Parameters.isSet(Rsiz, SIZ.MASK_RSIZ_ARBITRARY_SHAPED_ROI)) {
        capabilities.add(Capability.T801_ARBITRARY_SHAPED_ROI);
      }

      if (Parameters.isSet(Rsiz, SIZ.MASK_RSIZ_PRECINCT_DEPENDENT_QUANTIZATION)) {
        capabilities.add(Capability.T801_PRECINCT_DEPENDENT_QUANTIZATION);
      }
    }

    return capabilities;
  }
}
