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

import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.stage.BlockContributionStage;
import org.jadice.jpeg2000.internal.decode.push.stage.BufferedImageAssembler;
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
import org.jadice.jpeg2000.internal.image.DecodedBufferedImage;
import org.jadice.jpeg2000.internal.image.Resolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.levigo.jadice.document.io.MemoryInputStream;

public class DecodingPipelineTest {

  @Test
  public void test() throws Exception {
    final String resourcePath = "/codestreams/profile0/p0_01.j2k";
    final MemoryInputStream source = new MemoryInputStream(getClass().getResourceAsStream(resourcePath));

    final Codestream codestream = new Codestream(source);
    codestream.init();

    final ResultCollector<DecodedBufferedImage> resultCollector = new ResultCollector<>();

    final BufferedImageAssembler bufferedImageAssembler = new BufferedImageAssembler(resultCollector);
    final TileComposition tileComposition = new TileComposition(bufferedImageAssembler);
    final ColorPreparation colorPreparation = new ColorPreparation(tileComposition);
    final Gate<Resolution> resolutionGate = new Gate<>(colorPreparation, new HighestResolutionOnly());
    final InverseDWT inverseDWT = new InverseDWT(resolutionGate);
    final InverseQuantization quantization = new InverseQuantization(inverseDWT);
    final BlockContributionStage blockContribution = new BlockContributionStage(quantization);
    final PacketBodyReading packetBodyReading = new PacketBodyReading(blockContribution);
    final TilePartBodyReading tilePartBodyReading = new TilePartBodyReading(packetBodyReading);
    final ForwardTilePartReading forwardTilePartReading = new ForwardTilePartReading(tilePartBodyReading);

    final DecoderParameters parameters = new DecoderParameters();
    parameters.region = codestream.region().absolute();

    forwardTilePartReading.receive(codestream, parameters);

    // check result
    Assertions.assertNotNull(resultCollector.getResult());
  }
}
