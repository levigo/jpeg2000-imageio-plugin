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