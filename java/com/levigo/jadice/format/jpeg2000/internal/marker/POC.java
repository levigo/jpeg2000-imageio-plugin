package com.levigo.jadice.format.jpeg2000.internal.marker;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Validate;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.param.DirectParameterInfo;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;
import com.levigo.jadice.format.jpeg2000.msg.ValidationMessages;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Defined in <i>ITU-T.800, A.6.6</i>.
 * <p>
 * <b>Function:</b> Describes the bounds and progression order for any progression order other than specified in the
 * {@link COD} marker segments in the codestream.
 * <p>
 * <b>Usage:</b> Main and tile-part headers. At most one {@link POC} marker segment may appear in any header. However,
 * several progressions can be described with one {@link POC} marker segment. If a {@link POC} marker segment is used
 * in the main header, it overrides the progression order in the main and tile {@link COD} marker segments. If a {@link
 * POC} is used to describe the progression of a particular tile, a {@link POC} marker segment must appear in the first
 * tile-part header of that tile. Thus, the progression order of a given tile is determined by the presence of the
 * {@link POC} or the values of the {@link COD} in the following order of precedence:
 * <p>
 * <code>Tile-part POC > Main POC > Tile-part COD > Main COD</code>
 * <p>
 * where the "greater than" sign, <code>></code>, means that the greater overrides the lesser marker segment.
 * <p>
 * In the case where a {@link POC} marker segment is used, the progression of every packet in the codestream (or for
 * that tile of the codestream) shall be defined in one or more {@link POC} marker segments. Each progression order is
 * described in only one {@link POC} marker segment and shall be described in any tile-part header before any packets
 * of that progression are found.
 * <p>
 * <b>Length:</b> Variable depending on the number of different progressions.
 */
public class POC extends AbstractMarkerSegment {

  /**
   * Length of marker segment in bytes (not including the marker). The value of this parameter is determined by
   * equation <i>ITU-T.800, (A-6)</i>.
   */
  public int Lpoc;

  /**
   * Resolution level index (inclusive) for the start of a progression. One value for each progression change in this
   * tile or tile-part. The number of progression changes can be derived from the length of the marker segment.
   */
  public int[] RSpoc;

  /**
   * Component index (inclusive) for the start of a progression. The components are indexed 0,1,2,etc. (either 8 or 16
   * bits depending on {@link com.levigo.jadice.format.jpeg2000.internal.marker.SIZ#Csiz} value). One value for each
   * progression change in this tile or tile-part. The number of progression changes can be derived from the length of
   * the marker segment.
   */
  public int[] CSpoc;

  /**
   * Layer index (exclusive) for the end of a progression. The layer index always starts at zero for every progression.
   * Packets that have already been included in the codestream are not included again. One value for each progression
   * change in this tile or tile-part. The number of progression changes can be derived from the length of the marker
   * segment.
   */
  public int[] LYEpoc;

  /**
   * Resolution Level index (exclusive) for the end of a progression. One value for each progression change in this
   * tile or tile-part. The number of progression changes can be derived from the length of the marker segment.
   */
  public int[] REpoc;

  /**
   * Component index (exclusive) for the end of a progression. <b>The value <code>0</code> is interpreted as 256.</b>
   * The components are indexed 0,1,2, etc. (either 8 or 16 bits depending on {@link
   * com.levigo.jadice.format.jpeg2000.internal.marker.SIZ#Csiz} value). One value for each progression change in this
   * tile or tile-part. The number of progression changes can be derived from the length of the marker segment.
   */
  public int[] CEpoc;

  /**
   * Progression order. One value for each progression change in this tile or tile-part. The number of progression
   * changes can be derived from the length of the marker segment.
   */
  public int[] Ppoc;

