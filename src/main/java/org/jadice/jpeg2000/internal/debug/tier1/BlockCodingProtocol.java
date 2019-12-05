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

import org.jadice.jpeg2000.internal.debug.Protocol;

public interface BlockCodingProtocol extends Protocol {
  void run(int symbol);
  void runlength(int symbol);
  void contextWord(int ctxWord);
  void significance(boolean symbol);
  void sign(int symbol);
  void sample(int sample);
  void segmentationMark(int symbol);
  void refinement(int symbol);
}
