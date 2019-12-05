package com.levigo.jadice.format.jpeg2000.internal.fileformat;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.Debug;
import com.levigo.jadice.format.jpeg2000.internal.io.SectorInputStream;
import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;
import com.levigo.jadice.format.jpeg2000.msg.JPXMessages;

@Refer(to = Spec.J2K_CORE, page = 127, section = "Annex I", called = "JP2 File Format Syntax")
public class FileFormatReader {

  private static final QualifiedLogger LOGGER = LoggerFactory.getQualifiedLogger(FileFormatReader.class);

  public Collection<Box> read(SeekableInputStream source) throws JPEG2000Exception {
    final Collection<Box> boxes = new LinkedList<>();
    synchronized (source) {
      readFixedBoxes(source, boxes);

      boolean foundEOF = false;
      while (!foundEOF) {
        try {
          final Box box = readBasicBoxStructure(source);
          boxes.add(box);

        } catch (EOFException e) {
          // This is an expected EOF
          if (Debug.LOG_FILEFORMAT_READ) {
            LOGGER.info("End of file reached. No more boxes available");
          }
          foundEOF = true;
        } catch (IOException e) {
          throw new JPEG2000Exception(GeneralMessages.IO_ERROR, e);
        }
      }

    }
    return boxes;
  }

  /**
   * Reads the JPEG2000 Signature box which should be immediately followed by a File Type box.
   *
   * @param source
   * @param boxes
   *
   * @throws JPEG2000Exception
   */
  private void readFixedBoxes(SeekableInputStream source, Collection<Box> boxes) throws JPEG2000Exception {
    try {
      source.seek(0);

      Box box = readBasicBoxStructure(source);
      if (box.TBox != BoxType.JPEG2000Signature.type) {
        throw new JPEG2000Exception(JPXMessages.EXPECTED_JPEG2000_SIGNATURE_BOX, box.TBox);
      }
      boxes.add(box);

      box = readBasicBoxStructure(source);
      if (box.TBox != BoxType.FileType.type) {
        throw new JPEG2000Exception(JPXMessages.EXPECTED_FILE_TYPE_BOX, box.TBox);
      }
      boxes.add(box);
    } catch (IOException e) {
      throw new JPEG2000Exception(GeneralMessages.IO_ERROR, e);
    }
  }

  private Box readBasicBoxStructure(SeekableInputStream source) throws IOException, JPEG2000Exception {
    final long boxStart = source.getStreamPosition();
    final long LBox = source.readUnsignedInt();

    final long TBox = source.readUnsignedInt();
    final BoxType boxType = BoxType.byType(TBox);

    if (Debug.LOG_FILEFORMAT_READ) {
      LOGGER.info("Found " + boxType);
    }

    final Box box = boxType.createBox();
    box.LBox = LBox;
    box.DBox = getDBox(source, boxStart, box);
    return box;
  }

  private SectorInputStream getDBox(SeekableInputStream source, long boxStart, Box box)
      throws IOException, JPEG2000Exception {
    long start = boxStart + 8;

    if (box.LBox == 1) { // The XLBox field is present. Use it as DBox length.
      return getDBoxViaXL(source, box, start);

    } else if (box.LBox == 0) { // The length is totally unknown.
      return getDBoxUntilEOF(source, start);

    } else if (box.LBox >= 8 && box.LBox < Integer.MAX_VALUE) { // The length is specified in LBox directly
      return getDBoxViaL(source, box, start);

    } else {
      throw new JPEG2000Exception(JPXMessages.ILLEGAL_VALUE_FOR_LBOX, box.LBox);
    }
  }

  private SectorInputStream getDBoxViaL(SeekableInputStream source, Box box, long start) throws IOException {
    final long length = box.LBox - 8;
    source.seek(start + length);
    return new SectorInputStream(source, start, length);
  }

  private SectorInputStream getDBoxViaXL(SeekableInputStream source, Box box, long start) throws IOException {
    start += 8;
    box.XLBox = source.readLong();
    final long length = box.XLBox - 16;
    source.seek(start + length);
    return new SectorInputStream(source, start, length);
  }

  private SectorInputStream getDBoxUntilEOF(SeekableInputStream source, long start) throws IOException {
    long length = source.length();
    if (length < 0) {
      // The source is not able to determine its length. Going to read until EOF is reached.
      length = 0;
      while (source.read() != -1) {
        length++;
      }
    } else {
      length = length - start;
      source.seek(start + length);
    }
    return new SectorInputStream(source, start, length);
  }

}