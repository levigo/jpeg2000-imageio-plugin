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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class RegionsReplicationTest {

  @Parameter(0)
  public Region partition;

  @Parameter(1)
  public int x;
  
  @Parameter(2)
  public int y;

  @Parameter(3)
  public Region expected;

  @Parameter(4)
  public Region limit;
  

  @Parameterized.Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {new Region(100, 100), 0, 0, new Region(100, 100), null},
        {new Region(100, 100), 1, 1, new Region(100, 100, 100, 100), null},
        {new Region(10, 10), 100, 100, new Region(1000, 1000, 10, 10), null},
        {new Region(10, 10, 10, 10), 100, 100, new Region(1010, 1010, 10, 10), null},
        {new Region(10, 10, 10, 10), -1, -1, new Region(0, 0, 10, 10), null},
        {new Region(0, 0, 32768, 32768), 0, 0, new Region(128, 0, 128, 128), new Region(128, 0, 128, 128)},
        {new Region(0, 0, 64, 64), 0, 0, new Region(0, 128, 64, 64), new Region(0, 128, 64, 64)},
    });
  }

  @Test
  public void test() {
    final Region actual;
    
    if (limit != null) {
      actual = Regions.replicate(partition, x, y, limit);
    } else {
      actual = Regions.replicate(partition, x, y);
    }

    Assert.assertEquals(expected, actual);
  }

}
