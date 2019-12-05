package com.levigo.jadice.format.jpeg2000.msg;

import org.jadice.util.log.message.LogMessage;
import org.jadice.util.log.qualified.MessageID;

import com.levigo.jadice.document.ProductInformation;

public enum CodestreamMessages implements MessageID {

  @LogMessage("Codestream must start with an SOC marker")
  EXPECTED_SOC_MARKER,

  @LogMessage("Codestream must contain a SIZ marker immediately after the SOC marker")
  EXPECTED_SIZ_MARKER,

  @LogMessage("The first tile is required to have a non-empty intersection with the image on the high resolution grid.")
  EMPTY_TILE_INTERSECTION,

  @LogMessage("Could not create JPEG2000 codestream representation")
  UNABLE_TO_CREATE_CODESTREAM_REPRESENTATION,

  @LogMessage("Found QCD marker segment with unknown format.")
  MARKER_SEGMENT_FORMAT_FOR_QCD_UNKNOWN,

  @LogMessage("Found QCC marker segment with unknown format.")
  MARKER_SEGMENT_FORMAT_FOR_QCC_UNKNOWN,

  @LogMessage("Unsupported codestream feature: {0}.")
  UNSUPPORTED_CODESTREAM_FEATURE,

  @LogMessage("Codestream signaled the use of SOP marker segments but does not contain them consistently.")
  INCONSISTENT_USE_OF_SOP,

  @LogMessage("Codestream signaled the use of EPH marker segments but does not contain them consistently.")
  INCONSISTENT_USE_OF_EPH,

  @LogMessage("Skipping {0} bytes.")
  SKIPPING_MARKER_SEGMENT_BYTES,

  @LogMessage("Codestream should be aligned at SOT marker segment but signaled {0}.")
  EXPECTED_SOT_MARKER,

  @LogMessage("Already found a SIZ marker segment.")
  ALREADY_FOUND_SIZ,

  @LogMessage("Missing source for reading.")
  MISSING_SOURCE_FOR_READING,

  @LogMessage("Invalid SOT address. Source position {0} does not point to a SOT marker segment.")
  INVALID_SOT_ADDRESS,

  @LogMessage("Tile header for tile {0} could not be found.")
  MISSING_TILE_HEADER,

  @LogMessage("Missing COD or COC marker segment.")
  MISSING_CODING_MARKER_SEGMENT,

  @LogMessage("Illegal or unknown progression order {0}.")
  ILLEGAL_PROGRESSION_ORDER,

  @LogMessage("Illegal value for {0}: {1}.")
  ILLEGAL_PARAMETER_VALUE,

  @LogMessage("Marker segment length overflow.")
  MARKER_SEGMENT_LENGTH_OVERFLOW,

  @LogMessage("Missing tile-parts.")
  MISSING_TILE_PARTS,

  @LogMessage("Unsupported marker {0}.")
  UNSUPPORTED_MARKER;

  private static final String COMPONENT_ID = ProductInformation.getProductId() + ".FORMAT.JPEG2000.CODESTREAM";

  @Override
  public String getComponentID() {
    return COMPONENT_ID;
  }

}
