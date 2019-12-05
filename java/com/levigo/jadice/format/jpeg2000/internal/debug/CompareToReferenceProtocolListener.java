package com.levigo.jadice.format.jpeg2000.internal.debug;

import static com.levigo.jadice.format.jpeg2000.internal.debug.BasicProtocolElement.newElement;
import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.levigo.jadice.document.internal.LogMessages;
import org.jadice.util.log.qualified.QualifiedLogger;

public class CompareToReferenceProtocolListener implements ProtocolListener {

  private static final QualifiedLogger LOGGER = getQualifiedLogger(CompareToReferenceProtocolListener.class);

  private final ImageInputStream referenceProtocolInput;
  private final Protocol protocol;

  private long numCompared;

  public CompareToReferenceProtocolListener(FileInputStream referenceInput, Protocol protocol)
      throws FileNotFoundException {
    this.referenceProtocolInput = new MemoryCacheImageInputStream(referenceInput);
    this.protocol = protocol;
    numCompared = 0;
  }

  @Override
  public void newProtocolElement(ProtocolElement myElement) {
    try {
      final long token = referenceProtocolInput.readBits(protocol.tokenLength());
      for (ProtocolToken protocolToken : protocol.knownTokens()) {
        if (protocolToken.matches(token)) {
          final ParameterFactory parameterFactory = protocolToken.getParameterFactory();
          final Parameter referenceParameter = parameterFactory.create();
          referenceParameter.read(referenceProtocolInput);
          final BasicProtocolElement referenceElement = newElement(protocolToken, referenceParameter);
          if (!myElement.matches(referenceElement)) {
            System.err.println(numCompared + ". element differs:\n    actual: " + myElement
                + "\n  expected: " + referenceElement);
          }
          numCompared++;
        }
      }

    } catch (IOException e) {
      LOGGER.fatal(LogMessages.IO_EXCEPTION, e);
    }
  }
}
