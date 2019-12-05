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

import org.jadice.jpeg2000.internal.image.Pair;
import org.jadice.jpeg2000.internal.image.Region;
import org.jadice.jpeg2000.internal.image.Regions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class CanvasChildIndexTest {

  private final Region parent;
  private final Region partition;
  private final Pair expectedFirst;
  private final Pair expectedLast;

  @Parameterized.Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {new Region(0, 0, 128, 128),     new Region(128, 128), new Pair(0, 0), new Pair(0, 0)},
        {new Region(0, 0, 127, 127),     new Region(128, 128), new Pair(0, 0), new Pair(0, 0)},
        {new Region(1, 1, 128, 128),     new Region(128, 128), new Pair(0, 0), new Pair(1, 1)},
        {new Region(1, 1, 127, 127),     new Region(128, 128), new Pair(0, 0), new Pair(0, 0)},
        {new Region(64, 64, 127, 127),   new Region(128, 128), new Pair(0, 0), new Pair(1, 1)},
        {new Region(0, 0, 1024, 1024),   new Region(128, 128), new Pair(0, 0), new Pair(7, 7)},
        {new Region(128, 128, 127, 127), new Region(128, 128), new Pair(1, 1), new Pair(1, 1)},
        {new Region(512, 512, 512, 512), new Region(128, 128), new Pair(4, 4), new Pair(7, 7)},
        {new Region(300, 300, 300, 300), new Region(32, 32),   new Pair(9, 9), new Pair(18, 18)}
    });
  }

  public CanvasChildIndexTest(Region parent, Region partition, Pair expectedFirst, Pair expectedLast) {
    this.parent = parent;
    this.partition = partition;
    this.expectedFirst = expectedFirst;
    this.expectedLast = expectedLast;
  }

  @Test
  public void runTest() {
    final Pair actualFirst = Regions.getFirstChildIdx(parent, partition);
    final Pair actualLast = Regions.getLastChildIdx(parent, partition);

    Assert.assertEquals("First expected=" + expectedFirst + ", actual=" + actualFirst, expectedFirst, actualFirst);
    Assert.assertEquals("Last expected=" + expectedLast + ", actual=" + actualLast, expectedLast, actualLast);
  }

}
