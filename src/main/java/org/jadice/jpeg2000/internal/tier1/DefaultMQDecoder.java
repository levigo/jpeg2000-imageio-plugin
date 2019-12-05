/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jadice.jpeg2000.internal.tier1;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;

/**
 * This class represents the arithmetic entropy decoder procedures, described in <i>ITU.T.800, Annex C</i>.
 */
@Refer(to = Spec.J2K_CORE, page = 64, section = "Annex C", called = "Arithmetic Entropy Coding")
public class DefaultMQDecoder implements MQDecoder {

  protected static final int SWITCH = 3;

  protected static final int NLPS = 2;

  protected static final int NMPS = 1;

  protected static final int VALUE = 0;

  // @formatter:off
  protected static final int[][] QE = {{
      0x5601,  1,  1, 1 }, {
      0x3401,  2,  6, 0 }, {
      0x1801,  3,  9, 0 }, {
      0x0AC1,  4, 12, 0 }, {
      0x0521,  5, 29, 0 }, {
      0x0221, 38, 33, 0 }, {
      0x5601,  7,  6, 1 }, {
      0x5401,  8, 14, 0 }, {
      0x4801,  9, 14, 0 }, {
      0x3801, 10, 14, 0 }, {
      0x3001, 11, 17, 0 }, {
      0x2401, 12, 18, 0 }, {
      0x1C01, 13, 20, 0 }, {
      0x1601, 29, 21, 0 }, {
      0x5601, 15, 14, 1 }, {
      0x5401, 16, 14, 0 }, {
      0x5101, 17, 15, 0 }, {
      0x4801, 18, 16, 0 }, {
      0x3801, 19, 17, 0 }, {
      0x3401, 20, 18, 0 }, {
      0x3001, 21, 19, 0 }, {
      0x2801, 22, 19, 0 }, {
      0x2401, 23, 20, 0 }, {
      0x2201, 24, 21, 0 }, {
      0x1C01, 25, 22, 0 }, {
      0x1801, 26, 23, 0 }, {
      0x1601, 27, 24, 0 }, {
      0x1401, 28, 25, 0 }, {
      0x1201, 29, 26, 0 }, {
      0x1101, 30, 27, 0 }, {
      0x0AC1, 31, 28, 0 }, {
      0x09C1, 32, 29, 0 }, {
      0x08A1, 33, 30, 0 }, {
      0x0521, 34, 31, 0 }, {
      0x0441, 35, 32, 0 }, {
      0x02A1, 36, 33, 0 }, {
      0x0221, 37, 34, 0 }, {
      0x0141, 38, 35, 0 }, {
      0x0111, 39, 36, 0 }, {
      0x0085, 40, 37, 0 }, {
      0x0049, 41, 38, 0 }, {
      0x0025, 42, 39, 0 }, {
      0x0015, 43, 40, 0 }, {
      0x0009, 44, 41, 0 }, {
      0x0005, 45, 42, 0 }, {
      0x0001, 45, 43, 0 }, {
      0x5601, 46, 46, 0 }
  };
  // @formatter:on

  protected int a;
  protected long c;
  protected int ct;

  protected long b;

  protected long streamPos0;

  protected ImageInputStream input;

  protected boolean markerFound;

  public DefaultMQDecoder(ImageInputStream input) throws IOException {
    this.input = input;
    init();
  }
  
  protected DefaultMQDecoder() {
    
  }
  
  protected void init() throws IOException {
    streamPos0 = input.getStreamPosition();
    b = input.read();

    c = (b << 16);

    byteIn();

    c <<= 7;
    ct -= 7;
    a = 0x8000;
  }

  public int decode(ContextContainer cx) throws IOException {
    int d = cx.mps();

    final int icx = cx.cx();
    final int qeValue = QE[icx][VALUE];

    a -= qeValue;

    if ((c >>> 16) < qeValue) {
      d = lpsExchange(cx, icx, qeValue);
    } else {
      c -= (qeValue << 16);
      if ((a & 0x8000) == 0) {
        d = mpsExchange(cx, icx, qeValue);
      }
    }

    return d;
  }

  @Override
  public int decodeRaw() throws IOException {
    throw new UnsupportedOperationException("raw decoding is not supported by this mq decoder.");
  }

