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
package com.levigo.jadice.format.jpeg2000.internal.image;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.levigo.jadice.format.jpeg2000.internal.image.Regions.asRectangle;

public class Visualizer {

  public static void visializeElements(Codestream codestream, BufferedImage image, int comp) throws JPEG2000Exception {
    Graphics2D g2d = image.createGraphics();
    g2d.setStroke(new BasicStroke(1));
    Pair numTiles = codestream.numTiles;

    for (int ty = 0; ty < numTiles.y; ty++) {
      for (int tx = 0; tx < numTiles.x; tx++) {
        final Tile tile = codestream.tiles[ty * numTiles.x + tx];

        final TileComponent tileComp = tile.accessTileComp(comp);
        final Region tileCompRegion = tileComp.region().absolute();
        for(int r = 0; r < tileComp.numResolutions();r++) {
          final Resolution resolution = tileComp.accessResolution(r);
          final Region resolutionRegion = resolution.region().absolute();

          final SubbandType[] subbandTypes = resolution.subbandTypes();
          for (SubbandType type : subbandTypes) {
            final Subband subband = resolution.accessSubband(type);
            final Region bandRegion = subband.region().absolute();

            for (int p = 0; p < resolution.numPrecinctsTotal; p++) {
              final BandPrecinct bandPrecinct = subband.accessBandPrecinct(p);
              final Region bandPrecinctRegion = bandPrecinct.region().absolute();

              for (int y = 0; y < bandPrecinct.numBlocks.y; y++) {
                for (int x = 0; x < bandPrecinct.numBlocks.x; x++) {
                  final CodeBlock block = bandPrecinct.accessCodeBlock(new Pair(x, y));
                  final Region blockRegion = block.region().absolute();
                  g2d.setColor(Color.yellow);
                  g2d.draw(new Rectangle(
                      bandRegion.pos.x + blockRegion.pos.x,
                      bandRegion.pos.y + blockRegion.pos.y,
                      blockRegion.size.x, blockRegion.size.y
                  ));
                }

                g2d.setColor(Color.red);
                g2d.draw(new Rectangle(
                    bandRegion.pos.x + bandPrecinctRegion.pos.x,
                    bandRegion.pos.y + bandPrecinctRegion.pos.y,
                    bandPrecinctRegion.size.x, bandPrecinctRegion.size.y
                ));
              }
            }

            g2d.setColor(Color.orange);
            g2d.draw(asRectangle(bandRegion));
          }

          g2d.setColor(Color.green);
          g2d.draw(asRectangle(resolutionRegion));
        }

        g2d.setColor(Color.blue);
        g2d.draw(asRectangle(tileCompRegion));
      }
    }

    g2d.dispose();
  }

}
