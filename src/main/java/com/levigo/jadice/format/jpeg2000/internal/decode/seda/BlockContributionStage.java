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
import com.levigo.jadice.format.jpeg2000.internal.codestream.BlockContribution;
import com.levigo.jadice.format.jpeg2000.internal.tier1.BlockContributionTask;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.LongAggregator;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.TimeMeasuringTask;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.msg.CompressionMessages;

// part of an experiment. Currently not in use.
public class BlockContributionStage extends ConfigurableStage
    implements
      Transformer<BlockContribution, CodeBlock, JPEG2000Exception> {
  private LongAggregator aggregator;

  public BlockContributionStage() {
    if (Debug.RECORD_EBCOT_TIME) {
      aggregator = new LongAggregator();
    }
  }

  @Override
  public void transform(BlockContribution blockContribution,
      Consumer<? super CodeBlock, ? extends JPEG2000Exception> next) throws JPEG2000Exception {
    DecodeTask<CodeBlock> task = new BlockContributionTask(blockContribution);

    if (Debug.RECORD_EBCOT_TIME) {
      task = new TimeMeasuringTask<>(task, aggregator);
    }

    try {
      next.consume(task.call());
    } catch (final Exception e) {
      throw new JPEG2000Exception(CompressionMessages.FAILED_BLOCK_CONTRIBUTION_DECODING);
    }

    if (Debug.RECORD_DWT_FILTER_TIME) {
      Debug.printTimeNanoBased(aggregator, "EBCoT", System.out);
    }
  }
}