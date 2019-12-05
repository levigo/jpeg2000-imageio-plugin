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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import org.hamcrest.MatcherAssert;
import org.jadice.jpeg2000.internal.image.DefaultGridRegion;
import org.jadice.jpeg2000.internal.image.GridRegion;
import org.jadice.jpeg2000.internal.image.Region;
import org.jadice.jpeg2000.internal.image.Regions;
import org.junit.jupiter.api.Test;

public class DefaultGridRegionTest {

  @Test
  public void testDefaultCreation() {
    final Region region = new Region(0, 0, 128, 128);
    final GridRegion gridElement = new DefaultGridRegion(region);
    MatcherAssert.assertThat(gridElement.absolute(), is(equalTo(region)));
  }
  
  @Test
  public void testRelativeTo() {
    final Region absoluteRegionParent = new Region(32, 32, 128, 128);
    final DefaultGridRegion parent = new DefaultGridRegion(absoluteRegionParent);
    
    final Region absoluteRegion = new Region(64, 64, 96, 96);
    final DefaultGridRegion underTest = new DefaultGridRegion(absoluteRegion);

    final Region expectedAbsolute = Regions.copyOf(absoluteRegion);
    final Region expectedRelative = new Region(32, 32, 96, 96);

    MatcherAssert.assertThat(underTest.absolute(), is(equalTo(expectedAbsolute)));
    MatcherAssert.assertThat(underTest.relativeTo(parent), is(equalTo(expectedRelative)));
  }
  
}
