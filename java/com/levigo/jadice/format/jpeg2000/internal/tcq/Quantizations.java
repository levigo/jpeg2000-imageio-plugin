package com.levigo.jadice.format.jpeg2000.internal.tcq;

import static com.levigo.jadice.format.jpeg2000.internal.Debug.PROTOCOL_QUANTITAZION;
import static com.levigo.jadice.format.jpeg2000.internal.Debug.quantizationProtocol;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.format.jpeg2000.internal.buffer.DummyDataBuffer;
import com.levigo.jadice.format.jpeg2000.internal.image.BandPrecinct;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlockState;
import com.levigo.jadice.format.jpeg2000.internal.image.Component;
import com.levigo.jadice.format.jpeg2000.internal.image.Pair;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.Subband;

/**
 * Helper methods for Tile Component Quantization
 */
public class Quantizations {

  /** An arbitrarily chosen reconstruction parameter as per E.1.1.2 */
  private static final float RECONSTRUCTION_PARAMETER = .5f;

  @Refer(to = Spec.J2K_CORE, page = 96, section = "Annex E.1.2", called = "Reversible Transformation")
  public static void inverseReversible(CodeBlock block, DummyDataBuffer sampleBuffer) {
    final CodeBlockState state = block.state;
    if (block.numPassesTotal == 0) {
      return;
    }
    final BandPrecinct bandPrecinct = block.bandPrecinct;
    final Subband band = bandPrecinct.subband;

    final Region region = block.region().relativeTo(band.resolution.region());
    final Pair blockPos = region.pos;
    final Pair blockSize = region.size;

    final int scanline = sampleBuffer.scanline;
    final int dstScanlineGap = scanline - blockSize.x;
    final int dstOffsetX = blockPos.x;
    final int dstOffsetY = blockPos.y;

    final int downshift = 31 - band.Kmax;

    if (PROTOCOL_QUANTITAZION) {
      quantizationProtocol().downshift(downshift);
    }

    final int[] dst = sampleBuffer.intSamples;
    final int[] src = state.sampleBuffer;

    int dstIdx = dstOffsetY * scanline + dstOffsetX;
    int srcIdx = 0;

    for (int y = 0; y < blockSize.y; y++, dstIdx += dstScanlineGap) {
      for (int x = 0; x < blockSize.x; x++, dstIdx++, srcIdx++) {
        final int srcVal = src[srcIdx];

        if (PROTOCOL_QUANTITAZION) {
          quantizationProtocol().valueBefore(srcVal);
        }

        if (srcVal < 0) {
          dst[dstIdx] = -((srcVal & Integer.MAX_VALUE) >> downshift);
        } else {
          dst[dstIdx] = srcVal >> downshift;
        }

        if (PROTOCOL_QUANTITAZION) {
          quantizationProtocol().valueAfter(dst[dstIdx]);
        }
      }
    }
  }

  @Refer(to = Spec.J2K_CORE, page = 95, section = "Annex E.1.1", called = "Irreversible Transformation")
  public static void inverseIrreversible(CodeBlock block, DummyDataBuffer sampleBuffer) {
    final CodeBlockState state = block.state;
    final BandPrecinct bandPrecinct = block.bandPrecinct;
    final Subband band = bandPrecinct.subband;

    // FIXME: is there a shorter path?
    final Component component = band.resolution.tileComp.comp;

    final Region region = block.region().relativeTo(band.resolution.region());
    final Pair blockPos = region.pos;
    final Pair blockSize = region.size;

    final int scanlineStride = sampleBuffer.scanline;
    final int dstScanlineGap = scanlineStride - blockSize.x;
    final int dstOffsetX = blockPos.x;
    final int dstOffsetY = blockPos.y;

    final float[] dst = sampleBuffer.floatSamples;
    final int[] src = state.sampleBuffer;

    final int downshift = 31 - band.Kmax;

    if (PROTOCOL_QUANTITAZION) {
      quantizationProtocol().downshift(downshift);
    }

    int dstIdx = dstOffsetY * scanlineStride + dstOffsetX;
    int srcIdx = 0;

    // See E.1.1.1, E-3
    final float quantizationStepSize = // Δb
    (float) (Math.pow(2, (float) (component.precision + band.type.log2gain /* -1 ? */) - band.exponent) // 2^(Rb-εb)
    * (1f + (float) band.mantissa / (1 << 11))); // 1 + μb / 2^11

    // step size
    final int Mb = band.guardBits + band.exponent - 1; // number of bit-planes
    final int Nb = Mb; // number of decoded bit-planes; FIXME: need to deal with partial decoding?

    final int partialDecodingMagnitude = 1 << Mb - Nb; // will always be one for now, see above
    final float reconstructionStepSize = partialDecodingMagnitude * RECONSTRUCTION_PARAMETER;

    for (int y = 0; y < blockSize.y; y++, dstIdx += dstScanlineGap) {
      for (int x = 0; x < blockSize.x; x++, dstIdx++, srcIdx++) {
        final int srcVal = src[srcIdx];
        // E.1.1.2, E-6
        if (srcVal > 0) {
          dst[dstIdx] = ((srcVal >> downshift) + reconstructionStepSize) * quantizationStepSize;
        } else if (srcVal < 0) {
          dst[dstIdx] = -(((srcVal & Integer.MAX_VALUE) >> downshift) + reconstructionStepSize) * quantizationStepSize;
        } else {
          dst[dstIdx] = 0;
        }
      }
    }
  }
}