  @Override
  public Marker getMarker() {
    return Marker.POC;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lpoc = source.readUnsignedShort();

    final int numEntries;
    final int compParamSize;
    if (codestream.numComps < 257) {
      numEntries = (Lpoc - 2) / 7;
      compParamSize = 8;
    } else {
      numEntries = (Lpoc - 2) / 9;
      compParamSize = 16;
    }

    RSpoc = new int[numEntries];
    CSpoc = new int[numEntries];
    LYEpoc = new int[numEntries];
    REpoc = new int[numEntries];
    CEpoc = new int[numEntries];
    Ppoc = new int[numEntries];

    for (int i = 0; i < numEntries; i++) {
      RSpoc[i] = source.readUnsignedByte();
      CSpoc[i] = (int) source.readBits(compParamSize);
      LYEpoc[i] = source.readUnsignedShort();
      REpoc[i] = source.readUnsignedByte();
      CEpoc[i] = (int) source.readBits(compParamSize);
      Ppoc[i] = source.readUnsignedByte();
    }

  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lpoc);

    final int compParamSize = codestream.numComps < 257 ? 8 : 16;
    for (int i = 0; i < Ppoc.length; i++) {
      sink.writeByte(RSpoc[i]);
      sink.writeBits(CSpoc[i], compParamSize);
      sink.writeShort(LYEpoc[i]);
      sink.writeByte(REpoc[i]);
      sink.writeBits(CEpoc[i], compParamSize);
      sink.writeByte(Ppoc[i]);
    }
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    Validate.inRange("Lpoc", Lpoc, 9, 65535);
    for (int i = 0; i < RSpoc.length; i++) {
      Validate.inRange("RSpoc[" + i + "]", RSpoc[i], 0, 32);
    }
    for (int i = 0; i < REpoc.length; i++) {
      Validate.inRange("REpoc[" + i + "]", REpoc[i], RSpoc[i] + 1, 33);
    }
    for (int i = 0; i < CSpoc.length; i++) {
      Validate.inRange("CSpoc[" + i + "]", CSpoc[i], 0, codestream.numComps < 257 ? 255 : 16383);
    }
    for (int i = 0; i < CEpoc.length; i++) {
      final int max = codestream.numComps < 257 ? 255 : 16384;
      if (CEpoc[i] != 0 && (CEpoc[i] < CSpoc[i] + 1 || CEpoc[i] > max)) {
        throw new JPEG2000Exception(ValidationMessages.ILLEGAL_POC_ENTRY_VALUE_FOR_LAST_COMPONENT, i);
      }
    }
    for (int i = 0; i < LYEpoc.length; i++) {
      Validate.inRange("LYEpoc[" + i + "]", LYEpoc[i], 1, 65535);
    }
    for (int i = 0; i < Ppoc.length; i++) {
      Validate.inRange("Ppoc[" + i + "]", Ppoc[i], 0, 4);
    }
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lpoc", Lpoc, "POC.L"));

    markerInfo.add(new PropertiesParameterInfo("RSpoc", "POC.RS"));
    for (int i = 0; i < RSpoc.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", RSpoc[i]));
    }

    markerInfo.add(new PropertiesParameterInfo("CSpoc", "POC.CS"));
    for (int i = 0; i < CSpoc.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", CSpoc[i]));
    }

    markerInfo.add(new PropertiesParameterInfo("LYEpoc", "POC.LYE"));
    for (int i = 0; i < LYEpoc.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", LYEpoc[i]));
    }

    markerInfo.add(new PropertiesParameterInfo("REpoc", "POC.RE"));
    for (int i = 0; i < REpoc.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", REpoc[i]));
    }

    markerInfo.add(new PropertiesParameterInfo("CEpoc", "POC.CE"));
    for (int i = 0; i < CEpoc.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", CEpoc[i]));
    }

    markerInfo.add(new PropertiesParameterInfo("Ppoc", "POC.P"));
    for (int i = 0; i < Ppoc.length; i++) {
      markerInfo.add(new DirectParameterInfo("->[" + i + "]", Ppoc[i]));
    }
  }
}
