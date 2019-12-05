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
package org.jadice.jpeg2000.internal.marker;

import org.jadice.jpeg2000.internal.param.PropertiesParameterInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class PropertiesParameterInfoTest {

  // @formatter:off
  @Parameterized.Parameters
  public static Iterable<Object[]> params() {
    return Arrays.asList(new Object[][]{
      {new String[]{"SIZ.L","SIZ.R","SIZ.X","SIZ.Y","SIZ.XO","SIZ.YO","SIZ.XT","SIZ.YT","SIZ.XTO","SIZ.YTO","SIZ.C"}},
      {new String[]{"SIZ.S","SIZ.XR","SIZ.YR"}},
      {new String[]{"TLM.Z","TLM.L","TLM.S","TLM.T","TLM.P"}},
      {new String[]{"COM.L","COM.R","COM.C"}},
      {new String[]{"COD.L","COD.S","COD.SG.order","COD.SG.layers","COD.SG.mct","COD.SP.NL","COD.SP.xcb","COD.SP.ycb","COD.SP.modes","COD.SP.kernel","COD.SP.sso","COD.SP.precincts"}},
      {new String[]{"COC.SP.NL","COC.SP.xcb","COC.SP.ycb","COC.SP.modes","COC.SP.kernel","COC.SP.sso","COC.SP.precincts","COC.SP.modes","COC.SP.kernel","COC.SP.sso","COC.SP.precincts"}},
      {new String[]{"QCD.L","QCC.L","QPx.L","QPx.PL","QPx.PP","QPx.S.style","QxD.S.style","QxC.C","QxC.S.style","Qxx.SP.exp0","Qxx.SP.exp","Qxx.SP.man","Qxx.SP.deadzone","Qxx.S.guardbits"}},
      {new String[]{"DCO.L","DCO.S","DCO.SP"}},
      {new String[]{"POC.L","POC.RS","POC.CS","POC.LYE","POC.RE","POC.CE","POC.P"}},
      {new String[]{"ADS.L","ADS.S","ADS.IO","ADS.DO","ADS.IS","ADS.DS"}},
      {new String[]{"ATK.A","ATK.L","ATK.S","ATK.K","ATK.N","ATK.O","ATK.E","ATK.B","ATK.LC"}},
      {new String[]{"CBD.L","CBD.N","CBD.BD"}},
      {new String[]{"CRG.L","CRG.X","CRG.Y"}},
      {new String[]{"DFS.L","DFS.S","DFS.I","DFS.D"}},
      {new String[]{"SOP.L","SOP.N"}},
      {new String[]{"MCO.L","MCO.N","MCO.I"}},
      {new String[]{"MCC.L","MCC.Z","MCC.I","MCC.Y","MCC.Q","MCC.X","MCC.N","MCC.C","MCC.M","MCC.W","MCC.T","MCC.O"}},
      {new String[]{"VMS.L","VMS.C","VMS.S","VMS.W","VMS.R","VMS.A","VMS.B"}},
      {new String[]{"MCT.L","MCT.Z","MCT.I","MCT.Y","MCT.SP"}},
      {new String[]{"RGN.L","RGN.C","RGN.S","RGN.SP","RGN.XA","RGN.YA","RGN.XB","RGN.YB"}},
      {new String[]{"SOT.L","SOT.I","SOT.P","SOT.TP","SOT.TN"}},
      {new String[]{"PLM.L","PLM.Z"}},
      {new String[]{"NLT.L","NLT.C","NLT.BD","NLT.T","NLT.ST.E","NLT.ST.S","NLT.ST.T","NLT.ST.A","NLT.ST.B","NLT.ST.Npoints","NLT.ST.Dmin","NLT.ST.Dmax","NLT.ST.PTval","NLT.ST.Tvalue"}}
    });
  }
  // @formatter:on

  private String[] keys;

  public PropertiesParameterInfoTest(String[] keys) {
    this.keys = keys;
  }

  @Test
  public void testPropertiesLoading() {
    for (String key : keys) {
      PropertiesParameterInfo info = new PropertiesParameterInfo(key, key);
      String description = info.getDescription();
      Assert.assertNotNull(key + " should exist but was null", description);
      Assert.assertTrue("length of description for " + key + " should not be empty.", description.length() > 0);
    }
  }
}
