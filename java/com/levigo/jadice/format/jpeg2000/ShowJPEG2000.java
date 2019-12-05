package com.levigo.jadice.format.jpeg2000;

import static com.levigo.jadice.format.jpeg2000.internal.Debug.PROTOCOL_DWT;
import static com.levigo.jadice.format.jpeg2000.internal.Debug.PROTOCOL_EBCOT;
import static com.levigo.jadice.format.jpeg2000.internal.Debug.PROTOCOL_PACKET_HEADER;
import static com.levigo.jadice.format.jpeg2000.internal.Debug.PROTOCOL_QUANTITAZION;
import static com.levigo.jadice.format.jpeg2000.internal.Debug.blockCodingProtocol;
import static com.levigo.jadice.format.jpeg2000.internal.Debug.dwtProtocol;
import static com.levigo.jadice.format.jpeg2000.internal.Debug.quantizationProtocol;
import static com.levigo.jadice.format.jpeg2000.internal.image.Pair.p;
import static java.awt.Color.BLUE;
import static java.awt.Color.CYAN;
import static java.awt.Color.GREEN;
import static java.awt.Color.ORANGE;
import static java.awt.Color.RED;
import static java.awt.Color.YELLOW;
import static java.util.Collections.singleton;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.levigo.jadice.document.internal.render.debug.PartialResultsDebugger;
import com.levigo.jadice.document.io.RandomAccessFileInputStream;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestreams;
import com.levigo.jadice.format.jpeg2000.internal.debug.PrintlineProtocolListener;
import com.levigo.jadice.format.jpeg2000.internal.debug.Protocol;
import com.levigo.jadice.format.jpeg2000.internal.debug.WritingProtocolListener;
import com.levigo.jadice.format.jpeg2000.internal.debug.dwt.BasicDWTProtocol;
import com.levigo.jadice.format.jpeg2000.internal.debug.tcq.BasicQuantizationProtocol;
import com.levigo.jadice.format.jpeg2000.internal.debug.tier1.BasicBlockCodingProtocol;
import com.levigo.jadice.format.jpeg2000.internal.debug.tier2.BasicPacketHeaderProtocol;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.CodestreamDecoder;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.LongAggregator;
import com.levigo.jadice.format.jpeg2000.internal.image.BandPrecinct;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.Pair;
import com.levigo.jadice.format.jpeg2000.internal.image.Precinct;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;
import com.levigo.jadice.format.jpeg2000.internal.image.Subband;
import com.levigo.jadice.format.jpeg2000.internal.image.SubbandType;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponent;

public class ShowJPEG2000 {

  private static final int RUNS = 1;

  public static void main(String[] args) throws IOException, JPEG2000Exception {
    final String filePath = "E:\\work\\support\\jsx\\2655\\im11.j2k";
    final File file = new File(filePath);
    final RandomAccessFileInputStream source = new RandomAccessFileInputStream(file);

    final Collection<Closeable> closeables = installProtocols(filePath);

    try {
      for (int i = 0; i < RUNS; i++) {
        final Results results = decode(source);

        Debug.printTimeNanoBased(results.timeAggregator, "complete decoding of " + (i + 1) + ". run", System.out);

        if (Debug.VISUALIZE_ELEMENT_BOUNDS) {
          drawElementBounds(results.raster, results.codestream);
        }

        final int imageType = getBufferedImageType(results.codestream);
        PartialResultsDebugger.getInstance().addDebugResult(results.raster, imageType, (i + 1) + ". run");
      }
    } finally {
      closeProtocols(closeables);
    }
  }

  private static class Results {
    LongAggregator timeAggregator;
    Codestream codestream;
    Raster raster;
  }

