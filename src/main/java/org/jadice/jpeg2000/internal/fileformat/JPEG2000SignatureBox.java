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
package org.jadice.jpeg2000.internal.fileformat;

import static org.jadice.jpeg2000.internal.fileformat.BoxType.JPEG2000Signature;

import java.io.IOException;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.msg.JPXMessages;
import org.jadice.jpeg2000.msg.ValidationMessages;

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
