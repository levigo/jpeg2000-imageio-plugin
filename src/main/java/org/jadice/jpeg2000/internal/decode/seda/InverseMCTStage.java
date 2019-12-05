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
package org.jadice.jpeg2000.internal.decode.seda;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.buffer.DummyDataBuffer;

// part of an experiment. Currently not in use.
public class InverseMCTStage extends ConfigurableStage
    implements
      Transformer<DummyDataBuffer, Object, JPEG2000Exception> {

  @Override
  public void transform(DummyDataBuffer s, Consumer<? super Object, ? extends JPEG2000Exception> next)
      throws JPEG2000Exception {
    // TODO Auto-generated method stub
  }
}