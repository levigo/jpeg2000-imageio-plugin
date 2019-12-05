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
package com.levigo.jadice.format.jpeg2000.internal.marker;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;

@RunWith(Parameterized.class)
public class QCCTest extends MarkerTestBase<QCC> {

  @Parameter(1)
  public int Lqcc;

  @Parameter(2)
  public int Cqcc;

  @Parameter(3)
  public int Sqcc_guardbits;
  
  @Parameter(4)
  public int Sqcc_style;

  @Parameter(5)
  public int[] SPqcc_exp;
  
  @Parameter(6)
  public int[] SPqcc_man;
  
  @Parameter(7)
  public int[] SPqcc_dzone;
  
  @Parameters(name = "{0}")
  public static Object[][] data() {
    return new Object[][]{
        // @formatter:off
        {"qcc/qcc_p0_03", 8, 0, 2, 0, new int[]{4,5,5,6}, null, null}
        // @formatter:on
    };
  }

  @Override
  protected Marker marker() {
    return Marker.QCC;
  }
  
  @Override
  protected void inspect(QCC underTest) {
    assertEquals("Lqcc", Lqcc, underTest.Lqcc);
    assertEquals("Cqcc", Cqcc, underTest.Cqcc);
    assertEquals("Sqcc_guardbits", Sqcc_guardbits, underTest.Sqxx_guardbits);
    assertEquals("Sqcc_style", Sqcc_style, underTest.Sqxx_style);
    assertArrayEquals("SPqcc_exp", SPqcc_exp, underTest.SPqxx_exp);
    assertArrayEquals("SPqcc_man", SPqcc_man, underTest.SPqxx_man);
    assertArrayEquals("SPqcc_dzone", SPqcc_dzone, underTest.SPqxx_dzone);
  }
  
  @Override
  protected QCC prepareMarker() {
    final QCC qcc = new QCC();
    qcc.Lqcc = Lqcc;
    qcc.Cqcc = Cqcc;
    qcc.Sqxx_guardbits = (byte) Sqcc_guardbits;
    qcc.Sqxx_style = (byte) Sqcc_style;
    qcc.SPqxx_exp = SPqcc_exp;
    qcc.SPqxx_man = SPqcc_man;
    qcc.SPqxx_dzone = SPqcc_dzone;
    return qcc;
  }

  @Override
  protected Codestream codestream() {
    final Codestream codestream = super.codestream();
    codestream.numComps = 1;
    return codestream;
  }
}
