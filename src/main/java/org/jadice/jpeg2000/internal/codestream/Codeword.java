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
package org.jadice.jpeg2000.internal.codestream;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.document.io.ConcatenatedInputStream;
import com.levigo.jadice.document.io.SeekableInputStream;

@Refer(to = Spec.J2K_CORE, page = 56, section = "B.10.7", called = "Length of the Compressed Image Data from a Given Code Block")
public class Codeword {

  public volatile int passes;
  public volatile int passIdx;

  public int numBytes;

  public SeekableInputStream input;

  public void merge(Codeword additional) {
    passes += additional.passes;
    numBytes += additional.numBytes;

    if (input == null) {
      input = new ConcatenatedInputStream();
    }
    
    ((ConcatenatedInputStream) input).appendInputStream(additional.input);
  }
}
