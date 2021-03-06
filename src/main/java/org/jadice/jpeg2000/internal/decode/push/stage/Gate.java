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
package org.jadice.jpeg2000.internal.decode.push.stage;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.Pushable;
import org.jadice.jpeg2000.internal.decode.push.Receiver;

public class Gate<P extends Pushable> implements Receiver<P> {
  private final Receiver<P> nextStage;
  private final GateKeeper<P> gateKeeper;

  public Gate(Receiver<P> nextStage, GateKeeper<P> gateKeeper) {
    this.nextStage = nextStage;
    this.gateKeeper = gateKeeper;
  }

  @Override
  public void receive(P pushable, DecoderParameters parameters) throws JPEG2000Exception {
    if (gateKeeper.allows(pushable)) {
      nextStage.receive(pushable, parameters);
    }
  }
}
