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

import java.io.IOException;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.io.SectorInputStream;
import org.jadice.jpeg2000.msg.GeneralMessages;

/**
 * Base for JP2 or JPX file format boxes. JP2 file format and its boxes are defined in <i>ITU-T.800, Annex I</i>,
 * while JPX file format and its boxes are defined in <i>ITU-T.801, Annex M</i>.
 * <p>
 * The binary structure of a box is defined in <i>ITU-T.800, Figure I.4</i>. The basic fields defined here reflect this
 * structure.
 */
public abstract class Box {

  /**
   * Box Length. This field specifies the length of the box, stored as a 4-byte big endian unsigned integer. This value
   * includes all of the fields of the box, including the length and type. If the value of this field is 1, then the
   * {@link #XLBox} field shall exist and the value of that field shall be the actual length of the box. If the value
   * of this field is 0, then the length of the box was not known when the {@link #LBox} field was written. In this
   * case, this box contains all bytes up to the end of the file. If a box of length 0 is contained within another box
   * (its superbox), then the length of that superbox shall also be 0. This means that this box is the last box in the
   * file. The values 2-7 are reserved for ISO use.
   */
  public long LBox;

  /**
   * Box Type. This field specifies the type of information found in the {@link #DBox} field. The value of this field
   * is encoded as a 4-byte big endian unsigned integer. However, boxes are generally referred to by an ISO/IEC 646
   * character string translation of the integer value. For all box types defined within <i>ITU-T.800</i>, box types
   * will be indicated as both character string (normative) and as 4-byte hexadecimal integers (informative).
   */
  public long TBox;

  /**
   * Box Extended Length. This field specifies the actual length of the box if the value of the {@link #LBox} field is
   * 1. This field is stored as an 8-byte big endian unsigned integer. The value includes all of the fields of the box,
   * including the {@link #LBox}, {@link #TBox} and {@link #XLBox} fields.
   */
  public long XLBox;

  /**
   * Box Contents. This field contains the actual information contained within this box. The format of the box contents
   * depends on the box type and will be defined individually for each type.
   */
  public SectorInputStream DBox;

  public boolean readSuccessfully;

  public Box() {
    TBox = getBoxType().type;
    readSuccessfully = false;
  }
  
  public abstract BoxType getBoxType();
  
  public void read() throws JPEG2000Exception {
    try {
      readSuccessfully = readDBox();
    } catch (IOException e) {
      throw new JPEG2000Exception(GeneralMessages.IO_ERROR, e);
    }
  }

  protected abstract boolean readDBox() throws JPEG2000Exception, IOException;
  
}
