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
package com.levigo.jadice.format.jpeg2000.internal.decode.push.stage;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.image.DecodedImage;
import com.levigo.jadice.format.jpeg2000.internal.image.DecodedRaster;

public class RenderedImageAssemblerStage implements Receiver<DecodedRaster> {
  private final Receiver<DecodedImage> nextStage;

  public RenderedImageAssemblerStage(Receiver<DecodedImage> nextStage) {
    this.nextStage = nextStage;
  }

  @Override
  public void receive(DecodedRaster pushable, DecoderParameters parameters) throws JPEG2000Exception {

  }
}
