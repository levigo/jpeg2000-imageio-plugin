package com.levigo.jadice.format.jpeg2000.internal.marker;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Capability;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

public class RGN extends AbstractMarkerSegment {

  /**
   * Possible value of <i>Srgn</i> parameter defining a implicit region of interest with maximumshift. See
   * <i>ITU-T.800,
   * Table A.25</i> for details.
   */
  public static final int VALUE_IMPLICIT_ROI = 0;
  
  /**
   * Possible value of <i>Srgn</i> parameter defining an arbitrary rectangular region of interest. See <i>ITU-T.801,
   * Table A.16</i> for details.
   */
  public static final int VALUE_ARBITRARY_ROI_RECTANGLE = 1;
  
  /**
   * Possible value of <i>Srgn</i> parameter defining an arbitrary elliptic region of interest. See <i>ITU-T.801, Table
   * A.16</i> for details.
   */
  public static final int VALUE_ARBITRARY_ROI_ELLIPSE = 2;
  
  /**
   * The value for <i>Crgn</i> parameter which defines that an {@link RGN} marker segment's region of interest applies
   * to all components.
   */
  public static final int VALUE_COMPONENTS_ALL = 65535;
  
  public int Lrgn;
  public int Crgn;
  public int Srgn;
  public int SPrgn;
  public long XArgn;
  public long YArgn;
  public long XBrgn;
  public long YBrgn;

  public boolean useArbitraryROI;

  @Override
  public Marker getMarker() {
    return Marker.RGN;
  }

  @Override
  public MarkerKey getMarkerKey() {
    return new MarkerKey(getMarker(), Crgn);
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    useArbitraryROI = Capability.T801_ARBITRARY_SHAPED_ROI.isUsedBy(codestream);

    Lrgn = source.readUnsignedShort();

    if (useArbitraryROI || codestream.numComps >= 257) {
      Crgn = source.readUnsignedShort();
    } else {
      Crgn = source.readUnsignedByte();
    }

    Srgn = source.readUnsignedByte();
    SPrgn = source.readUnsignedByte();

    if (useArbitraryROI) {
      if (Srgn == VALUE_ARBITRARY_ROI_RECTANGLE || Srgn == VALUE_ARBITRARY_ROI_ELLIPSE) {
        XArgn = source.readUnsignedInt();
        YArgn = source.readUnsignedInt();
        XBrgn = source.readUnsignedInt();
        YBrgn = source.readUnsignedInt();
      }
    }
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lrgn);

    if (useArbitraryROI || codestream.numComps >= 257) {
      sink.writeShort(Crgn);
    } else {
      sink.writeByte(Crgn);
    }

    sink.writeByte(Srgn);
    sink.writeByte(SPrgn);

    if (useArbitraryROI) {
      if (Srgn == VALUE_ARBITRARY_ROI_RECTANGLE || Srgn == VALUE_ARBITRARY_ROI_ELLIPSE) {
        sink.writeInt((int) XArgn);
        sink.writeInt((int) YArgn);
        sink.writeInt((int) XBrgn);
        sink.writeInt((int) YBrgn);
      }
    }
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {

  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lrgn", Lrgn, "RGN.L"));
    markerInfo.add(new PropertiesParameterInfo("Crgn", Crgn, "RGN.C"));
    markerInfo.add(new PropertiesParameterInfo("Srgn", Srgn, "RGN.S"));
    markerInfo.add(new PropertiesParameterInfo("SPrgn", SPrgn, "RGN.SP"));

    if (useArbitraryROI) {
      markerInfo.add(new PropertiesParameterInfo("XArgn (left)", XArgn, "RGN.XA"));
      markerInfo.add(new PropertiesParameterInfo("YArgn (top)", YArgn, "RGN.YA"));
      markerInfo.add(new PropertiesParameterInfo("XBrgn (right)", XBrgn, "RGN.XB"));
      markerInfo.add(new PropertiesParameterInfo("YBrgn (bottom)", XBrgn, "RGN.YB"));
    }
  }
}
