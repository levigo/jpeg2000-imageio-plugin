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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.util.Hashtable;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.Receiver;
import org.jadice.jpeg2000.internal.image.DecodedBufferedImage;
import org.jadice.jpeg2000.internal.image.DecodedRaster;
import org.jadice.jpeg2000.msg.GeneralMessages;

public class BufferedImageAssembler implements Receiver<DecodedRaster> {
  private final Receiver<? super DecodedBufferedImage> nextStage;

  public BufferedImageAssembler(Receiver<? super DecodedBufferedImage> nextStage) {
    this.nextStage = nextStage;
  }

  @Override
  public void receive(DecodedRaster raster, DecoderParameters parameters) throws JPEG2000Exception {
    ColorModel cm = null;

    // FIXME
    switch (raster.codestream.numComps){
      case 1 :
        cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[]{
          8
        }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        break;
      case 3 :
        cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{
            8, 8, 8
        }, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        break;
      default :
        throw new JPEG2000Exception(GeneralMessages.UNSUPPORTED_COMPONENT_COUNT, raster.codestream.numComps);
    }

    final BufferedImage image = new BufferedImage(cm, raster.raster, false, new Hashtable<>());

    nextStage.receive(new DecodedBufferedImage(image, raster.codestream), parameters);
  }
}
