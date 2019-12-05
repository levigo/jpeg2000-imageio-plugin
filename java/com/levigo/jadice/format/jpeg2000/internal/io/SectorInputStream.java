package com.levigo.jadice.format.jpeg2000.internal.io;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

import com.levigo.jadice.document.io.SeekableInputStream;

/**
 * A wrapper for an {@link ImageInputStream} which is able to provide a view of a specific part of the wrapped stream.
 * <p>
 * <b>Note</b>: Read access to the wrapped stream is synchronized. Users of this stream needs to deal with
 * synchronization against other users of the same instance, but not against other users of the wrapped stream.
 */
public class SectorInputStream extends SeekableInputStream {

  private static final int BUFFER_SIZE_DEFAULT = 4096;

  private final ImageInputStream source;

  /** The absolute position in the wrapped stream at which the window starts. */
  private final long offset;

  /** The length of the window. Length is an relative value. */
  private final long length;

  /** A buffer which is used to improve read performance. */
  private final byte buffer[];

  /** Location of the first byte in the buffer with respect to the start of the stream. */
  private long bufferBase;

  /** Location of the last byte in the buffer with respect to the start of the stream. */
  private long bufferTop;

  private long streamPos;
  

  /**
   * Creates a new {@link com.levigo.jadice.format.jpeg2000.internal.io.SectorInputStream} which provides a view of the
   * wrapped stream. Convenience
   * argument list using a buffer with a default size of 4096 bytes.
   *
   * @param source the stream to be wrapped.
   * @param offset the absolute position in the wrapped stream at which this view starts.
   * @param length the length of the stream's view.
   */
  public SectorInputStream(final ImageInputStream source, final long offset, final long length) {
    this(source, offset, length, BUFFER_SIZE_DEFAULT);
  }

  /**
   * Creates a new {@link com.levigo.jadice.format.jpeg2000.internal.io.SectorInputStream} which provides a view of the
   * wrapped stream.
   *
   * @param source     the stream to be wrapped.
   * @param offset     the absolute position in the wrapped stream at which this view starts.
   * @param length     the length of the stream's view.
   * @param bufferSize the size of the byte buffer.
   */
  public SectorInputStream(final ImageInputStream source, final long offset, final long length, final int bufferSize) {
    assert null != source;
    assert length >= 0;
    assert offset >= 0;

    this.source = source;
    this.offset = offset;
    this.length = length;

    buffer = new byte[bufferSize];
  }

  @Override
  public int read() throws IOException {
    if (streamPos >= length) {
      return -1;
    }

    if (streamPos >= bufferTop || streamPos < bufferBase) {
      if (!fillBuffer()) {
        return -1;
      }
    }

    final int read = buffer[(int) (streamPos - bufferBase)] & 0xFF;

    streamPos++;

    return read;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (streamPos >= length) {
      return -1;
    }

    synchronized (source) {
      if (source.getStreamPosition() != streamPos + offset) {
        source.seek(streamPos + offset);
      }

      final int toRead = (int) Math.min(len, length - streamPos);
      final int read = source.read(b, off, toRead);

      streamPos += read;

      return read;
    }
  }

  /**
   * Fills the buffer at the current stream position.
   *
   * @return {@code true} if successful, {@code false} if not.
   * @throws java.io.IOException passed through if the wrapped stream throws one.
   */
  private boolean fillBuffer() throws IOException {
    synchronized (source) {
      if (source.getStreamPosition() != streamPos + offset) {
        source.seek(streamPos + offset);
      }

      bufferBase = streamPos;

      final int toRead = (int) Math.min(buffer.length, length - streamPos);
      final int read = source.read(buffer, 0, toRead);

      bufferTop = bufferBase + read;

      return read > 0;
    }
  }

  @Override
  public long getSizeEstimate() {
    return length;
  }
  
  @Override
  public long length() {
    return length;
  }

  /** Skips remaining bits in the current byte. */
  public void skipBits() {
    if (bitOffset != 0) {
      bitOffset = 0;
      streamPos++;
    }
  }

  @Override
  public int readUnsignedByte() throws IOException {
    return read() & 0xFF;
  }

  @Override
  public int readUnsignedShort() throws IOException {
    return ((read() & 0xFF) << 8) + (read() & 0xFF);
  }
  
  @Override
  public void seek(long pos) throws IOException {
    // This test also covers pos < 0
    if (pos < flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }

    this.streamPos = pos;
    this.bitOffset = 0;
  }

  @Override
  public long readUnsignedInt() throws IOException {
    long unsignedInt = ((read() & 0xFF) << 24) + ((read() & 0xFF) << 16);
    unsignedInt += ((read() & 0xFF) << 8) + (read() & 0xFF);
    return unsignedInt;
  }

  @Override
  public boolean readBoolean() throws IOException {
    return read() != 0;
  }

  @Override
  public char readChar() throws IOException {
    return (char) readUnsignedShort();
  }

  @Override
  public void readFully(byte[] b) throws IOException {
    for (int i = 0; i < b.length; i++) {
      b[i] = (byte) (read() & 0xFF);
    }
  }
  @Override
  public long getStreamPosition() throws IOException {
    return streamPos;
  }

}