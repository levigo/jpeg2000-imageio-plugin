package com.levigo.jadice.format.jpeg2000.internal.param;

public class DirectParameterInfo implements ParameterInfo {
  private String name;
  private String value;
  private String description;

  public DirectParameterInfo(String name, Object value) {
    this(name, value, null);
  }

  public DirectParameterInfo(String name, Object value, String description) {
    this.name = name;
    this.value = "" + value;
    this.description = description;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String getDescription() {
    return description;
  }
}
