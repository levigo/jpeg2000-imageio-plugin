package com.levigo.jadice.format.jpeg2000.internal.decode.push;

import java.awt.image.WritableRaster;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.BlockContributionStage;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.ColorPreparation;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.Gate;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.HighestResolutionOnly;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.InverseDWT;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.InverseQuantization;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.PacketBodyReading;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.ResultCollector;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.TileComposition;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.TilePartBodyReading;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.ForwardTilePartReading;
import com.levigo.jadice.format.jpeg2000.internal.image.DecodedRaster;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;

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
