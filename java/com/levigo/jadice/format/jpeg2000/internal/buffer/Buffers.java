package com.levigo.jadice.format.jpeg2000.internal.buffer;

import java.util.Arrays;

import com.levigo.jadice.format.jpeg2000.internal.image.GridRegion;
import com.levigo.jadice.format.jpeg2000.internal.image.Pair;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponent;

public class Buffers {

  public static DummyDataBuffer createTileComponentBuffer(TileComponent tileComp) {
    final GridRegion gridElement = tileComp.region();
    
    // We need tile-component region's size only. Position is meaningless.
    final Region region = gridElement.absolute(); 
    
    final Pair tileCompSize = region.size;
    final DummyDataBuffer buffer = new DummyDataBuffer();
    buffer.scanline = tileCompSize.x;
    if (tileComp.reversible) {
      buffer.intSamples = new int[tileCompSize.y * tileCompSize.x];
    } else {
      buffer.floatSamples = new float[tileCompSize.y * tileCompSize.x];
    }
    return buffer;
  }
  
  public static int[] crop(int[] intSamples, int startX, int startY, int width, int height, int scanline) {
    final int[] ints = new int[width * height];

    int srcStartX = (startY * scanline) + startX;
    for (int y = startY, i = 0; y < startY + height; y++, srcStartX += scanline) {
      for (int j = srcStartX; j < srcStartX + width; i++, j++) {
        ints[i] = intSamples[j];
      }
    }

    return ints;
  }

  public static float[] crop(float[] floatSamples, int startX, int startY, int width, int height, int scanline) {
    final float[] floats = new float[width * height];

    int srcStartX = (startY * scanline) + startX;
    for (int y = startY, i = 0; y < startY + height; y++, srcStartX += scanline) {
      for (int j = srcStartX; j < srcStartX + width; i++, j++) {
        floats[i] = floatSamples[j];
      }
    }

    return floats;
  }

  public static int[] copyVertical(int[] samples, int from, int numSamples, int scanline) {
    if (numSamples < 0) {
      throw new IllegalArgumentException("" + numSamples);
    }
    
    final int[] copy = new int[numSamples];
    for(int i = 0, j = from; i < numSamples; i++, j+=scanline) {
      copy[i] = samples[j];
    }
    return copy;
  }
 
  public static float[] copyVertical(int[] samples, int from, int numSamples, int scanline, float scaleFactor) {
    if (numSamples < 0) {
      throw new IllegalArgumentException("" + numSamples);
    }
    
    final float[] copy = new float[numSamples];
    for(int i = 0, j = from; i < numSamples; i++, j+=scanline) {
      copy[i] = samples[j] * scaleFactor;
    }
    return copy;
  }

  public static int[] copyHorizontal(int[] samples, int from, int to) {
    return Arrays.copyOfRange(samples, from, to);
  }
  
  public static float[] copyHorizontal(int[] samples, int from, int to, float scaleFactor) {
    final int newLength = Math.min(samples.length - from, to - from);
    final float[] copy = new float[newLength];

    for (int i = 0, j = from; i < newLength; i++, j++) {
      copy[i] = samples[j] * scaleFactor;
    }
    
    return copy;
  }
  
  public static float[] copyVertical(float[] samples, int from, int numSamples, int scanline, float scaleFactor) {
    if (numSamples < 0) {
      throw new IllegalArgumentException("" + numSamples);
    }
    
    final float[] copy = new float[numSamples];
    for(int i = 0, j = from; i < numSamples; i++, j+=scanline) {
      copy[i] = samples[j] * scaleFactor;
    }
    return copy;
  }
  
  public static float[] copyHorizontal(float[] samples, int from, int to, float scaleFactor) {
    final int newLength = Math.min(samples.length - from, to - from);
    final float[] copy = new float[newLength];

    for (int i = 0, j = from; i < newLength; i++, j++) {
      copy[i] = samples[j] * scaleFactor;
    }
    
    return copy;
  }

  public static int[] copyVertical(int[] samples, int from, int numSamples, int scanline, int scaleFactor) {
    if (numSamples < 0) {
      throw new IllegalArgumentException("" + numSamples);
    }

    final int[] copy = new int[numSamples];
    for (int i = 0, j = from; i < numSamples; i++, j += scanline) {
      copy[i] = samples[j] * scaleFactor;
    }
    return copy;
  }

  public static int[] copyHorizontal(int[] samples, int from, int to, int scaleFactor) {
    final int newLength = Math.min(samples.length - from, to - from);
    final int[] copy = new int[newLength];

    for (int i = 0, j = from; i < newLength; i++, j++) {
      copy[i] = samples[j] * scaleFactor;
    }

    return copy;
  }

  public static int[] diff(final int[] a, final int[] b) {
    final int diff[] = new int[b.length];
    for (int j = 0; j < diff.length; j++) {
      diff[j] = a[j] - b[j];
    }
    return diff;
  }

  public static float[] diff(final int[] a, final float[] b) {
    final float diff[] = new float[b.length];
    for (int j = 0; j < diff.length; j++) {
      diff[j] = a[j] - b[j];
    }
    return diff;
  }

  public static void dumpSamples(int[] sampleBuffer, int rows, int columns) {
    dumpSamples(sampleBuffer, rows, columns, 5);
  }
  
  public static void dumpSamples(int[] sampleBuffer, int rows, int columns, int cellWidth) {
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        System.out.printf("%" + cellWidth + "d ", sampleBuffer[r * columns + c]);
      }
      System.out.println();
    }
  }

  public static String samples(int[] sampleBuffer, int rows, int columns, int cellWidth) {
    final StringBuilder stringBuilder = new StringBuilder();
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        stringBuilder.append(String.format("%" + cellWidth + "d ", sampleBuffer[r * columns + c]));
      }
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }

  public static void dumpSamples(float[] sampleBuffer, int rows, int columns) {
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        System.out.printf("%5.1f ", sampleBuffer[r * columns + c]);
      }
      System.out.println();
    }
  }

}
