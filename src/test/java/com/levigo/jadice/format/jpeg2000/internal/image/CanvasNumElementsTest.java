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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CanvasNumElementsTest {

  private final Region region;
  private final Region partition;
  private final Pair expected;

  @Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {new Region(0, 0, 8, 8),   new Region(0, 0, 8, 8),   new Pair(1, 1)},
        {new Region(0, 0, 16, 16), new Region(0, 0, 8, 8),   new Pair(2, 2)},
        {new Region(8, 8, 16, 16), new Region(0, 0, 8, 8),   new Pair(2, 2)},
        {new Region(8, 8, 16, 16), new Region(0, 0, 32, 32), new Pair(1, 1)},
        {new Region(2, 2, 6, 6),   new Region(1, 1, 8, 8),   new Pair(1, 1)},
        {new Region(16, 16, 0, 0), new Region(1, 1, 8, 8),   new Pair(0, 0)},
    });
  }

  public CanvasNumElementsTest(Region region, Region partition, Pair expected) {
    this.region = region;
    this.partition = partition;
    this.expected = expected;
  }

  @Test
  public void runTest() {
    final String message = region + " partitioned in " + partition + ": " + expected;
    Assert.assertEquals(message, expected, Canvas.numElements(region, partition));
  }
}