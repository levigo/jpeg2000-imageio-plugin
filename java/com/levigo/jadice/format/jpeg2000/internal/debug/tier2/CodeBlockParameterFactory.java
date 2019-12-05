package com.levigo.jadice.format.jpeg2000.internal.debug.tier2;

import static com.levigo.jadice.format.jpeg2000.internal.debug.CompositeParameter.composite;
import static com.levigo.jadice.format.jpeg2000.internal.debug.IntegerParameter.integer;

import com.levigo.jadice.format.jpeg2000.internal.debug.CompositeParameter;
import com.levigo.jadice.format.jpeg2000.internal.debug.Parameter;
import com.levigo.jadice.format.jpeg2000.internal.debug.ParameterFactory;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;

class CodeBlockParameterFactory implements ParameterFactory {

  private static final String formattedString = "(x, y) = (%d, %d)";

  static CompositeParameter codeBlockParam(CodeBlock codeBlock) {
    return composite(formattedString, integer(codeBlock.indices.x), integer(codeBlock.indices.y));
  }

  @Override
  public Parameter create() {
    return composite(formattedString, integer(), integer());
  }

}
