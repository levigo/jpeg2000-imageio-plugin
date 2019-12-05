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
import org.jadice.jpeg2000.internal.Debug;
import org.jadice.jpeg2000.internal.codestream.BlockContribution;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.DecodeTask;
import org.jadice.jpeg2000.internal.decode.push.LongAggregator;
import org.jadice.jpeg2000.internal.decode.push.Receiver;
import org.jadice.jpeg2000.internal.decode.push.TimeMeasuringTask;
import org.jadice.jpeg2000.internal.image.CodeBlock;
import org.jadice.jpeg2000.internal.image.Component;
import org.jadice.jpeg2000.internal.image.Resolution;
import org.jadice.jpeg2000.internal.image.Subband;
import org.jadice.jpeg2000.internal.tier1.BlockContributionTask;
import org.jadice.jpeg2000.internal.tier1.Passes;
import org.jadice.jpeg2000.msg.CompressionMessages;

public class BlockContributionStage implements Receiver<BlockContribution> {

  private final Receiver<CodeBlock> nextStage;

  private LongAggregator aggregator;
  
  public BlockContributionStage(Receiver<CodeBlock> nextStage) {
    this.nextStage = nextStage;
    
    if(Debug.RECORD_EBCOT_TIME) {
      aggregator = new LongAggregator();
    }
  }

  @Override
  public void receive(BlockContribution blockContribution, DecoderParameters parameters) throws JPEG2000Exception {
    if (blockContribution.passes == 0 && !blockContribution.isLastLayer) {
      return;
    }

    final CodeBlock block = blockContribution.block;
    if (block.state == null) {
      block.start(parameters);
    }

    if (blockContribution.passes == 0 && blockContribution.isLastLayer) {
      block.state.finished = true;
    } else {
      final DecodeTask<CodeBlock> task = createTask(blockContribution);

      try {
        final CodeBlock refinedBlock = task.call();
        assert block == refinedBlock;
      } catch (Exception e) {
        throw new JPEG2000Exception(CompressionMessages.FAILED_BLOCK_CONTRIBUTION_DECODING, e);
      }
    }

    if (block.state.finished) {
      if (Debug.VISUALIZE_BLOCK_RESULT) {
        final int[] samples = block.state.sampleBuffer;
        final Subband subband = block.bandPrecinct.subband;
        final Resolution resolution = subband.resolution;
        final Component comp = resolution.tileComp.comp;
        Passes.addDebugResult(null, block, subband.type, samples, resolution.resLevel, comp.idx);
      }
      
      nextStage.receive(block, parameters);
    }
  }
  
  public DecodeTask<CodeBlock> createTask(BlockContribution blockContribution) {
    final DecodeTask<CodeBlock> task = new BlockContributionTask(blockContribution);

    if (Debug.RECORD_EBCOT_TIME) {
      return new TimeMeasuringTask<>(task, aggregator);
    }

    return task;
  }

}
