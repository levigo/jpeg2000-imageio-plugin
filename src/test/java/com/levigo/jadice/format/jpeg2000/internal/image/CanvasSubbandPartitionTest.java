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
public class CanvasSubbandPartitionTest {

  @Parameters(name= "{index}: resolution[{1}]={0}")
  public static Object[][] data() {
    return new Object[][]{
        {new Region(16, 16), 0, new Region(16, 16)},
        {new Region(32, 32), 1, new Region(16, 16)},
        {new Region(32, 32, 32, 32), 0, new Region(32, 32, 32, 32)},
        {new Region(32, 32, 32, 32), 1, new Region(32, 32, 16, 16)},
        {new Region(32, 32, 33, 32), 1, new Region(32, 32, 17, 16)}
    };
  }
  
  @Parameter(0)
  public Region resRegion;

  @Parameter(1)
  public int resLevel;

  @Parameter(2)
  public Region expected;
  
  @Test
  public void test() {
    assertEquals(expected, Canvas.subbandPartition(resRegion, resLevel));
  }
}
