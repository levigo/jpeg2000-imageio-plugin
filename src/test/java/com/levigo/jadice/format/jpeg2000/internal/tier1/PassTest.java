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
package com.levigo.jadice.format.jpeg2000.internal.tier1;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PassTest extends PassTestBase {

  @Parameters(name = "{0}")
  public static String[] data() {
    return new String[] { 
        "/com/levigo/jadice/format/jpeg2000/internal/tier1/p0_02.j2k.trace.json",
        "/com/levigo/jadice/format/jpeg2000/internal/tier1/p0_03.j2k.trace.json",
        "/com/levigo/jadice/format/jpeg2000/internal/tier1/crusoe01.trace.json", // Block with causal context
// "DSGVO Commit 70063732a1dc8d26722657eb95b8325fd164ce83"
//        "/com/levigo/jadice/format/jpeg2000/internal/tier1/porsche.j2k.trace.json"
    };
  }
  
  @Parameter(0)
  public String resourcePath;
  
  @Override
  protected String getTraceFileResourcePath() {
    return resourcePath;
  }
}
