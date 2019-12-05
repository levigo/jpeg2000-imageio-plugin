package com.levigo.jadice.format.jpeg2000;


import java.io.IOException;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import com.levigo.jadice.document.Format;
import com.levigo.jadice.document.FormatReader;
import com.levigo.jadice.document.ProductInformation;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.internal.JPEG2000FormatReader;

import org.jadice.util.log.Logger;
import org.jadice.util.log.LoggerFactory;

public class JPEG2000Format extends Format {

  private static final String JPEG2000 = "JPEG2000";

  private static final Logger LOGGER = LoggerFactory.getLogger(JPEG2000Format.class);

  @Override
  public String[] getDefaultExtensions() {
    return new String[]{
        "jp2", "j2k", "JP2", "J2K", "jpf", "jpg2", "jpx"
    };
  }

  @Override
  public String getDescription() {
    return JPEG2000;
  }

  @Override
  public String getName() {
    return JPEG2000;
  }

  @Override
  public String getVersion() {
    return ProductInformation.getVersion();
  }

  @Override
  public MimeType getMimeType() {
    try {
      return new MimeType("image", "jpx");
    } catch (MimeTypeParseException e) {
      // This never happens, as the MimeType parser cannot be as dumb.
      LOGGER.error(e.getMessage(), e);
    }

    return null;
  }

  @Override
  public boolean matches(SeekableInputStream stream) throws IOException {
    return JPEG2000Matcher.isFileFormat(stream) || JPEG2000Matcher.isCodestream(stream);
  }

  @Override
  public FormatReader createReader() {
    return new JPEG2000FormatReader();
  }

}
