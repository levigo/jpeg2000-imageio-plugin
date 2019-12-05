package com.levigo.jadice.format.jpeg2000.internal.debug;

import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.levigo.jadice.document.internal.LogMessages;
import org.jadice.util.log.qualified.QualifiedLogger;

public class WritingProtocolListener implements ProtocolListener, Closeable {

  private static final QualifiedLogger LOGGER = getQualifiedLogger(WritingProtocolListener.class);

  private final ImageOutputStream stream;

  public WritingProtocolListener(OutputStream stream) throws IOException {
    this.stream = new MemoryCacheImageOutputStream(stream);
  }

  @Override
  public void newProtocolElement(ProtocolElement element) {
    try {
      element.token().write(stream);
      element.parameter().write(stream);
    } catch (IOException e) {
      LOGGER.fatal(LogMessages.IO_EXCEPTION, e);
    }
  }

  public void close() {
    try {
      stream.close();
    } catch (IOException e) {
      LOGGER.fatal(LogMessages.IO_EXCEPTION, e);
    }
  }
}
