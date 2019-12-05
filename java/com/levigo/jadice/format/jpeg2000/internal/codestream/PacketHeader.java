package com.levigo.jadice.format.jpeg2000.internal.codestream;

import java.util.LinkedList;
import java.util.List;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;

@Refer(to = Spec.J2K_CORE, page = 54, section = "B.10", called = "Packet Header Information Coding")
public class PacketHeader implements Pushable {

  public ImageInputStream source;
  
  /** Component index */
  public int comp;

  /** Resolution level index */
  public int res;

  /** Layer index */
  public int layer;

  /** Precinct index */
  public int precinct;

  /**
   * Indicates if the current instance of {@link PacketHeader} is empty. Flag is set to <code>true</code> if the first bit
   * retrieved from a packet header was <code>0</code>.
   * <p>
   * If this flag is set to <code>true</code> there won't be any {@link #blockContributions} and the size will be
   * <code>0</code>.
   */
  public boolean empty;

  public long payloadLength;

  public final List<BlockContribution> blockContributions;

  public PacketHeader next;

  public PacketHeader() {
    empty = false;
    blockContributions = new LinkedList<>();
  }

  public PacketHeader(int comp, int res, int layer, int precinct) {
    this();
    this.comp = comp;
    this.res = res;
    this.layer = layer;
    this.precinct = precinct;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    PacketHeader packetHeader = (PacketHeader) o;

    if (comp != packetHeader.comp)
      return false;
    if (res != packetHeader.res)
      return false;
    if (layer != packetHeader.layer)
      return false;
    return precinct == packetHeader.precinct;

  }
  @Override
  public int hashCode() {
    int result = comp;
    result = 31 * result + res;
    result = 31 * result + layer;
    result = 31 * result + precinct;
    return result;
  }

  @Override
  public String toString() {
    return "Packet (" + "c=" + comp + ", r=" + res + ", l=" + layer + ", p=" + precinct + ')';
  }

  @Override
  public void start(DecoderParameters parameters) {
    empty = false;
    blockContributions.clear();
    payloadLength = 0;
  }

  @Override
  public void free() {
    empty = false;
    blockContributions.clear();
    payloadLength = 0;
  }
}
