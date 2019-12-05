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


import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.msg.ValidationMessages;

public class Validate {

  public static void inRange(String identifier, long value, long min, long max) throws JPEG2000Exception {
    if (value < min || value > max) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_RANGE, value, identifier, min, max);
    }
  }

  public static void notNull(String identifier, Object value) throws JPEG2000Exception {
    if (value == null) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_NULL_VALUE, identifier);
    }
  }

  public static void exact(String identifier, Object value, Object expected) throws JPEG2000Exception {
    if (!value.equals(expected)) {
      throw new JPEG2000Exception(ValidationMessages.ILLEGAL_VALUE, identifier, value, expected);
    }
  }
}
