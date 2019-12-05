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
