package com.levigo.jadice.format.jpeg2000.internal.tier1;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import com.levigo.jadice.document.internal.render.debug.PartialResultsDebugger;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.Subband;
import com.levigo.jadice.format.jpeg2000.internal.image.SubbandType;

public class Passes {

  static final SignificanceRawPass SIGNIFICANCE_RAW_PASS = new SignificanceRawPass();
  static final SignificancePass SIGNIFICANCE_PASS = new SignificancePass();

  public static void addDebugResult(Pass pass, CodeBlock block, SubbandType type, int[] samples, int r, int c) {
    final Subband band = block.bandPrecinct.subband;
    final Region region = block.region().absolute();
    final boolean isSigned = band.resolution.tileComp.comp.isSigned;
    final int downshift = 31 - band.Kmax;
    final Raster raster = Debug.intSamplesToRaster(samples, region.size.x, isSigned, downshift);

    final String passName = pass != null ? pass.getClass().getSimpleName() : "Final";
    final String name = passName + " "
        + "res=" + band.resolution.resLevel + " "
        + "band=" + type + " "
        + "block=" + block.indices + " "
        + "stripe=" + (block.numStripes - r) + " "
        + "column=" + c;
    
    // Debug.dump(raster, BufferedImage.TYPE_BYTE_GRAY, name);
    PartialResultsDebugger.getInstance().addDebugResult(raster, BufferedImage.TYPE_BYTE_GRAY, name);
  }
}
