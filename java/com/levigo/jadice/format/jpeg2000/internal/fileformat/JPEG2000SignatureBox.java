package com.levigo.jadice.format.jpeg2000.internal.fileformat;

import static com.levigo.jadice.format.jpeg2000.internal.fileformat.BoxType.JPEG2000Signature;

import java.io.IOException;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.msg.JPXMessages;
import com.levigo.jadice.format.jpeg2000.msg.ValidationMessages;

/**
 * Specified in <i>ITU-T.800, I.5.1</i>
 * <p>
 * The JPEG 2000 Signature box identifies that the format of this file was defined by <i>ITU-T.800</i>, as well as
 * provides a small amount of information which can help determine the validity of the rest of the file. The JPEG 2000
 * Signature box shall be the first box in the file, and all files shall contain one and only one JPEG 2000 Signature
 * box.
 * <p>
 * The type of the JPEG 2000 Signature box shall be {@code 'jP\040\040'} ({@code 0x6A50 2020}). The length of this box
 * shall be 12 bytes. The contents of this box shall be the 4-byte character string {@code '<CR><LF><0x87><LF>'}
 * ({@code 0x0D0A 870A} defined by constant {@link #DBOX_VALUE}). For file verification purposes, this box can be
 * considered a fixed-length 12-byte string which shall have the value: {@code 0x0000 000C 6A50 2020 0D0A 870A}.
 * <p>
 * The combination of the particular type and contents for this box enable an application to detect a common set of
 * file transmission errors. The CR-LF sequence in the contents catches bad file transfers that alter newline
 * sequences. The final linefeed checks for the inverse of the CR-LF translation problem. The third character of the
 * box contents has its high-bit set to catch bad file transfers that clear bit 7.
 */
public class JPEG2000SignatureBox extends Box {

  public static final long DBOX_VALUE = 0x0D0A870A;

  public long value;
  
  @Override
  public BoxType getBoxType() {
    return JPEG2000Signature;
  }
  
  @Override
  protected boolean readDBox() throws JPEG2000Exception, IOException {
    value = DBox.readUnsignedInt();
    
    if(value != DBOX_VALUE) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_VALUE, "DBox", Long.toHexString(value),
          Long.toHexString(DBOX_VALUE));
    }
    
    return true;
  }
}