  protected void byteIn() throws IOException {
    if (input.getStreamPosition() > streamPos0) {
      input.seek(input.getStreamPosition() - 1);
    }

    b = input.read();


    if (b == 0xFF) {
      final long b1 = input.read();
      if (b1 > 0x8f) {
        markerFound = true;
        c += 0xff00;
        ct = 8;
        input.seek(input.getStreamPosition() - 2);
      } else {
        c += b1 << 9;
        ct = 7;
      }
    } else {
      b = input.read();
      c += b << 8;
      ct = 8;
    }

    c &= 0xffffffffL;
  }

  protected void renormalize() throws IOException {
    do {
      renormalizeOnce();
    } while ((a & 0x8000) == 0);

    c &= 0xffffffffL;
  }

  private void renormalizeOnce() throws IOException {
    if (ct == 0) {
      byteIn();
    }

    a <<= 1;
    c <<= 1;
    ct--;
  }

  protected int mpsExchange(ContextContainer cx, int icx, int qeValue) throws IOException {
    int mps = cx.mps();

    if (a < qeValue) {
      if (QE[icx][SWITCH] == 1) {
        cx.toggleMps();
      }

      cx.setCx(QE[icx][NLPS]);
      renormalize();
      return 1 - mps;
    } else {
      cx.setCx(QE[icx][NMPS]);
      renormalizeOnce();
      return mps;
    }
  }

  protected int lpsExchange(ContextContainer cx, int icx, int qeValue) throws IOException {
    final int mps = cx.mps();

    if (a < qeValue) {
      a = qeValue;
      cx.setCx(QE[icx][NMPS]);
      renormalizeOnce();
      return mps;
    } else {
      if (QE[icx][SWITCH] == 1) {
        cx.toggleMps();
      }

      cx.setCx(QE[icx][NLPS]);
      a = qeValue;
      renormalize();
      return 1 - mps;
    }
  }

  int getA() {
    return a;
  }

  long getC() {
    return c;
  }

  public boolean checkPredictableTermination() throws IOException {
    int k; // Number of bits that where added in the termination process
    int q;

    // 1) If everything has been OK, 'b' must be 0xFF if a terminating marker hasn't yet been found.
    if (b != 0xFF && !markerFound)
      return true;

    // 2) If ct is not 0, we must have already reached the terminating marker.
    if (ct != 0 && !markerFound)
      return true;

    // 3) If ct is 1 there where no spare bits at the encoder, this is all that we can check
    if (ct == 1)
      return false;

    // 4) if ct is 0, then next byte must be the second byte of a terminating marker (i.e. larger
    // than 0x8F) if the terminating marker has not been reached yet.
    if (ct == 0) {
      if (!markerFound) {
        // Get next byte and check
        b = input.read() & 0xFF;
        if (b <= 0x8F)
          return true;
      }
      // Adjust ct for last byte
      ct = 8;
    }

    // 5) Now we can calculate the number 'k' of bits having error resilience information, which is
    // the number of bits left to normalization in register 'c', minus 1.
    k = ct - 1;

    // 6) The predictable termination policy is as if an LPS interval was coded that caused a
    // re-normalization of 'k' bits, before the termination marker started

    // We first check if an LPS is decoded, that causes a re-normalization of 'k' bits. Worst case
    // is smallest LPS probability 'q' that causes a re-normalization of 'k' bits.
    q = 0x8000 >> k;

    // Check that we can decode an LPS interval of probability 'q'
    a -= q;
    if ((c >>> 16) < a) {
      // Error: MPS interval decoded
      return true;
    }
    // OK: LPS interval decoded
    c = ((c - (a << 16)) & 0xffffffffL);
    // -- LPS Exchange
    // Here 'a' can not be smaller than 'q' because the minimum value for 'a' is
    // 0x8000-0x4000=0x4000 and 'q' is set to a value equal to or smaller than that.
    a = q;
    
    renormalize();
    // -- End LPS Exchange

    // 7) Everything seems OK, we have checked register 'c' for the LPS symbols and ensured that
    // it is followed by bits synthesized by the termination marker.
    return false;
  }

}
