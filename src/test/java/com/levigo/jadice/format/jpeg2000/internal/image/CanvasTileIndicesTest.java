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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CanvasTileIndicesTest {
  
  @Parameters
  public static Object[][] parameters() {
    return new Object[][]{
        {0, 2, new Pair(0, 0)},
        {2, 2, new Pair(0, 1)}
    };
  }

  @Parameter(0)
  public int t;

  @Parameter(1)
  public int numTilesHorizontal;
  
  @Parameter(2)
  public Pair expected;
  
  @Test
  public void tileIndicesTest() {
    assertEquals(expected, Canvas.tileIndices(t, numTilesHorizontal));
  }
  
}
