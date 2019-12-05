package com.levigo.jadice.format.jpeg2000.internal.codestream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.levigo.jadice.document.io.MemoryInputStream;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.BlockContributionStage;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.BufferedImageAssembler;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.ColorPreparation;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.ForwardTilePartReading;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.Gate;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.HighestResolutionOnly;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.InverseDWT;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.InverseQuantization;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.PacketBodyReading;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.ResultCollector;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.TileComposition;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.TilePartBodyReading;
import com.levigo.jadice.format.jpeg2000.internal.image.DecodedBufferedImage;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;

public class DecodingPipelineTest {

  @Test
  public void test() throws Exception {
    final String resourcePath = "/files/virgindigital_50.j2k";
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
