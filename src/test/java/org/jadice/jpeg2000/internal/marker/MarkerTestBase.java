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
package org.jadice.jpeg2000.internal.marker;

import static org.jadice.jpeg2000.internal.marker.CodestreamTests.createSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.codestream.Codestream;
import org.jadice.jpeg2000.internal.marker.Marker;
import org.jadice.jpeg2000.internal.marker.MarkerSegment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import com.levigo.jadice.document.io.IOUtils;
import com.levigo.jadice.document.io.SeekableInputStream;

public abstract class MarkerTestBase<M extends MarkerSegment> {
  
  @Parameter(0)
  public String path;

  private Marker marker;
  private Codestream codestream;

  @Before
  public void setup() {
    marker = marker();
    codestream = codestream();
  }
  
  protected Codestream codestream() {
    return new Codestream();
  }

  protected abstract Marker marker();

  protected void testReading(M m, ImageInputStream source) throws IOException, JPEG2000Exception {
    assertNotNull(m);
    assertNotNull(source);

    // Ensure the stream begins with an SIZ marker code (0xFF51) and forms a completely valid marker segment.
    final int markerCode = source.readUnsignedShort();
    assertEquals(marker.code, markerCode);

    m.read(source, new Codestream(), false);

    inspect(m);
  }
  
  protected abstract void inspect(M underTest);

  @Test
  public void readWithoutValidation() throws IOException, JPEG2000Exception, URISyntaxException {
    testReading((M) marker.createMarkerSegment(), createSource(path));
  }

  @Test
  public void writeWithoutValidation() throws IOException, JPEG2000Exception {
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    final MemoryCacheImageOutputStream outputStream = new MemoryCacheImageOutputStream(byteArrayOutputStream);

    try {
      final M m = prepareMarker();
      m.write(outputStream, new Codestream(), false);
      outputStream.flush();

      final byte[] bytes = byteArrayOutputStream.toByteArray();
      final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
      final SeekableInputStream inputStream = IOUtils.wrap(byteArrayInputStream);

      testReading((M) marker.createMarkerSegment(), inputStream);
    } catch (UnsupportedOperationException e) {
      // Thats ok. Some markers lack implementation of writing support. 
    }
  }
  
  protected abstract M prepareMarker() ;
}
