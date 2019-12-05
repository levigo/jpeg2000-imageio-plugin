package com.levigo.jadice.format.jpeg2000.internal;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import com.levigo.jadice.format.jpeg2000.internal.debug.dwt.DWTProtocol;
import com.levigo.jadice.format.jpeg2000.internal.debug.tcq.QuantizationProtocol;
import com.levigo.jadice.format.jpeg2000.internal.debug.tier1.BlockCodingProtocol;
import com.levigo.jadice.format.jpeg2000.internal.debug.tier2.PacketHeaderProtocol;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.MeasurementAggregator;

/**
 * We use constants to enable or disable debugging flags. If constants are found the compiler is
 * able to optimize heavily. If a constant ({@code static final}) boolean is set to {@code false}
 * and surrounds a block of source code the compiler knows that the complete block can be omitted.
 * This avoids the source code for informational or debugging purposes to be compiled in an
 * productive environment. The defined flags should always be {@code false} before a release build
 * is started.
 */
public class Debug {

  /** Flag to enable or disable logging while reading JP2 or JPX file format. */
  public static final boolean LOG_FILEFORMAT_READ = false;

  /** Flag to enable or disable logging of packet header decoding. */
  public static final boolean LOG_PACKET_HEADER_READ = false;

  /** Flag to enable or disable logging of marker and marker segment info while reading. */
  public static final boolean LOG_MARKER_READ = false;

  /** Flag to enable or disable logging of marker and marker segment info while writing. */
  public static final boolean LOG_MARKER_WRITE = false;

  /** Flag to enable or disable logging of progression order information. */
  public static final boolean LOG_PROGRESSION_ORDER = false;

  /** Flag to enable or disable painting element bounds into resulting image. */
  public static final boolean VISUALIZE_ELEMENT_BOUNDS = false;

  /** Flag to enable or disable detailed visual depiction of EBCoT passes. */
  public static final boolean VISUALIZE_EBCOT_PASSES = false;

  /** Flag to enable or disable visual depiction of EBCoT results. */
  public static final boolean VISUALIZE_BLOCK_RESULT = false;

  /** Flag to enable or disable detailed visual depiction of inverse quantization step. */
  public static final boolean VISUALIZE_QUANTIZATION = false;

  /** Flag to enable or disable detailed visual depiction of IDWT steps. */
  public static final boolean VISUALIZE_DWT = false;

  /** Flag to enable or disable visual depiction of IDWT results. */
  public static final boolean VISUALIZE_DWT_RESULT = false;

  /** Flag to enable or disable tracking of time spent in EBCoT step. */
  public static final boolean RECORD_EBCOT_TIME = false;

  /** Flag to enable or disable tracking of time spent in DWT filtering. */
  public static final boolean RECORD_DWT_FILTER_TIME = false;

  /** Flag to enable or disable the packet header protocol for tracing purposes. */
  public static final boolean PROTOCOL_PACKET_HEADER = false;

  /** Flag to enable or disable the block coding protocol for tracing purposes. */
  public static final boolean PROTOCOL_EBCOT = false;

  /** Flag to enable or disable the quantization protocol for tracing purposes. */
  public static final boolean PROTOCOL_QUANTITAZION = false;

  /** Flag to enable or disable the DWT protocol for tracing purposes. */
  public static final boolean PROTOCOL_DWT = false;

