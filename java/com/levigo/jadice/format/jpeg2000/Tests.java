package com.levigo.jadice.format.jpeg2000;

import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

import java.io.IOException;
import java.io.InputStream;

import com.levigo.jadice.document.io.IOUtils;
import com.levigo.jadice.document.io.SeekableInputStream;
import org.jadice.util.log.qualified.QualifiedLogger;

public class Tests {

  private static final QualifiedLogger LOGGER = getQualifiedLogger(Tests.class);

  public static SeekableInputStream openResource(String fullQualifiedResourcePath) throws IOException {
    LOGGER.info("Searching for " + fullQualifiedResourcePath);
    final InputStream inputStream = Tests.class.getResourceAsStream(fullQualifiedResourcePath);
    LOGGER.info("Found " + inputStream);
    return IOUtils.wrap(inputStream);
  }

}
