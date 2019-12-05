package com.levigo.jadice.format.jpeg2000.internal.marker;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.param.DirectParameterInfo;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;
import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

/**
 * Defined in <i>ITU-T.800, A.6.5</i>.
 * <p>
 * <b>Function:</b> Describes the quantization default used for compressing all components not defined by a QCC marker
 * segment. The parameter values can be overridden for an individual component by a QCC marker segment in either the
 * main or tile-part header.
 * <p>
 * <b>Usage:</b> Main and first tile-part header of a given tile. Shall be one and only one in the main header. May be
 * at most one for all tile-part headers of a tile. If there are multiple tile-parts for a tile, and this marker
 * segment is present, it shall be found only in the first tile-part ({@link SOT#TPsot} = 0). When used in the
 * tile-part header it overrides the main {@link QCD} and the main {@link QCC} for the specific component. Thus, the
 * order of precedence is the following:
 * <p>
 * <code>Tile-part QCC > Tile-part QCD > Main QCC > Main QCD</code>
 * <p>
 * where the "greater than" sign, >, means that the greater overrides the lessor marker segment.
 * <p>
 * <b>Length:</b> Variable depending on the number of quantized elements.
 */
public class QCD extends Qxx {

  /**
   * Length of marker segment in bytes (not including the marker).
   * <p>
   * <b>Note:</b> The {@link #Lqcd} can be used to determine how many quantization step sizes are present in the marker
   * segment. However, there is not necessarily a correspondence with the number of sub-bands present because the
   * sub-bands can be truncated with no requirement to correct this marker segment.
   */
  public int Lqcd;

  @Override
  public Marker getMarker() {
    return Marker.QCD;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lqcd = source.readUnsignedShort();
    Sqxx_guardbits = (byte) (source.readBits(3) & 0x7);
    Sqxx_style = (byte) (source.readBits(5) & 0x1F);

    int remainingBytes = Lqcd - 3;
    switch (Sqxx_style){
      case VALUE_NO_QUANTIZATION:
        int remainingLoops = remainingBytes;
        SPqxx_exp = new int[remainingLoops];
        for (int b = 0; b < remainingLoops; b++) {
          SPqxx_exp[b] = readExponent(source);
          source.readBits(3); // the rest of each byte are filled with unused bits
        }
        break;

      case VALUE_SCALAR_DERIVED:
      case VALUE_SCALAR_EXPOUNDED:
      case VALUE_TRELLIS_DERIVED:
      case VALUE_TRELLIS_EXPOUNDED:
        remainingLoops = remainingBytes >> 1; // equivalent to a division by 2
        SPqxx_exp = new int[remainingLoops];
        SPqxx_man = new int[remainingLoops];

        for (int b = 0; b < remainingLoops; b++) {
          SPqxx_exp[b] = readExponent(source);
          SPqxx_man[b] = readMantissa(source);
        }
        break;
      case VALUE_DEADZONE_AND_SCALAR_DERIVED:
      case VALUE_DEADZONE_AND_SCALAR_EXPOUNDED:
        remainingLoops = remainingBytes >> 2; // equivalent to a division by 4
        SPqxx_dzone = new int[remainingLoops];
        SPqxx_exp = new int[remainingLoops];
        SPqxx_man = new int[remainingLoops];

        for (int b = 0; b < remainingLoops; b++) {
          SPqxx_dzone[b] = source.readUnsignedShort();
          SPqxx_exp[b] = readExponent(source);
          SPqxx_man[b] = readMantissa(source);
        }
        break;
      case VALUE_DEADZONE_DERIVED_AND_SCALAR_EXPOUNDED:
        remainingLoops = (remainingBytes - 2) >> 1;
        SPqxx_dzone = new int[1];
        SPqxx_exp = new int[remainingLoops];
        SPqxx_man = new int[remainingLoops];

        SPqxx_dzone[0] = source.readUnsignedShort();

        for (int b = 0; b < remainingLoops; b++) {
          SPqxx_exp[b] = readExponent(source);
          SPqxx_man[b] = readMantissa(source);
        }
        break;
      default:
        final QualifiedLogger LOG = LoggerFactory.getQualifiedLogger(QCD.class);
        LOG.warn(CodestreamMessages.MARKER_SEGMENT_FORMAT_FOR_QCD_UNKNOWN);

        LOG.warn(CodestreamMessages.SKIPPING_MARKER_SEGMENT_BYTES, remainingBytes);
        source.skipBytes(remainingBytes);
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
    markerInfo.add(new PropertiesParameterInfo("Lqcd", Lqcd, "QCD.L"));
    markerInfo.add(new PropertiesParameterInfo("Guardbits (Sqcd)", Sqxx_guardbits, "Qxx.S.guardbits"));
    markerInfo.add(new PropertiesParameterInfo("Style (Sqcd)", Sqxx_style, "QxD.S.style"));

    if (SPqxx_dzone != null) {
      markerInfo.add(new PropertiesParameterInfo("DZone (SPqcd)", "Qxx.SP.deadzone"));
      for (int b = 0; b < SPqxx_dzone.length; b++) {
        markerInfo.add(new DirectParameterInfo("->[" + b + "]", SPqxx_dzone[b]));
      }
    }

    if (SPqxx_exp != null) {
      markerInfo.add(new PropertiesParameterInfo("Exponent (SPqcd)", SPqxx_man == null ? "Qxx.SP.exp0" : "Qxx.SP.exp"));
      for (int b = 0; b < SPqxx_exp.length; b++) {
        markerInfo.add(new DirectParameterInfo("->[" + b + "]", SPqxx_exp[b]));
      }
    }

    if (SPqxx_man != null) {
      markerInfo.add(new PropertiesParameterInfo("Mantissa (SPqcd)", "Qxx.SP.man"));
      for (int b = 0; b < SPqxx_man.length; b++) {
        markerInfo.add(new DirectParameterInfo("->[" + b + "]", SPqxx_man[b]));
      }
    }
  }
}
