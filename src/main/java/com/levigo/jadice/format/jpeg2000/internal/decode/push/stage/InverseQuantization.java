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

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import org.jadice.util.log.Logger;
import org.jadice.util.log.LoggerFactory;

import com.levigo.jadice.document.internal.render.debug.PartialResultsDebugger;
import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.Subband;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponent;
import com.levigo.jadice.format.jpeg2000.internal.marker.Qxx;
import com.levigo.jadice.format.jpeg2000.internal.tcq.InverseIrreversibleQuantizationTask;
import com.levigo.jadice.format.jpeg2000.internal.tcq.InverseReversibleQuantizationTask;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;

@Refer(to = Spec.J2K_CORE, page = 95, section = "Annex E", called = "Quantization")
public class InverseQuantization implements Receiver<CodeBlock> {
  protected static final Logger LOGGER = LoggerFactory.getLogger(InverseQuantization.class);

  private final Receiver<CodeBlock> nextStage;

  public InverseQuantization(Receiver<CodeBlock> nextStage) {
    this.nextStage = nextStage;
  }

  @Override
  public void receive(CodeBlock block, DecoderParameters parameters) throws JPEG2000Exception {
    if (block.state.finished) {
      final TileComponent tileComp = block.bandPrecinct.precinct.resolution.tileComp;
      if (tileComp.state == null) {
        tileComp.start(parameters);
      }

      final DummyDataBuffer buffer = tileComp.state.sampleBuffer;
      final DecodeTask<CodeBlock> task = createTask(block, buffer);
      try {
        final CodeBlock codeBlock = task.call();

        if (Debug.VISUALIZE_QUANTIZATION && block.bandPrecinct.subband.resolution.tileComp.comp.idx == 1) {
          showIntermediateResult(block, tileComp, buffer);
        }

        nextStage.receive(codeBlock, parameters);
      } catch (final JPEG2000Exception e) {
        throw e;
      } catch (final Exception e) {
        throw new JPEG2000Exception(GeneralMessages.WRAPPED_EXCEPTION, e);
      }
    }
  }

  private void showIntermediateResult(CodeBlock block, TileComponent tileComp, DummyDataBuffer buffer) {
    Raster raster;
    if (buffer.intSamples != null) {
      raster = Debug.intSamplesToRaster(buffer.intSamples, buffer.scanline, tileComp.comp.isSigned, 0);
    } else {
      float max = Float.MIN_VALUE, min = Float.MAX_VALUE;
      for (final float f : buffer.floatSamples) {
        max = Math.max(f, max);
        min = Math.min(f, min);
      }

      final float scale = max != min ? 255f / (max - min) : 1;
      final float offset = -min;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("range after inverse quantization, min=" + min + ", max=" + max + ", c=" + tileComp.comp.idx
            + ", r=" + block.region() + ", offset=" + offset + ", scale=" + scale);
      }

      raster = Debug.floatSamplesToRaster(buffer.floatSamples, buffer.scanline, offset, scale);
    }

    final PartialResultsDebugger debugger = PartialResultsDebugger.getInstance();
    final String name = "after inverse quantization, c=" + tileComp.comp.idx + ", r=" + block.region().absolute();
    debugger.addDebugResult(raster, BufferedImage.TYPE_BYTE_GRAY, name);
    // final Region tileCompRegion = tileComp.region;
    // Buffers.dumpSamples(buffer.intSamples, tileCompRegion.size.y, tileCompRegion.size.x);
  }

  private DummyDataBuffer getSampleBuffer(CodeBlock block, DecoderParameters parameters) {
    final TileComponent tileComp = block.bandPrecinct.precinct.resolution.tileComp;

    if (tileComp.state == null) {
      tileComp.start(parameters);
    }

    return tileComp.state.sampleBuffer;
  }

  private DecodeTask<CodeBlock> createTask(final CodeBlock block, DummyDataBuffer buffer) throws JPEG2000Exception {
    final Subband band = block.bandPrecinct.subband;
    final TileComponent tileComp = band.resolution.tileComp;
    final Qxx qxx = Qxx.accessQCx(tileComp.codestream, tileComp.tile, tileComp.comp.idx);

    switch (qxx.Sqxx_style){
      case Qxx.VALUE_NO_QUANTIZATION :
//        System.err.println("NO QUANTIZATION");
        return new InverseReversibleQuantizationTask(block, buffer);

        // TODO implement at least the other two cases defined in ITU-T.800:
        // case Qxx.VALUE_SCALAR_DERIVED :

      case Qxx.VALUE_SCALAR_DERIVED :
//        System.err.println("SCALAR_DERIVED");
        return new InverseIrreversibleQuantizationTask(block, buffer);
      
      case Qxx.VALUE_SCALAR_EXPOUNDED :
//        System.err.println("SCALAR EXPOUNDED");
        return new InverseIrreversibleQuantizationTask(block, buffer);

      default :
//        System.err.println("UNHANDLED QUANTIZATION");
        throw new JPEG2000Exception(CodestreamMessages.UNSUPPORTED_CODESTREAM_FEATURE, "(de)quantization style "
            + Integer.toBinaryString(qxx.Sqxx_style));
    }
  }

}
