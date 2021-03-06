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
import org.jadice.jpeg2000.internal.color.InverseIrreversibleMCTTask;
import org.jadice.jpeg2000.internal.color.InverseLevelShiftTask;
import org.jadice.jpeg2000.internal.color.InverseReversibleMCTTask;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.decode.push.DecodeTask;
import org.jadice.jpeg2000.internal.decode.push.Receiver;
import org.jadice.jpeg2000.internal.image.Resolution;
import org.jadice.jpeg2000.internal.image.Tile;
import org.jadice.jpeg2000.internal.image.TileComponent;
import org.jadice.jpeg2000.internal.marker.COx;
import org.jadice.jpeg2000.msg.GeneralMessages;

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
