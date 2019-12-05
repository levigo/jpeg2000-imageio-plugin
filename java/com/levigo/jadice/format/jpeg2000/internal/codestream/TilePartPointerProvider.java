package com.levigo.jadice.format.jpeg2000.internal.codestream;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.io.MarkerReader;
import com.levigo.jadice.format.jpeg2000.internal.marker.Marker;
import com.levigo.jadice.format.jpeg2000.internal.marker.SOT;
import com.levigo.jadice.format.jpeg2000.internal.marker.TLM;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class TilePartPointerProvider {

  private static class TilePartIndexComparator implements Comparator<Integer> {
    @Override
    public int compare(Integer o1, Integer o2) {
      return o1 > o2 ? -1 : o1 < o2 ? 1 : 0;
    }
  }

  private static class TLMIndexComparator implements Comparator<TLM> {
    @Override
    public int compare(TLM o1, TLM o2) {
      return o1.Ztlm < o2.Ztlm ? -1 : o1.Ztlm > o2.Ztlm ? 1 : 0;
    }
  }

  /**
   * Data structure containing {@link TilePartPointer} objects ordered by its tile-part index ({@link
   * TilePartPointer#tilePartIdx}).
   */
  public TreeMap<Integer, List<TilePartPointer>> tilePartPointers;

  private TreeSet<TLM> tlmMarkers;

  public boolean fromTLM;

  public TilePartPointerProvider() {
    tilePartPointers = new TreeMap<>(new TilePartIndexComparator());
    fromTLM = false;
  }

  /**
   * Use this method to register {@link TLM} marker segments which has been signalled in {@link Codestream}'s main
   * header. To ensure all information is saved correctly call {@link #finish(long)} if the main header is expected to
   * be fully read.
   *
   * @param tlm a {@link TLM} marker segment object which should be registered for later application.
   */
  public void registerTLM(TLM tlm) {
    if (tlmMarkers == null) {
      tlmMarkers = new TreeSet<>(new TLMIndexComparator());
    }

    tlmMarkers.add(tlm);
  }

  /**
   * Saves all information of previously registered {@link TLM} marker segments into the persistent and publicly
   * accessible {@link #tilePartPointers} data structure. After all information has been processed all references to
   * registered {@link TLM} marker segment objects will be released and these objects are free to be garbage collected.
   * <p>
   * Call this method only if the {@link Codestream}'s main header signalled {@link TLM} marker segments which has
   * previously been registered via {@link #registerTLM(TLM)}-method.
   * <p>
   * To find out if there are any registered {@link TLM} marker segments use {@link #hasTLM()}.
   *
   * @param firstTilePartPosition the position in the {@link Codestream}'s source where the first {@link SOT} marker
   *                              segment is expected.
   */
  public void finish(long firstTilePartPosition) {
    long tilePartStart = firstTilePartPosition;
    int tilePartIdx = 0;
    for (TLM tlm : tlmMarkers) {
      final Iterator<Integer> tileIndices = getIndexIterator(tlm);
      final Iterator<Integer> tilePartLengths = getLengthIterator(tlm);
      while (tileIndices.hasNext() && tilePartLengths.hasNext()) {
        final int tileIdx = tileIndices.next();
        final int tilePartLength = tilePartLengths.next();
        final List<TilePartPointer> pointers = getPointers(tileIdx);
        pointers.add(new TilePartPointer(tilePartIdx++, tilePartStart, tilePartLength, tileIdx));
        tilePartStart += tilePartLength;
      }
    }
    fromTLM = true;
    tlmMarkers = null;
  }
  
  private Iterator<Integer> getIndexIterator(TLM tlm) {
    if (tlm.Ttlm == null) {
      return new TilePartSequence();
    } else {
      return tlm.Ttlm.iterator();
    }
  }

  private Iterator<Integer> getLengthIterator(TLM tlm) {
    return tlm.Ptlm.iterator();
  }

  /**
   * Use this method to determine if any {@link TLM} marker segment has been registered.
   *
   * @return <code>true</code> if the internally maintained store for {@link TLM} marker segment has been initialized
   * and contains at least one object; otherwise <code>false</code>.
   */
  public boolean hasTLM() {
    return tlmMarkers != null && !tlmMarkers.isEmpty();
  }

  /**
   * Registers a tile-part directly from a read {@link SOT} marker segment. The implementation creates a new {@link
   * TilePartPointer} object and stores it directly into {@link #tilePartPointers}.
   *
   * @param sot           the found {@link SOT} marker segment signalled as the beginning of a new tile-part.
   * @param tilePartStart the position in {@link Codestream}'s source where the tile-part begins.
   * @return <code>true</code> if the new tile-part information was successfully stored; otherwise <code>false</code>.
   */
  public boolean registerSOT(SOT sot, long tilePartStart) {
    final TilePartPointer pointer = new TilePartPointer(sot.TPsot, tilePartStart, sot.Psot, sot.Isot);
    final List<TilePartPointer> pointers = getPointers(sot.Isot);
    if (!pointers.isEmpty() && pointers.contains(pointer)) {
      return false;
    }
    return pointers.add(pointer);
  }

  private List<TilePartPointer> getPointers(int tileIdx) {
    return getPointers(tileIdx, true);
  }

  private List<TilePartPointer> getPointers(int tileIdx, boolean create) {
    List<TilePartPointer> pointers = tilePartPointers.get(tileIdx);
    if (pointers == null && create) {
      tilePartPointers.put(tileIdx, pointers = new ArrayList<>(2));
    }
    return pointers;
  }

  /**
   * Retrieves a {@link java.util.List} of {@link TilePartPointer}s which belong to a specific tile.
   *
   * @param tileIdx the tile's absolute index.
   * @return a {@link java.util.List} of {@link TilePartPointer}s which belong to a specific tile or <code>null</code>
   * if not yet available.
   */
  public List<TilePartPointer> getTilePartPointers(int tileIdx) {
    return getPointers(tileIdx, false);
  }

  public SOT findTileHeader(int tileIdx, Codestream codestream) throws IOException, JPEG2000Exception {
    final MarkerReader markerReader = new MarkerReader();
    final SOT sot = new SOT();
    Marker marker;
    do {
      codestream.source.seek(codestream.nextTilePartAddress);
      marker = markerReader.next(codestream.source);
      if (marker == Marker.EOC) {
        return null;
      }
      if (marker != Marker.SOT) {
        throw new JPEG2000Exception(CodestreamMessages.INVALID_SOT_ADDRESS, codestream.nextTilePartAddress);
      }
      sot.read(codestream.source, codestream, false);
      registerSOT(sot, codestream.nextTilePartAddress);
      codestream.nextTilePartAddress += sot.Psot;
    } while (sot.Isot != tileIdx);

    return sot;
  }
  
  private class TilePartSequence implements Iterator<Integer> {
    private int next;
    
    public TilePartSequence() {
      next = 0;
    }
    
    @Override
    public boolean hasNext() {
      return true;
    }
    
    @Override
    public Integer next() {
      return next++;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }
  
}
