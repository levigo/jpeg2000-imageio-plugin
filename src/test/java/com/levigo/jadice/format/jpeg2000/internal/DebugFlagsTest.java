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

import java.lang.reflect.Field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * This test case checks if all debug flags are set to false. This is done to ensure, that all debug
 * options are disabled for release builds to avoid debug logging or intermediate rasters show up.
 */
public class DebugFlagsTest {

  @Test
  public void ensureDisabled() throws IllegalArgumentException, IllegalAccessException {
    final Field[] declaredFields = Debug.class.getDeclaredFields();
    for (Field field : declaredFields) {
      final Class<?> type = field.getType();
      Assertions.assertNotNull(type);
      if (type.equals(boolean.class)) {
        Assertions.assertFalse(field.getBoolean(field), field.getName() + " was true");
      }
    }
  }

}
