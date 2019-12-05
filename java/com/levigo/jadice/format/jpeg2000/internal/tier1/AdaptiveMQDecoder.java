package com.levigo.jadice.format.jpeg2000.internal.tier1;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;

@Refer(to = Spec.J2K_CORE, page = 151, section = "Annex J.1", called = "Software Conventions Adaptive Entropy Decoder")
public class AdaptiveMQDecoder extends DefaultMQDecoder {

  public AdaptiveMQDecoder(ImageInputStream input) throws IOException {
    super(input);
  }

  @Override
  protected void init() throws IOException {
    streamPos0 = input.getStreamPosition();

    markerFound = false;

    b = input.read();

    c = (b ^ 0xFF) << 16;

    byteIn();

    c <<= 7;
    ct -= 7;
    a = 0x8000;
  }

  @Override
  public int decode(ContextContainer cx) throws IOException {
    int d = cx.mps();

    final int icx = cx.cx();
    final int qeValue = QE[icx][VALUE];

    a -= qeValue;

    if ((c >>> 16) < a) {
      if (a < 0x8000) {
        d = mpsExchange(cx, icx, qeValue);
      }
    } else {
      c -= a << 16;
      d = lpsExchange(cx, icx, qeValue);
    }

    return d;
  }

  @Override
  protected void byteIn() throws IOException {
    if (!markerFound) {
      if (b == 0xFF) {
        b = input.read() & 0xFF;
        if (b > 0x8F) {
          markerFound = true;
          ct = 8;
        } else {
          c += 0xFE00 - (b << 9);
          ct = 7;
        }
      } else {
        b = input.read() & 0xFF;
        c += 0xFF00 - (b << 8);
        ct = 8;
      }
    } else {
      // software-convention decoder: c unchanged
      ct = 8;
    }

    c &= 0xffffffffL;
  }

}
