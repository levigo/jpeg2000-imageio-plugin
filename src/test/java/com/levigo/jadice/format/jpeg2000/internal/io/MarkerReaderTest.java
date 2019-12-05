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
package com.levigo.jadice.format.jpeg2000.internal.io;

import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

import com.levigo.jadice.document.io.MemoryInputStream;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.internal.marker.Marker;
import org.jadice.util.log.qualified.QualifiedLogger;

import org.junit.Assert;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MarkerReaderTest {
  
  private static final QualifiedLogger LOGGER = getQualifiedLogger(MarkerReaderTest.class);

  private SeekableInputStream getInput(List<Marker> markers, int paddingAfter, int paddingBefore) {
    final int dataSize = (markers.size() * paddingBefore) + (markers.size() * 2) + (markers.size() *  paddingAfter);
    final byte[] inputBytes = new byte[dataSize];

    final Random random = new Random(System.currentTimeMillis());
    int i = 0;
    for (Marker marker : markers) {
      for (int j = 0; j < paddingBefore; j++) {
        inputBytes[i++] = (byte) random.nextInt(255);
      }

      inputBytes[i++] = (byte) ((marker.code >> 8) & 0xFF);
      inputBytes[i++] = (byte) (marker.code & 0xFF);

      for (int j = 0; j < paddingAfter; j++) {
        inputBytes[i++] = (byte) random.nextInt(255);
      }
    }

    return new MemoryInputStream(inputBytes);
  }

  protected List<Marker> getMarkers() {
    final Marker[] values = Marker.values();
    final List<Marker> markers = new ArrayList<>(values.length - 1);
    for (Marker marker : values) {
      if (marker != Marker.RES) {
        markers.add(marker);
      }
    }
    return markers;
  }

  @Test
  public void nextWithImplicitSearch() throws IOException {
    nextMarkerTest(null, 100, 100);
  }

  @Test
  public void nextWithExplicitSearch() throws IOException {
    nextMarkerTest(true, 100, 100);
  }

  @Test
  public void nextWithoutSearch() throws IOException {
    nextMarkerTest(false, 0, 0);
  }

  private void nextMarkerTest(Boolean search, int paddingAfter, int paddingBefore) throws IOException {
    final List<Marker> markers = getMarkers();

    final SeekableInputStream inputStream = getInput(markers, paddingAfter, paddingBefore);
    final MarkerReader markerReader = new MarkerReader();
    for (Marker expected : markers) {
      final String expectedIdentifier = expected.name() + " (" + Integer.toHexString(expected.code) + ")";
      final StringBuilder stringBuilder = new StringBuilder("Searching for " + expectedIdentifier + " ... ");

      final Marker actual = search == null ? markerReader.next(inputStream) : markerReader.next(inputStream, search);

      stringBuilder.append("Found " + expectedIdentifier + " ... ");

      if (expected != actual) {
        stringBuilder.append("FAILED");
      } else {
        stringBuilder.append("OK");
      }
      
      LOGGER.info(stringBuilder.toString());

      Assert.assertEquals(expected, actual);
    }
  }

  @Test
  public void nextWithoutSearchReturnsNull() throws IOException {
    final byte[] inputBytes = new byte[]{0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77};
    final SeekableInputStream inputStream = new MemoryInputStream(inputBytes);
    final MarkerReader markerReader = new MarkerReader();
    final Marker actual = markerReader.next(inputStream, false);
    Assert.assertNull(actual);
  }

  @Test(expected = EOFException.class)
  public void nextWithImplicitSearchEOF() throws IOException {
    final byte[] inputBytes = new byte[]{0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77};
    final SeekableInputStream inputStream = new MemoryInputStream(inputBytes);
    final MarkerReader markerReader = new MarkerReader();
    markerReader.next(inputStream);
  }

  @Test(expected = EOFException.class)
  public void nextWithExplicitSearchEOF() throws IOException {
    final byte[] inputBytes = new byte[]{0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77};
    final SeekableInputStream inputStream = new MemoryInputStream(inputBytes);
    final MarkerReader markerReader = new MarkerReader();
    markerReader.next(inputStream, true);
  }

  @Test(expected = EOFException.class)
  public void nextWithoutSearchEOF() throws IOException {
    final byte[] inputBytes = new byte[]{0x11};
    final SeekableInputStream inputStream = new MemoryInputStream(inputBytes);
    final MarkerReader markerReader = new MarkerReader();
    markerReader.next(inputStream, false);
  }

}
