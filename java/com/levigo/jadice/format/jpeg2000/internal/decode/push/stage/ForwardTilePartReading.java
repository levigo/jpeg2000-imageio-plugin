package com.levigo.jadice.format.jpeg2000.internal.decode.push.stage;

import java.io.IOException;
import java.util.List;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.codestream.TilePartPointer;
import com.levigo.jadice.format.jpeg2000.internal.codestream.TilePartPointerProvider;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.image.Canvas;
import com.levigo.jadice.format.jpeg2000.internal.image.Pair;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.Regions;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.io.MarkerReader;
import com.levigo.jadice.format.jpeg2000.internal.marker.Marker;
import com.levigo.jadice.format.jpeg2000.internal.marker.SOT;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;

/**
 * reads tile part headers and delegates to tile part body reading stage
 */
@Refer(to = Spec.J2K_CORE, page = 12, section = "Annex A", called = "Codestream Syntax")
public class ForwardTilePartReading implements Receiver<Codestream> {
  private final Receiver<TilePartPointer> nextStage;

  public ForwardTilePartReading(Receiver<TilePartPointer> nextStage) {
    this.nextStage = nextStage;
  }

  @Override
  public void receive(Codestream codestream, DecoderParameters parameters) throws JPEG2000Exception {
    final ImageInputStream source = codestream.source;
    synchronized (source) {
      try {
        final long savedPosition = source.getStreamPosition();
        long nextTilePartPosition = codestream.firstTilePartAddress;
        source.seek(nextTilePartPosition);

        final MarkerReader markerReader = new MarkerReader();
        Marker marker = markerReader.next(source);

        while (marker != Marker.EOC) {
          if (marker != Marker.SOT) {
            throw new JPEG2000Exception(CodestreamMessages.EXPECTED_SOT_MARKER, marker);
          }

          nextTilePartPosition = readTilePart(codestream, parameters, nextTilePartPosition);

          if (source.getStreamPosition() != nextTilePartPosition) {
            source.seek(nextTilePartPosition);
          }
          
          marker = markerReader.next(source);
        }

        source.seek(savedPosition);
      } catch (IOException e) {
        throw new JPEG2000Exception(GeneralMessages.IO_ERROR, e);
      }
    }
  }

  private long readTilePart(Codestream codestream, DecoderParameters parameters, long tilePartStart)
      throws IOException, JPEG2000Exception {

    final SOT sot = new SOT();
    sot.read(codestream.source, codestream, parameters.validate);

    final TilePartPointerProvider pointerProvider = codestream.accessTilePartPointerProvider();
    pointerProvider.registerSOT(sot, tilePartStart);

    final long tilePartEnd = tilePartStart + sot.Psot;

    final Pair numTiles = codestream.numTiles;
    final Pair tileIndices = Canvas.tileIndices(sot.Isot, numTiles.x);

    final Region canvas = codestream.region().absolute();
    final Region tileRegion = Canvas.tileRegion(canvas, codestream.tilePartition, tileIndices.x, tileIndices.y);

    if (Regions.intersect(tileRegion, parameters.region)) {
      final Tile tile = codestream.accessTile(tileIndices);
      final List<TilePartPointer> tilePartPointers = pointerProvider.getTilePartPointers(tile.idx);
      final TilePartPointer tilePartPointer = tilePartPointers.get(tilePartPointers.size() - 1);
      tilePartPointer.tile = tile;
      tile.readTilePartHeader(tilePartPointer);
      nextStage.receive(tilePartPointer, parameters);
    }

    return tilePartEnd;
  }

}
