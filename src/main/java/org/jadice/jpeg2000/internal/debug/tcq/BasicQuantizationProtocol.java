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
package org.jadice.jpeg2000.internal.debug.tcq;

import static org.jadice.jpeg2000.internal.debug.IntegerParameter.integer;
import static org.jadice.jpeg2000.internal.debug.NullParameter.noParam;
import static org.jadice.jpeg2000.internal.debug.tcq.QuantizationToken.Downshift;
import static org.jadice.jpeg2000.internal.debug.tcq.QuantizationToken.FillWithZeros;
import static org.jadice.jpeg2000.internal.debug.tcq.QuantizationToken.Finish;
import static org.jadice.jpeg2000.internal.debug.tcq.QuantizationToken.ValueAfter;
import static org.jadice.jpeg2000.internal.debug.tcq.QuantizationToken.ValueBefore;

import java.util.ArrayList;
import java.util.Collection;

import org.jadice.jpeg2000.internal.debug.ProtocolBase;
import org.jadice.jpeg2000.internal.debug.ProtocolToken;

public class BasicQuantizationProtocol extends ProtocolBase implements QuantizationProtocol {

  private static final Collection<ProtocolToken> QUANTIZATION_TOKENS;

  static {
    QUANTIZATION_TOKENS = new ArrayList<ProtocolToken>();
    for (QuantizationToken quantizationToken : QuantizationToken.values()) {
      QUANTIZATION_TOKENS.add(quantizationToken);
    }
  }

  public BasicQuantizationProtocol() {
    super(QUANTIZATION_TOKENS);
  }

  @Override
  public void downshift(int downshift) {
    createAndNotify(Downshift, integer(downshift));
  }

  @Override
  public void valueBefore(int value) {
    createAndNotify(ValueBefore, integer(value));
  }

  @Override
  public void valueAfter(int value) {
    createAndNotify(ValueAfter, integer(value));
  }

  @Override
  public void fillWithZeros() {
    createAndNotify(FillWithZeros, noParam());
  }

  @Override
  public void finish() {
    createAndNotify(Finish, integer(getNumElements()));
  }
}
