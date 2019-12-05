package com.levigo.jadice.format.jpeg2000.internal.debug;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.List;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public class CompositeParameter implements Parameter {

  private List<Parameter> parameters;
  private final String formattedString;

  public static CompositeParameter composite(Parameter... parameters) {
    return new CompositeParameter(asList(parameters), "");
  }

  public static CompositeParameter composite(String formattedString, Parameter... parameters) {
    return new CompositeParameter(asList(parameters), formattedString);
  }

  public CompositeParameter(List<Parameter> parameters, String formattedString) {
    this.parameters = parameters;
    this.formattedString = formattedString;
  }

  @Override
  public Object value() {
    return parameters;
  }

  @Override
  public Object read(ImageInputStream source) throws IOException {
    for (Parameter parameter : parameters) {
      parameter.read(source);
    }
    return parameters;
  }

  @Override
  public void write(ImageOutputStream sink) throws IOException {
    for (Parameter parameter : parameters) {
      parameter.write(sink);
    }
  }

  @Override
  public boolean matches(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof CompositeParameter))
      return false;

    CompositeParameter that = (CompositeParameter) o;

    if (parameters.size() != that.parameters.size()) {
      return false;
    }

    for (int i = 0; i < parameters.size(); i++) {
      final Parameter thisParameter = parameters.get(i);
      final Parameter thatParameter = that.parameters.get(i);
      if (!thisParameter.matches(thatParameter)) {
        return false;
      }
    }

    return formattedString != null ? formattedString.equals(that.formattedString) : that.formattedString == null;
  }

  @Override
  public String toString() {
    final Object[] args = new Object[parameters.size()];
    for (int i = 0; i < args.length; i++) {
      args[i] = parameters.get(i).value();
    }
    return format(formattedString, args);
  }
}
