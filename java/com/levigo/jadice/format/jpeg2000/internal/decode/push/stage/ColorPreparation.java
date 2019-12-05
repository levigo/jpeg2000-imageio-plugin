package com.levigo.jadice.format.jpeg2000.internal.decode.push.stage;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.DecodeTask;
import com.levigo.jadice.format.jpeg2000.internal.color.InverseIrreversibleMCTTask;
import com.levigo.jadice.format.jpeg2000.internal.color.InverseLevelShiftTask;
import com.levigo.jadice.format.jpeg2000.internal.color.InverseReversibleMCTTask;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponent;
import com.levigo.jadice.format.jpeg2000.internal.marker.COx;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;

public class ColorPreparation implements Receiver<Resolution> {
  private final Receiver<Tile> nextStage;

  public ColorPreparation(Receiver<Tile> nextStage) {
    this.nextStage = nextStage;
  }

  @Override
  public void receive(Resolution resolution, DecoderParameters parameters) throws JPEG2000Exception {
    // FIXME: who, exactly is responsible for the state initialization?

    final TileComponent tileComp = resolution.tileComp;
    final Tile tile = tileComp.tile;
    
    if (null == tile.state) {
      tile.start(parameters);
    }

    // Check if tile state claims that all tile-components are ready
    if (tile.state.remainingComps.decrementAndGet() == 0) {
      final DecodeTask<Tile> task = createTask(tileComp);
      if (task != null) {
        try {
          task.call();
        } catch (final JPEG2000Exception e) {
          throw e;
        } catch (final Exception e) {
          throw new JPEG2000Exception(GeneralMessages.WRAPPED_EXCEPTION, e);
        }
      }
      nextStage.receive(tile, parameters);
    }
  }
  
  private DecodeTask<Tile> createTask(TileComponent tileComp) {
    final Tile tile = tileComp.tile;
    
    if (tile.useMCT && tile.codestream.numComps >= 3) {
      if (tileComp.reversible && tileComp.kernelId == COx.VALUE_KERNEL_5_3) {
        return new InverseReversibleMCTTask(tile);
      } else if (!tileComp.reversible && tileComp.kernelId == COx.VALUE_KERNEL_9_7) {
        return new InverseIrreversibleMCTTask(tile);
      }
    } else {
      // perform stand-alone level-shifting
      return new InverseLevelShiftTask(tile);
    }
    
    return null;
  }

}
