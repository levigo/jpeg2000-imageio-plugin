package com.levigo.jadice.format.jpeg2000.internal.io;

import com.levigo.jadice.format.jpeg2000.internal.marker.Marker;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

public class MarkerReader {

  /**
   * Reads the given input until a valid marker code was found.
   *
   * @param source the input providing codestream bytes
   * @return a {@link Marker} if found.
   * @throws java.io.IOException if general I/O error occured.
   * @throws java.io.EOFException
   */
  public Marker next(ImageInputStream source) throws IOException {
    int markerCode;

    int read = (source.readByte() & 0xFF);
    do {
      while (read != 0xFF) {
        read = (source.readByte() & 0xFF);
      }

      markerCode = (read << 8) | (read = ((source.readByte() & 0xFF)));

    } while (!Marker.isValid(markerCode));

    return Marker.byCode(markerCode);
  }

  /**
   * This method has two different behaviours. If search is allowed, {@link com.levigo.jadice.format.jpeg2000.internal.io.MarkerReader#next(javax.imageio.stream.ImageInputStream)}
   * is called. If search is not allowed, two bytes will be read. If the result is a valid marker code a {@link
   * com.levigo.jadice.format.jpeg2000.internal .marker.Marker} will be created and returned. If the result is not a
   * valid marker code <code>null</code> is returned.
   *
   * @param source the input providing codestream bytes
   * @param search flag if the reading operation should search for the next marker code and therefore read until one is
   *               recognized.
   * @return A {@link Marker} if present or found. Otherwise <code>null</code>.
   * @throws java.io.IOException
   */
  public Marker next(ImageInputStream source, boolean search) throws IOException {
    if (search) {
      return next(source);
    }

    final int markerCode = source.readUnsignedShort();
    if (Marker.isValid(markerCode)) {
      return Marker.byCode(markerCode);
    }

    return null;
  }
}
