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
package org.jadice.jpeg2000.internal.decode.push;

import java.awt.image.WritableRaster;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.stage.BlockContributionStage;
import org.jadice.jpeg2000.internal.decode.push.stage.ColorPreparation;
import org.jadice.jpeg2000.internal.decode.push.stage.ForwardTilePartReading;
import org.jadice.jpeg2000.internal.decode.push.stage.Gate;
import org.jadice.jpeg2000.internal.decode.push.stage.HighestResolutionOnly;
import org.jadice.jpeg2000.internal.decode.push.stage.InverseDWT;
import org.jadice.jpeg2000.internal.decode.push.stage.InverseQuantization;
import org.jadice.jpeg2000.internal.decode.push.stage.PacketBodyReading;
import org.jadice.jpeg2000.internal.decode.push.stage.ResultCollector;
import org.jadice.jpeg2000.internal.decode.push.stage.TileComposition;
import org.jadice.jpeg2000.internal.decode.push.stage.TilePartBodyReading;
import org.jadice.jpeg2000.internal.image.DecodedRaster;
import org.jadice.jpeg2000.internal.image.Resolution;
import org.jadice.jpeg2000.msg.GeneralMessages;

/**
 * Main driver and entry point. Receives the {@link Codestream} and triggers decoding.
 * 
 * Basic flow:
 * <ul>
 *   <li>Main Header is parsed (see {@link Codestream})</li>
 *   <li>Push Pipeline is triggered. The flow within the pipeline is defined by the pipeline steps defined in this class.</li>
 * </ul>
 * 
 */
public class PushPipeline {
  public WritableRaster start(Codestream codestream, DecoderParameters parameters) throws JPEG2000Exception {
    final ResultCollector<DecodedRaster> resultCollector = new ResultCollector<>();
    final TileComposition tileComposition = new TileComposition(resultCollector);
    final ColorPreparation colorPreparation = new ColorPreparation(tileComposition);
    final Gate<Resolution> resolutionGate = new Gate<>(colorPreparation, new HighestResolutionOnly());
    final InverseDWT inverseDWT = new InverseDWT(resolutionGate);
    final InverseQuantization quantization = new InverseQuantization(inverseDWT);
    final BlockContributionStage blockContribution = new BlockContributionStage(quantization);
    final PacketBodyReading packetBodyReading = new PacketBodyReading(blockContribution);
    final TilePartBodyReading tilePartBodyReading = new TilePartBodyReading(packetBodyReading);
    final ForwardTilePartReading forwardTilePartReading = new ForwardTilePartReading(tilePartBodyReading);

    try {
      forwardTilePartReading.receive(codestream, parameters);
      final DecodedRaster result = resultCollector.getResult();

      if (result == null || result.raster == null) {
        throw new JPEG2000Exception(GeneralMessages.MISSING_RESULT);
      }

      return result.raster;
    } catch (JPEG2000Exception e) {
      throw e;
    } catch (Exception e) {
      throw new JPEG2000Exception(GeneralMessages.WRAPPED_EXCEPTION, e);
    } finally {
      codestream.free();
    }
  }
}
