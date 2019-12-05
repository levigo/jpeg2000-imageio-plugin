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

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.image.Pair;
import org.jadice.jpeg2000.internal.image.Region;
import org.jadice.jpeg2000.internal.image.Regions;
import org.jadice.jpeg2000.msg.ValidationMessages;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class RegionsSubsamplingTest {

  private final Region region;
  private final Pair subsampling;
  private final Object expected;

  @Parameterized.Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {new Region(100, 100), new Pair(1, 1), new Region(100, 100)},
        {new Region(100, 100), new Pair(2, 3), new Region(0, 0, 50, 33)},
        {new Region(10, 10), new Pair(4, 5), new Region(0, 0, 2, 2)},
        {new Region(10, 10, 10, 10), new Pair(2, 2), new Region(5, 5, 5, 5)},
        {new Region(10, 10, 10, 10), new Pair(-1, -1),
            new JPEG2000Exception(ValidationMessages.ILLEGAL_SUBSAMPLING_FACTORS)
        },
        {new Region(10, 10, 10, 10), new Pair(1, -1),
            new JPEG2000Exception(ValidationMessages.ILLEGAL_SUBSAMPLING_FACTORS)
        },
        {new Region(10, 10, 10, 10), new Pair(-1, 1),
            new JPEG2000Exception(ValidationMessages.ILLEGAL_SUBSAMPLING_FACTORS)
        },
        {new Region(10, 10, 10, 10), new Pair(0, 1),
            new JPEG2000Exception(ValidationMessages.ILLEGAL_SUBSAMPLING_FACTORS)
        },
        {new Region(10, 10, 10, 10), new Pair(1, 0),
            new JPEG2000Exception(ValidationMessages.ILLEGAL_SUBSAMPLING_FACTORS)
        },
    });
  }

  public RegionsSubsamplingTest(Region region, Pair subsampling, Object expected) {
    this.region = region;
    this.subsampling = subsampling;
    this.expected = expected;
  }

  @Test
  public void runTest() throws JPEG2000Exception {
    if (expected instanceof Region) {
      Region actual = Regions.createSubsampled(region, subsampling);
      Assert.assertEquals(expected, actual);
    } else if (expected instanceof JPEG2000Exception) {
      JPEG2000Exception expectedException = null;
      try {
        Regions.createSubsampled(region, subsampling);
      } catch (JPEG2000Exception e) {
        expectedException = e;
      }

      Assert.assertNotNull(expectedException);
    }
  }
}
