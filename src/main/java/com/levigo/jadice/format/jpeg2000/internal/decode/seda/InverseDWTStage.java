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
package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.dwt.Inverse5x3FilterTask;
import com.levigo.jadice.format.jpeg2000.internal.dwt.Inverse9x7FilterTask;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.LongAggregator;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.TimeMeasuringTask;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponent;
import com.levigo.jadice.format.jpeg2000.internal.marker.COx;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;

// part of an experiment. Currently not in use.
public class InverseDWTStage extends ConfigurableStage implements Transformer<CodeBlock, Resolution, JPEG2000Exception> {

  private LongAggregator aggregator;

  public InverseDWTStage() {
    if (Debug.RECORD_DWT_FILTER_TIME) {
      aggregator = new LongAggregator();
    }
  }

  @Override
  public void transform(CodeBlock codeBlock, Consumer<? super Resolution, ? extends JPEG2000Exception> next)
      throws JPEG2000Exception {
    // First, collect all code-blocks until a code-block row is completed.
    final Resolution resolution = codeBlock.bandPrecinct.subband.resolution;
    if (resolution.state.blocksLeft.decrementAndGet() == 0) {

      if (resolution.resLevel > 0) {
        final TileComponent tileComp = resolution.tileComp;
        final DummyDataBuffer sampleBuffer = tileComp.state.sampleBuffer;

        final DecodeTask<Resolution> task = createFilterTask(resolution, tileComp, sampleBuffer);

        try {
          next.consume(task.call());
        } catch (final Exception e) {
          throw new JPEG2000Exception(GeneralMessages.WRAPPED_EXCEPTION, e);
        }

        if (Debug.RECORD_DWT_FILTER_TIME) {
          Debug.printTimeNanoBased(aggregator, "DWT", System.out);
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
