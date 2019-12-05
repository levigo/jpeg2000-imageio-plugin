package com.levigo.jadice.format.jpeg2000.internal.fileformat;

import java.util.HashMap;
import java.util.Map;

public enum BoxType {
  /** Contains the codestream as defined by <i>ITU-T.800, Annex A</i>. */
  ContiguousCodestream(true, 0x6A703263) {
    @Override
    public Box createBox() {
      return new ContiguousCodestreamBox();
    }
  },
  
  /** Uniquely identifies the file as being part of the JPEG 2000 family of files. */
  JPEG2000Signature(true, 0x6a502020) {
    @Override
    public Box createBox() {
      return new JPEG2000SignatureBox();
    }
  },

  /**
   * Specifies file type, version and compatibility information, including specifying if this file is a conforming JP2
   * file or if it can be read by a conforming JP2 reader.
   */
  FileType(true, 0x66747970) {
    @Override
    public Box createBox() {
      return new FileTypeBox();
    }
  },

  /** Specifies the grid resolution at which the image was captured. */
  CaptureResolution(0x72657363) {
    @Override
    public Box createBox() {
      return new CaptureResolutionBox();
    }
  },

  /** Specifies the default grid resolution at which the image should be displayed. */
  DefaultDisplayResolution(0x72657364) {
    @Override
    public Box createBox() {
      return new DefaultDisplayResolutionBox();
    }
  },

  /** Contains the grid resolution. */
  Resolution(0x72657320,
      CaptureResolution,
      DefaultDisplayResolution) {
    @Override
    public Box createBox() {
      return new ResolutionBox();
    }
  },

  /** Specifies the size of the image and other related fields. */
  ImageHeader(true, 0x69686472) {
    @Override
    public Box createBox() {
      return new ImageHeaderBox();
    }
  },

  /**
   * Specifies the bit depth of the components in the file in cases where the bit depth is not constant across all
   * components.
   */
  BitsPerComponent(0x62706363) {
    @Override
    public Box createBox() {
      return new BitsPerComponentBox();
    }
  },

  /** Specifies the colour space of the image. */
  ColourSpecification(true, 0x636F6C72) {
    @Override
    public Box createBox() {
      return new ColourSpecificationBox();
    }
  },

  /** Specifies the palette which maps a single component in index space to a multiple-component image. */
  Palette(0x70636C72) {
    @Override
    public Box createBox() {
      return new PaletteBox();
    }
  },

  /** Specifies the mapping between a palette and codestream components. */
  ComponentMapping(0x636D6170) {
    @Override
    public Box createBox() {
      return new ComponentMappingBox();
    }
  },

  /**
   * Specifies the type and ordering of the components within the codestream, as well as those created by the
   * application of a palette.
   */
  ChannelDefinition(0x63646566) {
    @Override
    public Box createBox() {
      return new ChannelDefinitionBox();
    }
  },

  /** Contains a series of boxes that contain header-type information about the file. */
  JP2Header(true, 0x6A703268,
      ImageHeader,
      BitsPerComponent,
      ColourSpecification,
      Palette,
      ComponentMapping,
      ChannelDefinition,
      Resolution) {
    @Override
    public Box createBox() {
      return new JP2HeaderBox();
    }
  },

  /** Contains intellectual property information about the image. */
  IntellectualProperty(0x6A703269) {
    @Override
    public Box createBox() {
      return new IntellectualPropertyBox();
    }
  },

  /** Provides a tool by which vendors can add XML formatted information to a JP2 file. */
  XML(0x786D6C20) {
    @Override
    public Box createBox() {
      return new XMLBox();
    }
  },

  /**
   * Provides a tool by which vendors can add additional information to a file without risking conflict with other
   * vendors.
   */
  UUID(0x75756964){
    @Override
    public Box createBox() {
      return new UUIDBox();
    }
  },

  /** Specifies a list of UUIDs. */
  UUIDList(0x75637374){
    @Override
    public Box createBox() {
      return new UUIDListBox();
    }
  },

  /** Specifies a URL. */
  URL(0x75726C20) {
    @Override
    public Box createBox() {
      return new URLBox();
    }
  },

  /** Provides a tool by which a vendor may provide access to additional information associated with a UUID. */
  UUIDInfo(0x75696E66,
      UUIDList,
      URL) {
    @Override
    public Box createBox() {
      return new UUIDInfoBox();
    }
  },

  Unknown(0x0) {
    @Override
    public Box createBox() {
      return new UnknownBox();
    }
  };

  public final boolean required;
  public final boolean superbox;
  public final long type;
  public final BoxType[] subboxes;

  BoxType(final boolean required, final long type, final BoxType... subboxes) {
    this.required = required;
    this.type = type;
    this.superbox = subboxes != null;
    this.subboxes = subboxes;
  }

  BoxType(final int type, final BoxType... subboxes) {
    this(false, type);
  }

  @Override
  public String toString() {
    return name() + " (" + Long.toHexString(type) + ")";
  }

  private static final Map<Long, BoxType> typesByCode = new HashMap<>();

  static {
    for (BoxType boxType : BoxType.values()) {
      typesByCode.put(boxType.type, boxType);
    }
  }

  /**
   * @param type (unsigned int representing) the box type.
   *
   * @return always one of the defined {@link BoxType}s. If the box type is unknown, not supported or not yet
   * implemented the return value will be {@link BoxType#Unknown}.
   */
  public static BoxType byType(long type) {
    final BoxType boxType = typesByCode.get(type);
    return boxType != null ? boxType : Unknown;
  }

  /**
   * Validates the given box type value. The validation supports all boxes defined in <i>ITU-T.800</i> and
   * <i>ITU-T.801</i>.
   *
   * @param type the (unsigned int) value that should be tested.
   *
   * @return <type>true</type> if the marker type is valid as defined in <i>ITU-T.800</i> and <i>ITU-T.801</i>,
   * <type>false</type> if not.
   */
  public static boolean isValid(long type) {
    return typesByCode.containsKey((int) type);
  }
  
  public abstract Box createBox();
}
