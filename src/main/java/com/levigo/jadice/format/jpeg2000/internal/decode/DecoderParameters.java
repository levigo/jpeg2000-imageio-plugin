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
package com.levigo.jadice.format.jpeg2000.internal.decode;

import com.levigo.jadice.format.jpeg2000.internal.image.Region;

public class DecoderParameters {

  /**
   * Flag indicating if codestream validation should be performed.
   */
  public boolean validate;

  /**
   * Depicts the region of interest.
   */
  public Region region;

  public DecoderParameters() {
    validate = false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DecoderParameters that = (DecoderParameters) o;

    if (validate != that.validate)
      return false;
    return !(region != null ? !region.equals(that.region) : that.region != null);

  }
  @Override
  public int hashCode() {
    int result = (validate ? 1 : 0);
    result = 31 * result + (region != null ? region.hashCode() : 0);
    return result;
  }
}
