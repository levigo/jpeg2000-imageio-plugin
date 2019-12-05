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
package org.jadice.jpeg2000.internal.debug.tier1;

import static org.jadice.jpeg2000.internal.debug.BitsParameter.bits;
import static org.jadice.jpeg2000.internal.debug.BooleanParameter.bool;
import static org.jadice.jpeg2000.internal.debug.IntegerParameter.integer;
import static org.jadice.jpeg2000.internal.debug.tier1.BlockCodingToken.ContextWord;
import static org.jadice.jpeg2000.internal.debug.tier1.BlockCodingToken.Finish;
import static org.jadice.jpeg2000.internal.debug.tier1.BlockCodingToken.Refinement;
import static org.jadice.jpeg2000.internal.debug.tier1.BlockCodingToken.RunLength;
import static org.jadice.jpeg2000.internal.debug.tier1.BlockCodingToken.RunMode;
import static org.jadice.jpeg2000.internal.debug.tier1.BlockCodingToken.Sample;
import static org.jadice.jpeg2000.internal.debug.tier1.BlockCodingToken.SegmentationMark;
import static org.jadice.jpeg2000.internal.debug.tier1.BlockCodingToken.Sign;
import static org.jadice.jpeg2000.internal.debug.tier1.BlockCodingToken.Significance;
import static org.jadice.jpeg2000.internal.debug.tier1.BlockCodingToken.values;

import java.util.ArrayList;
import java.util.Collection;

import org.jadice.jpeg2000.internal.debug.ProtocolBase;
import org.jadice.jpeg2000.internal.debug.ProtocolToken;

public class BasicBlockCodingProtocol extends ProtocolBase implements BlockCodingProtocol {

  private static final Collection<ProtocolToken> BLOCK_CODING_TOKENS;

  static {
    BLOCK_CODING_TOKENS = new ArrayList<ProtocolToken>();
    for (BlockCodingToken blockCodingToken : values()) {
      BLOCK_CODING_TOKENS.add(blockCodingToken);
    }
  }

  public BasicBlockCodingProtocol() {
    super(BLOCK_CODING_TOKENS);
  }

  @Override
  public void finish() {
    createAndNotify(Finish, integer(getNumElements()));
  }

  @Override
  public void run(int symbol) {
    createAndNotify(RunMode, bool(symbol == 0));
  }

  @Override
  public void runlength(int symbol) {
    createAndNotify(RunLength, bits(symbol, 2));
  }

  @Override
  public void contextWord(int ctxWord) {
    createAndNotify(ContextWord, integer(ctxWord));
  }

  @Override
  public void significance(boolean symbol) {
    createAndNotify(Significance, bool(symbol));
  }

  @Override
  public void sign(int symbol) {
    createAndNotify(Sign, bits(symbol, 1));
  }

  @Override
  public void sample(int sample) {
    createAndNotify(Sample, integer(sample));
  }

  @Override
  public void segmentationMark(int sym) {
    createAndNotify(SegmentationMark, bits(sym, 4));
  }

  @Override
  public void refinement(int symbol) {
    createAndNotify(Refinement, bool(symbol == 1));
  }
}
