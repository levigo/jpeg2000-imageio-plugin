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
package com.levigo.jadice.format.jpeg2000.internal.debug.dwt;

import static com.levigo.jadice.format.jpeg2000.internal.debug.IntegerParameter.integer;
import static com.levigo.jadice.format.jpeg2000.internal.debug.tcq.QuantizationToken.Finish;

import java.util.ArrayList;
import java.util.Collection;

import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolBase;
import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolToken;

public class BasicDWTProtocol extends ProtocolBase implements DWTProtocol {

  private static final Collection<ProtocolToken> DWT_TOKENS;

  static {
    DWT_TOKENS = new ArrayList<ProtocolToken>();
    for (DWTToken dwtToken : DWTToken.values()) {
      DWT_TOKENS.add(dwtToken);
    }
  }

  public BasicDWTProtocol() {
    super(DWT_TOKENS);
  }

  @Override
  public void finish() {
    createAndNotify(Finish, integer(getNumElements()));
  }

}
