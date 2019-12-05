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
