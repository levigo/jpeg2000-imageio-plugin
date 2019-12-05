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

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.marker.COx;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class CanvasNumPrecinctsTest {

  private final int expected;
  private final int tr0;
  private final int tr1;
  private final int z;
  private final int pp;

  @Parameters(name = "{index}: tr0={1}, tr1={2}, z={3}, PP={4} --> num={0}")
  public static List<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {1, 0, 128, 0, COx.PP_DEFAULT},
        {1, 0, 128, 0, 8},
        {2, 0, 512, 0, 8},
        {3, 0, 768, 0, 8},
        {4, 0, 1024, 0, 8},
        {1, 0, 256, 0, 8}, // Exact dimensions
        {2, 0, 300, 0, 8}, // Rounding
        {0, 128, 128, 0, COx.PP_DEFAULT},  // tr0 = tr1
        {0, 1024, 1024, 0, COx.PP_DEFAULT},  // tr0 = tr1
        {2, 0, 33000, 0, COx.PP_DEFAULT}, // Possible but improbable
    });
  }

  public CanvasNumPrecinctsTest(final int expected, final int tr0, final int tr1, final int z, final int pp) {
    this.expected = expected;
    this.tr0 = tr0;
    this.tr1 = tr1;
    this.z = z;
    this.pp = pp;
  }

  @Test
  public void numPrecincts() throws JPEG2000Exception {
    assertEquals(expected, Canvas.numPrecinctsX(tr0, tr1, z, pp));
    assertEquals(expected, Canvas.numPrecinctsY(tr0, tr1, z, pp));
  }

}
