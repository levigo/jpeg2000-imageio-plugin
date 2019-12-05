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
package com.levigo.jadice.format.jpeg2000.internal.dwt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.image.DefaultGridRegion;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.ResolutionMock;

public class Inverse5x3FilterTaskTest {

  /**
   * Tests the 5-tap/3-tap convolutional filter with the example shown in ITU-T.800, J.4.2
   */
  @Test
  public void example_Part1() throws Exception {
    // @formatter:off
    final int[] expecteds = new int[] {
       0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12,
       1,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12,
       2,  2,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12,
       3,  3,  3,  4,  5,  5,  6,  7,  8,  9, 10, 11, 12,
       4,  4,  4,  5,  5,  6,  7,  8,  8,  9, 10, 11, 12,
       5,  5,  5,  5,  6,  7,  7,  8,  9, 10, 11, 12, 13,
       6,  6,  6,  6,  7,  7,  8,  9, 10, 10, 11, 12, 13,
       7,  7,  7,  7,  8,  8,  9,  9, 10, 11, 12, 13, 13,
       8,  8,  8,  8,  8,  9, 10, 10, 11, 12, 12, 13, 14,
       9,  9,  9,  9,  9, 10, 10, 11, 12, 12, 13, 14, 15,
      10, 10, 10, 10, 10, 11, 11, 12, 12, 13, 14, 14, 15,
      11, 11, 11, 11, 11, 12, 12, 13, 13, 14, 14, 15, 16,
      12, 12, 12, 12, 12, 13, 13, 13, 14, 15, 15, 16, 16,
      13, 13, 13, 13, 13, 13, 14, 14, 15, 15, 16, 17, 17,
      14, 14, 14, 14, 14, 14, 15, 15, 16, 16, 17, 17, 18,
      15, 15, 15, 15, 15, 15, 16, 16, 17, 17, 18, 18, 19,
      16, 16, 16, 16, 16, 16, 17, 17, 17, 18, 18, 19, 20
    };
    // @formatter:on

    // @formatter:off
    final int[] samples = new int[] {
       0,  4,  8, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0,
       4,  5,  8, 12,  0,  1,  0,  0,  0,  0,  0,  0,  0,
       8,  8, 11, 15,  0,  1,  0,  0,  1,  0,  1,  0,  0,
      12, 12, 14, 18,  0,  0,  1,  0,  0,  0,  0, -1,  1,
      16, 16, 18, 20,  0,  0,  0,  0,  0,  0,  0,  1,  1,
       0,  0,  0,  0, -1,  0,  0,  0,  0,  1,  1,  0, -1,
       0,  1,  1,  1,  0, -1,  0,  0,  0,  1,  0,  1,  1,
       0,  0,  0,  0,  0,  1,  0,  0,  0,  0,  0,  0,  0,
       0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
       0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
       0,  0,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
       0,  0,  0,  0,  0,  1,  1,  0,  0,  1,  0,  1,  0,
       0,  0,  1,  0,  0,  1,  1,  0,  0,  0,  0,  0,  1,
       0,  0,  0,  0,  1,  0,  2,  0,  0,  0,  0,  0,  1,
       0,  0,  0,  0,  0,  0,  1,  0,  0,  0,  1,  0,  0,
       0,  0,  0,  0,  0,  0,  1,  0,  0,  0,  0,  0,  1,
       0,  0,  0,  0,  1,  1,  0,  0,  0,  0,  0, -1,  0
    };
    // @formatter:on
    
    final DummyDataBuffer dataBuffer = new DummyDataBuffer();
    dataBuffer.intSamples = samples;
    dataBuffer.scanline = 13;

    ResolutionMock r1 = new ResolutionMock();
    r1.dwtLevel = 1;
    r1.resLevel = 1;
    r1.setGridRegion(new DefaultGridRegion(new Region(7, 9)));
    r1.subbandPartition = new Region(4, 5);
    
    ResolutionMock r2 = new ResolutionMock();
    r2.dwtLevel = 0;
    r2.resLevel = 2;
    r2.setGridRegion(new DefaultGridRegion(new Region(13, 17)));
    r2.subbandPartition = new Region(7, 9);

    Inverse5x3FilterTask task = new Inverse5x3FilterTask(dataBuffer, r1);
    task.call();
    
    task = new Inverse5x3FilterTask(dataBuffer, r2);
    task.call();

    Assertions.assertArrayEquals(expecteds, samples);
  }   
  
//  @Test
//  public void example_Part1_HorizontallyDisplaced() throws Exception {
//    // @formatter:off
//    final int[] expecteds = new int[] {
//      0, 0, 0, 0,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 0, 0, 0, 0,
//      0, 0, 0, 0,  1,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 0, 0, 0, 0,
//      0, 0, 0, 0,  2,  2,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 0, 0, 0, 0,
//      0, 0, 0, 0,  3,  3,  3,  4,  5,  5,  6,  7,  8,  9, 10, 11, 12, 0, 0, 0, 0,
//      0, 0, 0, 0,  4,  4,  4,  5,  5,  6,  7,  8,  8,  9, 10, 11, 12, 0, 0, 0, 0,
//      0, 0, 0, 0,  5,  5,  5,  5,  6,  7,  7,  8,  9, 10, 11, 12, 13, 0, 0, 0, 0,
//      0, 0, 0, 0,  6,  6,  6,  6,  7,  7,  8,  9, 10, 10, 11, 12, 13, 0, 0, 0, 0,
//      0, 0, 0, 0,  7,  7,  7,  7,  8,  8,  9,  9, 10, 11, 12, 13, 13, 0, 0, 0, 0,
//      0, 0, 0, 0,  8,  8,  8,  8,  8,  9, 10, 10, 11, 12, 12, 13, 14, 0, 0, 0, 0,
//      0, 0, 0, 0,  9,  9,  9,  9,  9, 10, 10, 11, 12, 12, 13, 14, 15, 0, 0, 0, 0,
//      0, 0, 0, 0, 10, 10, 10, 10, 10, 11, 11, 12, 12, 13, 14, 14, 15, 0, 0, 0, 0,
//      0, 0, 0, 0, 11, 11, 11, 11, 11, 12, 12, 13, 13, 14, 14, 15, 16, 0, 0, 0, 0,
//      0, 0, 0, 0, 12, 12, 12, 12, 12, 13, 13, 13, 14, 15, 15, 16, 16, 0, 0, 0, 0,
//      0, 0, 0, 0, 13, 13, 13, 13, 13, 13, 14, 14, 15, 15, 16, 17, 17, 0, 0, 0, 0,
//      0, 0, 0, 0, 14, 14, 14, 14, 14, 14, 15, 15, 16, 16, 17, 17, 18, 0, 0, 0, 0,
//      0, 0, 0, 0, 15, 15, 15, 15, 15, 15, 16, 16, 17, 17, 18, 18, 19, 0, 0, 0, 0,
//      0, 0, 0, 0, 16, 16, 16, 16, 16, 16, 17, 17, 17, 18, 18, 19, 20, 0, 0, 0, 0
//    };
//    // @formatter:on
//
//    // @formatter:off
//    final int[] samples = new int[] {
//      0, 0, 0, 0,  0,  4,  8, 12,  0,  0,  0,  0,  0,  0,  0,  0,  0, 0, 0, 0, 0,
//      0, 0, 0, 0,  4,  5,  8, 12,  0,  1,  0,  0,  0,  0,  0,  0,  0, 0, 0, 0, 0,
//      0, 0, 0, 0,  8,  8, 11, 15,  0,  1,  0,  0,  1,  0,  1,  0,  0, 0, 0, 0, 0,
//      0, 0, 0, 0, 12, 12, 14, 18,  0,  0,  1,  0,  0,  0,  0, -1,  1, 0, 0, 0, 0,
//      0, 0, 0, 0, 16, 16, 18, 20,  0,  0,  0,  0,  0,  0,  0,  1,  1, 0, 0, 0, 0,
//      0, 0, 0, 0,  0,  0,  0,  0, -1,  0,  0,  0,  0,  1,  1,  0, -1, 0, 0, 0, 0,
//      0, 0, 0, 0,  0,  1,  1,  1,  0, -1,  0,  0,  0,  1,  0,  1,  1, 0, 0, 0, 0,
//      0, 0, 0, 0,  0,  0,  0,  0,  0,  1,  0,  0,  0,  0,  0,  0,  0, 0, 0, 0, 0,
//      0, 0, 0, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 0, 0, 0, 0,
//      0, 0, 0, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 0, 0, 0, 0,
//      0, 0, 0, 0,  0,  0,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 0, 0, 0, 0,
//      0, 0, 0, 0,  0,  0,  0,  0,  0,  1,  1,  0,  0,  1,  0,  1,  0, 0, 0, 0, 0,
//      0, 0, 0, 0,  0,  0,  1,  0,  0,  1,  1,  0,  0,  0,  0,  0,  1, 0, 0, 0, 0,
//      0, 0, 0, 0,  0,  0,  0,  0,  1,  0,  2,  0,  0,  0,  0,  0,  1, 0, 0, 0, 0,
//      0, 0, 0, 0,  0,  0,  0,  0,  0,  0,  1,  0,  0,  0,  1,  0,  0, 0, 0, 0, 0,
//      0, 0, 0, 0,  0,  0,  0,  0,  0,  0,  1,  0,  0,  0,  0,  0,  1, 0, 0, 0, 0,
//      0, 0, 0, 0,  0,  0,  0,  0,  1,  1,  0,  0,  0,  0,  0, -1,  0, 0, 0, 0, 0
//    };
//    // @formatter:on
//
//    final DummyDataBuffer dataBuffer = new DummyDataBuffer();
//    dataBuffer.intSamples = samples;
//    dataBuffer.scanline = 21;
//
//    Resolution r1 = new Resolution(null, null, 1, 1, new Region(4, 0, 7, 9));
//    r1.subbandPartition = new Region(4, 0, 4, 5);
//
//    Resolution r2 = new Resolution(null, null, 0, 2, new Region(4, 0, 13, 17));
//    r2.subbandPartition = new Region(4, 0, 7, 9);
//
//    Inverse5x3FilterTask underTest = new Inverse5x3FilterTask(dataBuffer, r1);
//    r1 = underTest.call();
//
//    underTest = new Inverse5x3FilterTask(dataBuffer, r2);
//    r2 = underTest.call();
//
//    Assert.assertArrayEquals(expecteds, samples);
//  }
       
}      
       