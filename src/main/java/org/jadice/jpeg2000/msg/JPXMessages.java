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
package org.jadice.jpeg2000.msg;

import org.jadice.util.log.message.LogMessage;
import org.jadice.util.log.qualified.MessageID;

import com.levigo.jadice.document.ProductInformation;

public enum JPXMessages implements MessageID {
  @LogMessage("JPX must start with an JPEG2000 Signature box, but was {0}.")
  EXPECTED_JPEG2000_SIGNATURE_BOX,

  @LogMessage("JPX expects a File Type box after a JPEG2000 Signature box, but was {0}.")
  EXPECTED_FILE_TYPE_BOX,

  @LogMessage("Illegal LBox value {0}.")
  ILLEGAL_VALUE_FOR_LBOX;

  public static final String COMPONENT_ID = ProductInformation.getProductId() + "FORMAT.JPEG2000.JPX";

  @Override
  public String getComponentID() {
    return COMPONENT_ID;
  }
}
