package com.levigo.jadice.format.jpeg2000.internal;

import static com.levigo.jadice.format.jpeg2000.internal.debug.BasicProtocolElement.newElement;

import java.io.EOFException;
import java.io.FileInputStream;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.levigo.jadice.format.jpeg2000.internal.debug.BasicProtocolElement;
import com.levigo.jadice.format.jpeg2000.internal.debug.Parameter;
import com.levigo.jadice.format.jpeg2000.internal.debug.ParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.debug.PrintlineProtocolListener;
import com.levigo.jadice.format.jpeg2000.internal.debug.Protocol;
import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolListener;
import com.levigo.jadice.format.jpeg2000.internal.debug.ProtocolToken;
import com.levigo.jadice.format.jpeg2000.internal.debug.tcq.BasicQuantizationProtocol;

public class ReadProtocol {
  public static void main(String[] args) throws Exception {
    final String filepath = "E:\\work\\support\\jsx\\2655\\im4.j2k.kdu.dq";
    final FileInputStream source = new FileInputStream(filepath);
    final ImageInputStream input = new MemoryCacheImageInputStream(source);
    final Protocol protocol = new BasicQuantizationProtocol();
    final ProtocolListener protocolListener = new PrintlineProtocolListener();

    while (true) {
      try {
        final long symbol = input.readBits(protocol.tokenLength());
        for (ProtocolToken protocolToken : protocol.knownTokens()) {
          if (protocolToken.matches(symbol)) {
            final ParameterFactory parameterFactory = protocolToken.getParameterFactory();
            final Parameter referenceParameter = parameterFactory.create();
            referenceParameter.read(input);
            final BasicProtocolElement element = newElement(protocolToken, referenceParameter);
            protocolListener.newProtocolElement(element);
          }
        }
      } catch (EOFException e) {
        break;
      }
    }

  }
}
