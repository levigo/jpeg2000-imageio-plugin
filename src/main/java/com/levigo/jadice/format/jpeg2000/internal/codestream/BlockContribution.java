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
package com.levigo.jadice.format.jpeg2000.internal.codestream;

import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;

import java.util.LinkedList;
import java.util.List;

/**
 * Holds information used while decoding codewords belonging to a code-block.
 */
public class BlockContribution implements Pushable {

  public final CodeBlock block;

  /** The amount of passes the current packet contributes to the code-block */
  public int passes;

  public int layer;

  public boolean isLastLayer;
  
  public final List<Codeword> codewords;

  public BlockContribution(CodeBlock block) {
    this.block = block;
    codewords = new LinkedList<>();
  }

  @Override
  public void start(DecoderParameters parameters) {
    
  }

  @Override
  public void free() {

  }
}