  public static void printTimeNanoBased(MeasurementAggregator<Long> aggregator, String identifier, OutputStream out) {
    final Long durationNanos = aggregator.get();
    final Long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);
    final String durationString = durationMillis + " ms (" + durationNanos + " ns)";
    final String finalString = "Current time spent in " + identifier + ": " + durationString + "\n";
    try {
      out.write(finalString.getBytes());
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static File DUMP_DIR = null;

  public static void dump(Raster raster, int imageType, String name) {
    if (DUMP_DIR == null) {
      throw new IllegalStateException("DUMP_DIR must be set!");
    }

    DUMP_DIR.mkdirs();
    final File file = new File(DUMP_DIR, nextName(name));
    FileImageOutputStream output = null;
    try {
      file.createNewFile();
      final Iterator<ImageWriter> pngWriters = ImageIO.getImageWritersByFormatName("PNG");
      if (!pngWriters.hasNext())
        throw new RuntimeException("Can't find ImageWriter for PNG");
      final ImageWriter pngWriter = pngWriters.next();
      output = new FileImageOutputStream(file);
      pngWriter.setOutput(output);
      final BufferedImage bufferedImage = new BufferedImage(raster.getWidth(), raster.getHeight(), imageType);
      bufferedImage.setData(raster);
      pngWriter.write(bufferedImage);
    } catch (final IOException e) {
      e.printStackTrace();
    } finally {
      if (output != null) {
        try {
          output.flush();
          output.close();
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }

    System.out.println("Written " + file.getAbsolutePath());
  }

  private static final AtomicInteger GLOBAL_COUNTER = new AtomicInteger(0);

  private static String nextName(String identifier) {
    identifier = identifier.replace(" ", "_");
    final int count = GLOBAL_COUNTER.getAndIncrement();
    return String.format("img%07d.%s.png", count, identifier);
  }

  public static Raster intSamplesToRaster(int[] samples, int scanline) {
    return intSamplesToRaster(samples, scanline, true, 0);
  }

  public static Raster intSamplesToRaster(int[] samples, int scanline, boolean signed, int downshift) {
    final int length = samples.length;
    final int w = scanline;
    final int h = length / scanline;
    final WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, w, h, 1, new Point(0, 0));

    for (int y = 0, i = 0; y < h; y++) {
      for (int x = 0; x < w; x++, i++) {
        final int sample = samples[i] >> downshift;
        raster.setSample(x, y, 0, signed ? sample + 128 : sample); // +128 for level shifting
      }
    }

    return raster;
  }

  public static Raster floatSamplesToRaster(float[] samples, int scanline) {
    return floatSamplesToRaster(samples, scanline, 0f, 1f);
  }

  public static Raster floatSamplesToRaster(float[] samples, int scanline, float offset, float scale) {
    final int length = samples.length;
    final int w = scanline;
    final int h = length / scanline;
    final WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, w, h, 1, new Point(0, 0));

    final float buffer[] = new float[scanline];

    for (int y = 0; y < h; y++) {
      System.arraycopy(samples, y * scanline, buffer, 0, scanline);

      if (offset != 0) {
        for (int j = 0; j < buffer.length; j++) {
          buffer[j] += offset;
        }
      }
      if (scale != 1f) {
        for (int j = 0; j < buffer.length; j++) {
          buffer[j] *= scale;
        }
      }

      raster.setSamples(0, y, scanline, 1, 0, buffer);
    }

    return raster;
  }

  private static void validateProtocolEnabling(boolean protocolEnabled) {
    if (!protocolEnabled) {
      throw new IllegalStateException("protocol not enabled but injected or requested");
    }
  }

  private static PacketHeaderProtocol PACKET_HEADER_PROTOCOL_INSTANCE;

  public static void installPacketHeaderProtocol(PacketHeaderProtocol protocol) {
    validateProtocolEnabling(PROTOCOL_PACKET_HEADER);
    PACKET_HEADER_PROTOCOL_INSTANCE = protocol;
  }

  public static PacketHeaderProtocol packetHeaderProtocol() {
    validateProtocolEnabling(PROTOCOL_PACKET_HEADER);
    return PACKET_HEADER_PROTOCOL_INSTANCE;
  }

  private static BlockCodingProtocol BLOCK_CODING_PROTOCOL_INSTANCE;

  public static void installBlockCodingProtocol(BlockCodingProtocol protocol) {
    validateProtocolEnabling(PROTOCOL_EBCOT);
    BLOCK_CODING_PROTOCOL_INSTANCE = protocol;
  }

  public static BlockCodingProtocol blockCodingProtocol() {
    validateProtocolEnabling(PROTOCOL_EBCOT);
    return BLOCK_CODING_PROTOCOL_INSTANCE;
  }

  private static QuantizationProtocol QUANTIZATION_PROTOCOL_INSTANCE;

  public static void installQuantizationProtocol(QuantizationProtocol protocol) {
    validateProtocolEnabling(PROTOCOL_QUANTITAZION);
    QUANTIZATION_PROTOCOL_INSTANCE = protocol;
  }

  public static QuantizationProtocol quantizationProtocol() {
    validateProtocolEnabling(PROTOCOL_QUANTITAZION);
    return QUANTIZATION_PROTOCOL_INSTANCE;
  }

  private static DWTProtocol DWT_PROTOCOL_INSTANCE;

  public static void installDWTProtocol(DWTProtocol protocol) {
    validateProtocolEnabling(PROTOCOL_DWT);
    DWT_PROTOCOL_INSTANCE = protocol;
  }

  public static DWTProtocol dwtProtocol() {
    validateProtocolEnabling(PROTOCOL_DWT);
    return DWT_PROTOCOL_INSTANCE;
  }

}