  private static Results decode(final RandomAccessFileInputStream source) throws IOException, JPEG2000Exception {
    final Results results = new Results();
    synchronized (source) {
      source.seek(0);
      final long timestampAtStart = System.nanoTime();

      final SeekableInputStream codestreamSource = Codestreams.createCodestreamSource(source);

      results.codestream = new Codestream(codestreamSource, 0);
      results.codestream.init();

      final DecoderParameters parameters = new DecoderParameters();
      parameters.validate = false;
      parameters.region = results.codestream.region().absolute();

      final CodestreamDecoder decoder = new CodestreamDecoder(results.codestream);
      results.raster = decoder.decode(parameters);

      assert results.raster != null;
      final long timestampAtFinish = System.nanoTime();
      results.timeAggregator = new LongAggregator(timestampAtFinish - timestampAtStart);
    }
    return results;
  }

  private static int getBufferedImageType(Codestream codestream) {
    if (codestream.comps.length == 3) {
      return BufferedImage.TYPE_3BYTE_BGR;
    } else if (codestream.comps.length == 1) {
      return BufferedImage.TYPE_BYTE_GRAY;
    }
    throw new UnsupportedOperationException("codestream.comps.length=" + codestream.comps.length + " unsupported.");
  }

  private static Collection<Closeable> installProtocols(String filePath) throws IOException {
    final Collection<Closeable> closeables = new ArrayList<Closeable>();
    if (PROTOCOL_PACKET_HEADER) {
      closeables.addAll(installPacketHeaderProtocol(filePath));
    }
    if (PROTOCOL_EBCOT) {
      closeables.addAll(installBlockCodingProtocol(filePath));
    }
    if (PROTOCOL_QUANTITAZION) {
      closeables.addAll(installQuantizationProtocol(filePath));
    }
    if (PROTOCOL_DWT) {
      closeables.addAll(installDWTProtocol(filePath));
    }
    return closeables;
  }

  private static Collection<Closeable> installPacketHeaderProtocol(String filePath) throws IOException {
    final BasicPacketHeaderProtocol protocol = new BasicPacketHeaderProtocol();
    Debug.installPacketHeaderProtocol(protocol);
    return augment(protocol, filePath, "ph");
  }

  private static Collection<Closeable> installBlockCodingProtocol(String filePath) throws IOException {
    final BasicBlockCodingProtocol protocol = new BasicBlockCodingProtocol();
    Debug.installBlockCodingProtocol(protocol);
    return augment(protocol, filePath, "bc");
  }

  private static Collection<Closeable> installQuantizationProtocol(String filePath) throws IOException {
    final BasicQuantizationProtocol protocol = new BasicQuantizationProtocol();
    Debug.installQuantizationProtocol(protocol);
    return augment(protocol, filePath, "dq");
  }

  private static Collection<Closeable> installDWTProtocol(String filePath) throws IOException {
    final BasicDWTProtocol protocol = new BasicDWTProtocol();
    Debug.installDWTProtocol(protocol);
    return augment(protocol, filePath, "wv");
  }

  private static Set<Closeable> augment(Protocol protocol, String filePath, String suffix) throws IOException {
    final FileOutputStream myProtocolOutputStream = new FileOutputStream(new File(filePath + ".jad." + suffix));
//    final FileInputStream referenceProtocolInputStream = new FileInputStream(new File(filePath + ".kdu." + suffix));
    final WritingProtocolListener writingListener = new WritingProtocolListener(myProtocolOutputStream);
    protocol.addProtocolListener(writingListener);
    protocol.addProtocolListener(new PrintlineProtocolListener());
//    protocol.addProtocolListener(new CompareToReferenceProtocolListener(referenceProtocolInputStream, protocol));
    return singleton((Closeable) writingListener);
  }

  private static void closeProtocols(Collection<Closeable> closeables) throws IOException {
    // if (Debug.PROTOCOL_PACKET_HEADER) {
    // Debug.packetHeaderProtocol().finish();
    // }
    if (PROTOCOL_EBCOT) {
      blockCodingProtocol().finish();
    }
    if (PROTOCOL_QUANTITAZION) {
      quantizationProtocol().finish();
    }
    if (PROTOCOL_DWT) {
      dwtProtocol().finish();
    }
    for (Closeable closeable : closeables) {
      closeable.close();
    }
  }

