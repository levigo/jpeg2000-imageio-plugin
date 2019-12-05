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
package com.levigo.jadice.format.jpeg2000.internal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.msg.ValidationMessages;

public class ValidateInRangeTest {

  @Test
  public void valid() throws JPEG2000Exception {
    Validate.inRange("value=within", 1, 0, 2);
    Validate.inRange("value=min", 0, 0, 2);
    Validate.inRange("value=max", 2, 0, 2);
  }
  
  @Test
  public void invalidTooSmall() throws JPEG2000Exception {
    JPEG2000Exception actual = null;
    try {
      Validate.inRange("value=too small", 2, 3, 6);
    } catch (JPEG2000Exception e) {
      actual = e;
    }

    Assertions.assertNotNull(actual);

    Assertions.assertEquals(ValidationMessages.ILLEGAL_RANGE, actual.getMessageID(), "message id");
  }

  @Test
  public void invalidTooLarge() throws JPEG2000Exception {
    JPEG2000Exception actual = null;
    try {
      Validate.inRange("value=too large", 6, 1, 5);
    } catch (JPEG2000Exception e) {
      actual = e;
    }

    Assertions.assertNotNull(actual);

    Assertions.assertEquals(ValidationMessages.ILLEGAL_RANGE, actual.getMessageID(), "message id");
  }

}
