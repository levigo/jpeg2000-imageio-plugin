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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class RegionsEqualAreasTest {

  private final Region a;
  private final Region b;
  private final boolean expected;

  @Parameterized.Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {new Region(100, 100),       new Region(100, 100),           true},
        {new Region(10, 10),         new Region(0, 0, 10, 10),       true},
        {new Region(10, 10, 10, 10), new Region(10, 10, 10, 10),     true},
        {new Region(100, 100),       new Region(100, 100, 100, 100), false},
        {new Region(10, 10, 10, 10), new Region(10, 0, 10, 10),      false},
        {new Region(10, 10, 10, 10), new Region(0, 10, 10, 10),      false},
        {new Region(10, 10, 10, 10), new Region(0, 0, 100, 10),      false},
        {new Region(10, 10, 10, 10), new Region(0, 0, 10, 100),      false}
    });
  }

  public RegionsEqualAreasTest(Region a, Region b, boolean expected) {
    this.a = a;
    this.b = b;
    this.expected = expected;
  }

  @Test
  public void runTest() {
    if (expected) {
      assertTrue(Regions.equalAreas(a, b));
    } else {
      assertFalse(Regions.equalAreas(a, b));
    }
  }
}
