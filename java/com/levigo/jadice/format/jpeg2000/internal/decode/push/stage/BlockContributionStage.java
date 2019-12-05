package com.levigo.jadice.format.jpeg2000.internal.decode.push.stage;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.codestream.BlockContribution;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.image.Component;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;
import com.levigo.jadice.format.jpeg2000.internal.image.Subband;
import com.levigo.jadice.format.jpeg2000.internal.tier1.BlockContributionTask;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.LongAggregator;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.TimeMeasuringTask;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.tier1.Passes;
import com.levigo.jadice.format.jpeg2000.msg.CompressionMessages;

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
