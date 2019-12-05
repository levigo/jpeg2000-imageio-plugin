package com.levigo.jadice.format.jpeg2000.internal.codestream;

import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;

public class TilePartPointer implements Pushable {

  public final int tilePartIdx;

  /**
   * Tile index to which the tile-part belongs.
   */
  public final int tileIdx;

  /**
   * Start position of the tile-part.
   */
  public final long tilePartStart;

  /**
   * Length of the tile-part relative to the {@link #tilePartStart}. Thus, the end position can be determined by
   * <code>{@link #tilePartStart} + {@link #tilePartLength}</code>.
   */
  public final long tilePartLength;

  public long tilePartDataStart;
  
  public Tile tile;

  public TilePartPointer(int tilePartIdx, long tilePartStart, long tilePartLength, int tileIdx) {
    this.tilePartIdx = tilePartIdx;
    this.tilePartStart = tilePartStart;
    this.tilePartLength = tilePartLength;
    this.tileIdx = tileIdx;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TilePartPointer that = (TilePartPointer) o;

    if (tileIdx != that.tileIdx) return false;
    if (tilePartIdx != that.tilePartIdx) return false;
    if (tilePartLength != that.tilePartLength) return false;
    if (tilePartStart != that.tilePartStart) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = tilePartIdx;
    result = 31 * result + tileIdx;
    result = 31 * result + (int) (tilePartStart ^ (tilePartStart >>> 32));
    result = 31 * result + (int) (tilePartLength ^ (tilePartLength >>> 32));
    return result;
  }

  @Override
  public void start(DecoderParameters parameters) {
    
  }

  @Override
  public void free() {

  }
}
