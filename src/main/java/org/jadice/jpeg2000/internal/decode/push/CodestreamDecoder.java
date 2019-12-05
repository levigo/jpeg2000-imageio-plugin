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
package org.jadice.jpeg2000.internal.decode.push;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import com.levigo.jadice.document.internal.mm.CompositeKey;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.decode.Decoder;
import org.jadice.jpeg2000.internal.decode.DecoderParameters;
import org.jadice.jpeg2000.internal.image.Region;
import org.jadice.util.mm.Cache;
import org.jadice.util.mm.CacheManager;

public class CodestreamDecoder implements Decoder {

  private final Codestream codestream;
  
  public CodestreamDecoder(Codestream codestream) {
    this.codestream = codestream;
  }

  @Override
  public Raster decode(DecoderParameters targetParameters) throws JPEG2000Exception {
    final DecoderParameters decoderParameters = optimizeRequest(targetParameters);
    
    final CompositeKey cacheKey = new CompositeKey(codestream, decoderParameters);
    final Cache cache = CacheManager.getDefault();
    WritableRaster raster = (WritableRaster) cache.get(cacheKey);

    if (raster == null) {
      final PushPipeline pipeline = new PushPipeline();
      raster = pipeline.start(codestream, decoderParameters);
      cache.put(cacheKey, raster, raster.getDataBuffer().getSize());
    }

    if (decoderParameters != targetParameters) {
      final Region region = targetParameters.region;
      final int minX = region.x0();
      final int minY = region.y0();
      final int width = region.width();
      final int height = region.height();
      final int[] bands = new int[raster.getNumBands()];
      for (int i = 0; i < raster.getNumBands(); i++) {
        bands[i] = i;
      }
      return raster.createChild(minX, minY, width, height, minX, minY, bands); 
    } else {
      return raster;
    }
  }

  private DecoderParameters optimizeRequest(DecoderParameters targetParameters) {
    if (codestream.numTiles.x == 1 && codestream.numTiles.y == 1) {
      final DecoderParameters decoderParameters = new DecoderParameters();
      decoderParameters.validate = targetParameters.validate;
      decoderParameters.region = codestream.region().absolute();
      return decoderParameters;
    } else {
      return targetParameters;
    }
  }

}
