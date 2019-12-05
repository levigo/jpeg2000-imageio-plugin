package com.levigo.jadice.format.jpeg2000.internal.param;

import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

import java.io.IOException;
import java.util.Properties;

import com.levigo.jadice.format.jpeg2000.msg.GeneralMessages;

public class PropertiesParameterInfo implements ParameterInfo {

  static final Properties properties = new Properties();

  static {
    try {
      properties.load(PropertiesParameterInfo.class.getResourceAsStream("./param_descriptions.properties"));
    } catch (final IOException e) {
      getQualifiedLogger(PropertiesParameterInfo.class).warn(GeneralMessages.IO_ERROR, e);
    }
  }

  private final String name;
  private final Object value;
  private final String key;

  public PropertiesParameterInfo(String name, String key) {
    this(name, "", key);
  }

  public PropertiesParameterInfo(String name, Object value, String key) {
    this.name = name;
    this.value = value;
    this.key = key;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getValue() {
    return "" + value;
  }

  @Override
  public String getDescription() {
    return properties.getProperty(key);
  }
}
