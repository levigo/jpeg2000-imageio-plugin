package com.levigo.jadice.format.jpeg2000.internal.marker;

import static com.levigo.jadice.format.jpeg2000.internal.codestream.Capability.T801_SINGLE_SAMPLE_OVERLAP;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Validate;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.param.DirectParameterInfo;
import com.levigo.jadice.format.jpeg2000.internal.param.Parameters;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;
import com.levigo.jadice.format.jpeg2000.internal.tier2.ProgressionOrders;

/**
 * Defined in <i>ITU-T.800, A.6.1</i>. Extended by <i>ITU-T.801, A.2.3</i>.
 * <p>
 * <b>Function:</b> Describes the coding style, number of decomposition levels, and layering that is the default used
 * for compressing all components of an image (if in the main header) or a tile (if in the tile-part header). The
 * parameter values can be overridden for an individual component by a COC marker segment in either the main or
 * tile-part header.
 * <p>
 * <b>Usage:</b> Main and first tile-part header of a given tile. Shall be one and only one in the main header.
 * Additionally, there may be at most one for each tile. If there are multiple tile-parts in a tile, and this marker
 * segment is present, it shall be found only in the first tile-part (<code>{@link SOT#TPsot} = 0</code>). When used in
 * the main header, the {@link COD} marker segment parameter values are used for all tile-components that do not have a
 * corresponding {@link COC} marker segment in either the main or tile-part header. When used in the tile-part header
 * it overrides the main header {@link COD} and {@link COC}s and is used for all components in that tile without a
 * corresponding {@link COC} marker segment in the tile-part. Thus, the order of precedence is the following:
 * <p>
 * <code>Tile-part COC > Tile-part COD > Main COC > Main COD</code>
 * <p>
 * where the "greater than" sign, <code>></code>, means that the greater overrides the lessor marker segment.
 * <p>
 * <b>Length:</b> Variable depending on the value of <code>Scod</code>.
 */
public class COD extends COx {

  public int Lcod;

  /**
   * To access flags use the constants defined in super class {@link COx} with the '<code>MASK_CODING_</code>' prefix.
   */
  public int Scod;

  public int SGcod_order;

  /**
   * Number of layers.
   */
  public int SGcod_layers;

  /**
   * Multiple component transformation usage. Use the following constants to extract information:
   * <ul>
   * <li>{@link #MASK_MCT_YCC} = {@value #MASK_MCT_YCC}</li>
   * <li>{@link #MASK_MCT_ARRAY_BASED} = {@value #MASK_MCT_ARRAY_BASED}</li>
   * <li>{@link #MASK_MCT_WAVELET_BASED} = {@value #MASK_MCT_WAVELET_BASED}</li>
   * </ul>
   */
  public int SGcod_MCT;

  /** SSO overlap values */
  public int SPcod_SSO;

  private boolean extended;

  @Override
  public Marker getMarker() {
    return Marker.COD;
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lcod = source.readUnsignedShort();
    Scod = source.readUnsignedByte();
    SGcod_order = source.readUnsignedByte();
    SGcod_layers = source.readUnsignedShort();
    SGcod_MCT = source.readUnsignedByte();
    SP_NL = source.readUnsignedByte();
    SP_xcb = source.readUnsignedByte() + 2;
    SP_ycb = source.readUnsignedByte() + 2;
    SP_modes = source.readUnsignedByte();
    SP_kernel = source.readUnsignedByte();

    extended = T801_SINGLE_SAMPLE_OVERLAP.isUsedBy(codestream);
    if (extended) {
      SPcod_SSO = source.readUnsignedByte();
    }

    if (Parameters.isSet(Scod, MASK_CODING_USER_PRECINCTS)) {
      final int numParams = SP_NL + 1;
      SP_precincts = new int[numParams];
      for (int i = 0; i < numParams; i++) {
        SP_precincts[i] = source.readUnsignedByte();
      }
    }
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lcod);
    sink.writeByte(Scod);
    sink.writeByte(SGcod_order);
    sink.writeShort(SGcod_layers);
    sink.writeByte(SGcod_MCT);
    sink.writeByte(SP_NL);
    sink.writeByte(SP_xcb);
    sink.writeByte(SP_ycb);
    sink.writeByte(SP_modes);
    sink.writeByte(SP_kernel);

    if (extended) {
      sink.writeByte(SPcod_SSO);
    }

    if (Parameters.isSet(Scod, MASK_CODING_USER_PRECINCTS)) {
      for (int precinct : SP_precincts) {
        sink.writeByte(precinct);
      }
    }
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {
    Validate.inRange("Lcod", Lcod, 12, 45);
    Validate.inRange("SGcod:progression order", SGcod_order, ProgressionOrders.VALUE_PROGRESSION_RLCP,
        ProgressionOrders.VALUE_PROGRESSION_CPRL);
    Validate.inRange("SGcod:layers", SGcod_layers, MIN_LAYERS, MAX_LAYERS);

    // TODO implement further validation

  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lcod", Lcod, "COD.L"));
    markerInfo.add(new PropertiesParameterInfo("Scod", Integer.toBinaryString(Scod), "COD.S"));
    markerInfo.add(new PropertiesParameterInfo("Order (SGcod)", SGcod_order, "COD.SG.order"));
    markerInfo.add(new PropertiesParameterInfo("Layers (SGcod)", SGcod_layers, "COD.SG.layers"));
    markerInfo.add(new PropertiesParameterInfo("MCT (SGcod)", SGcod_MCT, "COD.SG.mct"));
    markerInfo.add(new PropertiesParameterInfo("NL (SPcod)", SP_NL, "COD.SP.NL"));
    markerInfo.add(new PropertiesParameterInfo("xcb (SPcod)", SP_xcb, "COD.SP.xcb"));
    markerInfo.add(new PropertiesParameterInfo("ycb (SPcod)", SP_ycb, "COD.SP.ycb"));
    markerInfo.add(new PropertiesParameterInfo("Modes (SPcod)", SP_modes, "COD.SP.modes"));
    markerInfo.add(new PropertiesParameterInfo("Kernel (SPcod)", SP_kernel, "COD.SP.kernel"));

    if (extended) {
      markerInfo.add(new PropertiesParameterInfo("SSO (SPcod)", SPcod_SSO, "COD.SP.sso"));
    }

    if (SP_precincts != null) {
      markerInfo.add(new PropertiesParameterInfo("Precinct (SPcod)", "COD.SP.precincts"));
      for (int i = 0; i < SP_precincts.length; i++) {
        markerInfo.add(new DirectParameterInfo("->[" + i + "]", SP_precincts[i]));
      }
    }
  }

}
