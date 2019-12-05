package com.levigo.jadice.format.jpeg2000.internal.marker;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Defined in <i>ITU-T.801, A.3.10</i>
 * <p>
 * <b>Function:</b> Describes either a gamma or LUT non-linearity to be applied to a single component or all
 * components.
 * <p>
 * <b>Usage:</b> Present only if the non-linearity point transformation capability bit in the {@link SIZ#Rsiz}
 * parameter (see <i>ITU-T.801, A.2.1</i>) is a one value. Main and first tile-part header of a given tile. There may
 * be no more than one marker segment per component plus one default in any header. When used in the main header, the
 * defined non-linearity can be established as a default for all components or established as a default for a single
 * component. When used in a tile-part header, it can be used to establish a default for all components in the tile or
 * to set the non-linearity transformation for a single component in that tile. Thus, the order of precedence is the
 * following:<br>
 * <i>Tile-part NLT > Tile-part NLT default > Main NLT > Main NLT default</i><br>
 * where the "greater than" sign, >, means that the greater overrides the lesser marker segment.
 * <p>
 * <b>Length:</b> Variable depending on the value of {@link #Tnlt}.
 */
public class NLT extends AbstractMarkerSegment {

  /**
   * <b>Value</b> for determining if {@link NLT#Cnlt} parameter defines non-linearity transformation descriptions in
   * this marker segment apply to all components.
   */
  public static final int VALUE_APPLIES_TO_ALL_COMPONENTS = 65535;

  /**
   * <b>Mask</b> (<code>x111 1111</code>) for determining <i>component sample bit depth = value + 1</i>. From 1 bit
   * deep through 38 bits deep respectively. Masked value ({@link NLT#BDnlt}) can be used directly.
   */
  public static final int MASK_BIT_DEPTH = 0x7F;

  /**
   * <b>Mask</b> (<code>1xxx xxxx</code>) for determining the samples sign.<br>
   * <b>Values:</b><br>
   * Bit 0: Component sample values are unsigned values.<br>
   * Bit 1: Component sample values are signed values.
   */
  public static final int MASK_SIGN = 1 << 7;

  /** Value signaled via {@link NLT#Tnlt} that defines that the no non-linearity transformation shall be applied. */
  public static final int VALUE_NO_NON_LINEARITY_TRANSFORMATION = 0;

  /** Value signaled via {@link NLT#Tnlt} that defines that the use of gamma-style non-linearity transformation. */
  public static final int VALUE_GAMMA_STYLE_TRANSFORMATION = 1;

  /** Value signaled via {@link NLT#Tnlt} that defines the use of LUT-style non-linearity transformation. */
  public static final int VALUE_LUT_STYLE_TRANSFORMATION = 2;

  /** Length of marker segment in bytes (not including the marker). */
  public int Lnlt;

  /**
   * The index of the component to which this marker segment relates. The components are indexed 0, 1, 2, etc. If this
   * value is 65 535, then this marker segment applies to all components. <i>ITU-T.801, Table A.42</i> shows the value
   * for the {@link #Cnlt} parameter.
   */
  public int Cnlt;

  /**
   * Bit depth and sign of the decoded image component, <i>Z<sub>i</sub></i>, after processing of the <i>i</i>-th
   * reconstructed image component by the non-linearity. If <i>{@link #Cnlt} = 65 535</i>, then this value applies to
   * all components. <i>ITU-T.801, Table A.43</i> shows the values for the {@link #BDnlt} parameter.
   */
  public int BDnlt;

  /** Non-linearity type. <i>ITU-T.801, Table A.44</i> shows the value for the {@link #Tnlt} parameter. */
  public int Tnlt;

  /** Non-linearity exponent (8-bit integer + 16-bit fraction). Only present if {@link #Tnlt} = 1. */
  public long STnlt_E;

  /** Non-linearity toe slope (8-bit integer + 16-bit fraction). Only present if {@link #Tnlt} = 1. */
  public long STnlt_S;

  /** Non-linearity threshold (8-bit integer + 16-bit fraction). Only present if {@link #Tnlt} = 1. */
  public long STnlt_T;

  /** Non-linearity continuity parameter A (8-bit integer + 16-bit fraction). Only present if {@link #Tnlt} = 1. */
  public long STnlt_A;

  /** Non-linearity continuity parameter B (8-bit integer + 16-bit fraction). Only present if {@link #Tnlt} = 1. */
  public long STnlt_B;

  /**
   * (Number of points – 1) in the LUT-style non-linearity definition (all other values reserved). Only present if
   * {@link #Tnlt} = 2.
   */
  public int STnlt_Npoints;

  /** D<sub>min</sub> = parameter value / (2<sup>32</sup>–1). Only present if {@link #Tnlt} = 2. */
  public long STnlt_Dmin;

  /** D<sub>max</sub> = parameter value / (2<sup>32</sup>–1). Only present if {@link #Tnlt} = 2. */
  public long STnlt_Dmax;

  /**
   * Precision of {@link #STnlt_Tvalue} parameter in bits (1-32). This also implies how many bytes are used to express
   * the {@link #STnlt_Tvalue} (all other values reserved). Only present if {@link #Tnlt} = 2.
   */
  public int STnlt_PTval;

  /**
   * Run of table values for the LUT-style non-linearity. The (<i>{@link #STnlt_Npoints} + 1</i>) parameters are
   * unsigned integers. The actual value of {@link #STnlt_Tvalue} is {@link #STnlt_Tvalue} = parameter value /
   * (2<sup>{@link #STnlt_PTval}</sup> – 1). Only present if {@link #Tnlt} = 2.
   */
  public long STnlt_Tvalue;

  @Override
  public Marker getMarker() {
    return Marker.NLT;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException, JPEG2000Exception {
    Lnlt = source.readUnsignedShort();
    Cnlt = source.readUnsignedShort();
    BDnlt = source.readUnsignedByte();
    Tnlt = source.readUnsignedByte();

    switch (Tnlt) {
      case VALUE_GAMMA_STYLE_TRANSFORMATION:
        STnlt_E = source.readBits(24);
        STnlt_S = source.readBits(24);
        STnlt_T = source.readBits(24);
        STnlt_A = source.readBits(24);
        STnlt_B = source.readBits(24);
        break;
      case VALUE_LUT_STYLE_TRANSFORMATION:
        STnlt_Npoints = source.readUnsignedShort();
        STnlt_Dmin = source.readUnsignedInt();
        STnlt_Dmax = source.readUnsignedInt();
        STnlt_PTval = source.readUnsignedByte();

        if (STnlt_PTval <= 8) {
          STnlt_Tvalue = source.readUnsignedByte();
        } else if (STnlt_PTval <= 16) {
          STnlt_Tvalue = source.readUnsignedShort();
        } else {
          STnlt_Tvalue = source.readUnsignedInt();
        }
        break;
    }
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    throw new UnsupportedOperationException("Currently not yet implemented.");
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    throw new UnsupportedOperationException("Currently not yet implemented.");
  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    throw new UnsupportedOperationException("Currently not yet implemented.");
  }
}
