package com.levigo.jadice.format.jpeg2000.internal.marker;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Validate;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Defined in <i>ITU-T.800, A.3.2</i>
 * <p>
 * <b>Function:</b> Describes the visual masking for all tile-components in the image or tile.
 * <p>
 * <b>Usage:</b> Present only if the visual masking capability bit in the {@link SIZ#Rsiz} parameter (see <i>ITU-T.801,
 * A.2.1</i>) has the value one. Optionally used in the main and/or the first tile-part header of a given tile. No
 * more than one {@link VMS} marker segment for a component shall appear in any header. When used in both the main
 * header and the first tile-part header, the {@link VMS} marker segment in the first tile part header overrides the
 * one in the main header for that tile. A {@link VMS} marker segment specifying a single component (<i>Cvms ≠ 65
 * 535</i>) overrides one specifying all components (<i>Cvms = 65 535</i>). Thus the order of precedence is the
 * following: <br>
 * <i>Tile-part VMS (Cvms ≠ 65 535) > Tile-part VMS (Cvms = 65 535) > Main VMS (Cvms ≠ 65 535) > Main VMS (Cvms = 65
 * 535)</i>
 * <br> where the "greater than" sign, <i>></i>, means that the greater overrides the lesser marker segment.
 * <p>
 * <b>Length:</b> Fixed.
 */
public class VMS extends AbstractMarkerSegment {

  /**
   * This value is used in <i>Cvms</i> parameter and defines that the region of interest descriptions apply to all
   * components.
   */
  public static final int VALUE_ALL_COMPONENTS = 65535;

  /**
   * Mask (<code>x111 1111</code>) for determining the minimum resolution level value, <i>minlevel</i> (0-32) coded in
   * <i>Svms</i> parameter (see <i>ITU-T.801, E.6</i>).<br>
   */
  public static final int MASK_MINLEVEL = 0x7F;

  /**
   * Mask (<code>1xxx xxxx</code>) for determining the value of the variable <i>respect_block_boundaries</i>
   * (<code>0</code> or <code>1</code>) coded in <i>Svms</i> parameter (see <i>ITU-T.801, E.6</i>).
   */
  public static final int MASK_RESPECT_BLOCK_BOUNDARIES = 1 << 7;

  /** Length of marker segment in bytes (not including the marker). Fixed at 7 bytes. */
  public int Lvms;

  /**
   * The index of the component to which this marker segment applies. Could be all components.
   * <table>
   * <thead>
   * <th>Values</th>
   * <th>Component index parameter</th>
   * </thead>
   * <tbody>
   * <tr>
   * <td><code>0-16383</code></td>
   * <td>Specifies component to which these region of interest descriptions apply</td>
   * </tr>
   * <tr>
   * <td><code>16394-65354</code></td>
   * <td>Reserved</td>
   * </tr>
   * <tr>
   * <td><code>65535</code></td>
   * <td>Region of interest descriptions apply to all components</td>
   * </tr>
   * </tbody>
   * </table>
   */
  public int Cvms;

  /**
   * Minimal resolution level and respect block boundaries flag.
   * <table>
   * <thead>
   * <th>Values (bits)<br>
   * <code>MSB LSB</code></th>
   * <th>Visual masking parameters</th>
   * </thead>
   * <tbody>
   * <tr>
   * <td><code>x000 0000</code><br>
   * to<br>
   * <code>x001 0000</code></td>
   * <td>Minimum resolution level value, <i>minlevel</i> (0-32) (see <i>ITU-T.801, E.6</i>)</td>
   * </tr>
   * <tr>
   * <td><code>0xxx xxxx</code><br>
   * <code>1xxx xxxx</code></td>
   * <td>
   * Variable <i>respect_block_boundaries</i><code>=0</code> (see <i>ITU-T.801, E.6</i>)<br>
   * Variable <i>respect_block_boundaries</i><code>=1</code> (see <i>ITU-T.801, E.6</i>)</td>
   * </tr>
   * </tbody>
   * </table>
   */
  public int Svms;

  /** Window width variable, <i>win_width</i> (see <i>ITU-T.801, E.6</i>). */
  public int Wvms;

  /** Bits retained variable, <i>bits_retained</i> (see <i>ITU-T.801, E.6</i>). */
  public int Rvms;

  /** Value of the numerator of the α parameter, α<code>=Avms/128</code> (see <i>ITU-T.801, E.6</i>). */
  public int Avms;

  /** Value of the numerator of the β parameter, β<code>=Bvms/128</code> (see <i>ITU-T.801, E.6</i>). */
  public int Bvms;

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException, JPEG2000Exception {
    Lvms = source.readUnsignedShort();
    Cvms = source.readUnsignedShort();
    Svms = source.readUnsignedByte();
    Wvms = source.readUnsignedByte();
    Rvms = source.readUnsignedByte();
    Avms = source.readUnsignedByte();
    Bvms = source.readUnsignedByte();
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lvms);
    sink.writeShort(Cvms);
    sink.writeByte(Svms);
    sink.writeByte(Wvms);
    sink.writeByte(Rvms);
    sink.writeByte(Avms);
    sink.writeByte(Bvms);
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    Validate.exact("Lvms", Lvms, 9);
    Validate.inRange("Wvms", Wvms, 0, 8);
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lvms", Lvms, "VMS.L"));
    markerInfo.add(new PropertiesParameterInfo("Cvms", Cvms, "VMS.C"));
    markerInfo.add(new PropertiesParameterInfo("Svms", Svms, "VMS.S"));
    markerInfo.add(new PropertiesParameterInfo("Wvms", Wvms, "VMS.W"));
    markerInfo.add(new PropertiesParameterInfo("Rvms", Rvms, "VMS.R"));
    markerInfo.add(new PropertiesParameterInfo("Avms", Avms, "VMS.A"));
    markerInfo.add(new PropertiesParameterInfo("Bvms", Bvms, "VMS.B"));
  }

  @Override
  public Marker getMarker() {
    return Marker.VMS;
  }
}
