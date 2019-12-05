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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RegionCoversTest {
  
  @Parameters(name = "{index}: {0} vs. {1}")
  public static Object[][] parameters() {
    return new Object[][]{
        {new Region(0, 0, 256, 256), new Region(0, 0, 256, 256), true},
        {new Region(0, 0, 256, 256), new Region(1, 1, 255, 255), true},
        {new Region(0, 0, 256, 256), new Region(128, 128, 128, 128), true},
        {new Region(128, 128, 256, 256), new Region(128, 128, 256, 256), true},
        {new Region(0, 0, 256, 256), new Region(0, 0, 257, 256), false},
        {new Region(0, 0, 256, 256), new Region(0, 0, 256, 257), false},
        {new Region(128, 128, 256, 256), new Region(127, 128, 256, 256), false},
        {new Region(128, 128, 256, 256), new Region(128, 127, 256, 256), false},
    };
  }
  
  @Parameter(0)
  public Region first;
  
  @Parameter(1)
  public Region second;

  @Parameter(2)
  public boolean expected;
      
  @Test
  public void testThat_covers_returnsExpectedBoolean() {
    assertThat(first.covers(second), is(expected));
  }
  
}
