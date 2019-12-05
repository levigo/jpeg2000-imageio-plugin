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

import com.levigo.jadice.document.internal.render.debug.PartialResultsDebugger;
import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.LongAggregator;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.TimeMeasuringTask;
import com.levigo.jadice.format.jpeg2000.internal.dwt.Inverse5x3FilterTask;
import com.levigo.jadice.format.jpeg2000.internal.dwt.Inverse9x7FilterTask;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponent;
import com.levigo.jadice.format.jpeg2000.internal.marker.COx;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;
import org.jadice.util.log.Logger;
import org.jadice.util.log.LoggerFactory;

public class InverseDWT implements Receiver<CodeBlock> {
  protected static final Logger LOGGER = LoggerFactory.getLogger(InverseDWT.class);

  private final Receiver<Resolution> nextStage;

  private LongAggregator aggregator;

  public InverseDWT(Receiver<Resolution> nextStage) {
    this.nextStage = nextStage;

    if (Debug.RECORD_DWT_FILTER_TIME) {
      aggregator = new LongAggregator();
    }
  }

  @Override
  public void receive(CodeBlock codeBlock, DecoderParameters parameters) throws JPEG2000Exception {
    // First, collect all code-blocks until all code-blocks that belong to the same resolution are
    // completed.
    Resolution resolution = codeBlock.bandPrecinct.subband.resolution;
    if (resolution.state == null) {
      resolution.start(parameters);
    }

    if (resolution.state.blocksLeft.decrementAndGet() == 0) {

      if (resolution.resLevel > 0) {
        final TileComponent tileComp = resolution.tileComp;
        final DummyDataBuffer sampleBuffer = tileComp.state.sampleBuffer;

        final DecodeTask<Resolution> task = createFilterTask(resolution, tileComp, sampleBuffer);

        try {
          resolution = task.call();

          if (resolution.dwtLevel == 0) {
            tileComp.state.finished = true;
          }

          if (Debug.VISUALIZE_DWT_RESULT) {
            Raster raster;
            if (sampleBuffer.intSamples != null)
              raster = Debug.intSamplesToRaster(sampleBuffer.intSamples, sampleBuffer.scanline, tileComp.comp.isSigned,
                  0);
            else {
              float max = Float.MIN_VALUE, min = Float.MAX_VALUE;
              for (final float f : sampleBuffer.floatSamples) {
                max = Math.max(f, max);
                min = Math.min(f, min);
              }
              
              if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("range after IDWT, min=" + min + ", max=" + max + ", c=" + tileComp.comp.idx + ", l="
                    + resolution.resLevel);
              }
              
              final float scale = max != min ? 255f / (max - min) : 1;
              final float offset = -min;

              raster = Debug.floatSamplesToRaster(sampleBuffer.floatSamples, sampleBuffer.scanline, offset, scale);
            }
            PartialResultsDebugger.getInstance().addDebugResult(raster, BufferedImage.TYPE_BYTE_GRAY,
                "after IDWT, c=" + tileComp.comp.idx + ", l=" + resolution.resLevel);
          }

          nextStage.receive(resolution, parameters);

          if (Debug.RECORD_DWT_FILTER_TIME) {
            Debug.printTimeNanoBased(aggregator, "DWT", System.out);
          }
        } catch (final JPEG2000Exception e) {
          throw e;
        } catch (final Exception e) {
          throw new JPEG2000Exception(GeneralMessages.WRAPPED_EXCEPTION, e);
        }
      }

      resolution.free();
    }
  }

  private DecodeTask<Resolution> createFilterTask(Resolution resolution, TileComponent tileComp,
      DummyDataBuffer sampleBuffer) throws JPEG2000Exception {

    DecodeTask<Resolution> task = null;
    if (tileComp.reversible && tileComp.kernelId == COx.VALUE_KERNEL_5_3) {
      task = new Inverse5x3FilterTask(sampleBuffer, resolution);
    } else if (!tileComp.reversible && tileComp.kernelId == COx.VALUE_KERNEL_9_7) {
      task = new Inverse9x7FilterTask(sampleBuffer, resolution);
    }

    if (task == null) {
      // There was a request for a kernel currently not implemented
      throw new JPEG2000Exception(CodestreamMessages.UNSUPPORTED_CODESTREAM_FEATURE, "kernel: " + tileComp.kernelId);
    }

    if (Debug.RECORD_DWT_FILTER_TIME) {
      task = new TimeMeasuringTask<>(task, aggregator);
    }

    return task;
  }

}