  private static void drawElementBounds(Raster raster, Codestream codestream) throws IOException, JPEG2000Exception {
    final int bufferedImageType = getBufferedImageType(codestream);
    final Region region = codestream.region().absolute();
    final BufferedImage img = new BufferedImage(region.width(), region.height(), bufferedImageType);
    img.setData(raster);
    final Graphics2D canvas = img.createGraphics();

    final Pair numTiles = codestream.numTiles;
    for (int tileY = 0; tileY < numTiles.y; tileY++) {
      for (int tileX = 0; tileX < numTiles.x; tileX++) {
        final Tile tile = codestream.accessTile(p(tileX, tileY));

        for (int comp = 0; comp < codestream.numComps; comp++) {
          final TileComponent tileComponent = tile.accessTileComp(comp);

          for (int res = 0; res < tileComponent.numResolutions(); res++) {
            final Resolution resolution = tileComponent.accessResolution(res);

            for (int p = 0; p < resolution.numPrecinctsTotal; p++) {
              final Precinct precinct = resolution.accessPrecinct(p);
              final Region precRegion = precinct.region().absolute();
              final Rectangle precRect = getResizedRect(precRegion);
              canvas.setColor(YELLOW);
              canvas.draw(precRect);
            }

            for (SubbandType subbandType : resolution.subbandTypes()) {
              final Subband subband = resolution.accessSubband(subbandType);

              for (int bp = 0; bp < resolution.numPrecinctsTotal; bp++) {
                final BandPrecinct bandPrecinct = subband.accessBandPrecinct(bp);

                for (int cby = 0; cby < bandPrecinct.numBlocks.y; cby++) {
                  for (int cbx = 0; cbx < bandPrecinct.numBlocks.x; cbx++) {
                    final CodeBlock codeBlock = bandPrecinct.accessCodeBlock(p(cbx, cby));
                    final Region blockRegion = codeBlock.region().absolute();
                    final Rectangle blockRect = getResizedRect(blockRegion);
                    canvas.setColor(RED);
                    canvas.draw(blockRect);
                  }
                }


                final Region bandPrecRegion = bandPrecinct.region().absolute();
                final Rectangle bandPrecRect = getResizedRect(bandPrecRegion);
                canvas.setColor(Color.PINK);
                canvas.draw(bandPrecRect);
              }

              final Region bandRegion = subband.region().absolute();
              final Rectangle bandRect = getResizedRect(bandRegion);
              canvas.setColor(ORANGE);
              canvas.draw(bandRect);
            }

            final Region resRegion = resolution.region().absolute();
            final Rectangle resRect = getResizedRect(resRegion);
            canvas.setColor(GREEN);
            canvas.draw(resRect);
          }


          final Region tileCompRegion = tileComponent.region().absolute();
          final Rectangle tileCompRect = getResizedRect(tileCompRegion);
          canvas.setColor(CYAN);
          canvas.draw(tileCompRect);
        }

        final Region tileRegion = tile.region().absolute();
        final Rectangle tileRect = getResizedRect(tileRegion);
        canvas.setColor(BLUE);
        canvas.draw(tileRect);

        final Pair codeBlockAnchorPoint = tile.codeBlockAnchorPoint;
        canvas.setColor(RED);
        canvas.fill(new Rectangle(codeBlockAnchorPoint.x - 1, codeBlockAnchorPoint.y - 1, 4, 4));
      }
    }

    canvas.dispose();
    PartialResultsDebugger.getInstance().addDebugResult(img, "With element bounds");
  }

  private static Rectangle getResizedRect(Region r) {
    return new Rectangle(r.x0(), r.y0(), r.width() - 1, r.height() - 1);
  }
}
