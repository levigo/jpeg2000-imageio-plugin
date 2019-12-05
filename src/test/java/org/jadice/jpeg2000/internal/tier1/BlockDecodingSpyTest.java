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
package org.jadice.jpeg2000.internal.tier1;

import org.junit.jupiter.api.Disabled;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@Disabled("DSGVO Commit 70063732a1dc8d26722657eb95b8325fd164ce83")
//@RunWith(Parameterized.class)
public class BlockDecodingSpyTest extends BlockDecodingSpyTestBase {

  @Parameters
  public static Object[][] parameters() {
    return new Object[][] {
        {"/com/levigo/jadice/format/jpeg2000/internal/tier1/porsche.j2k.mqtrace.json", "/files/porsche.j2k"}
    };
  }
  
  @Parameter(0)
  public String traceFileResourcePath;

  @Parameter(1)
  public String inputFileResourcePath;
  
  @Override
  protected String getTraceFileResourcePath() {
    return traceFileResourcePath;
  }

  @Override
  protected String getInputFileResourcePath() {
    return inputFileResourcePath;
  }
}
