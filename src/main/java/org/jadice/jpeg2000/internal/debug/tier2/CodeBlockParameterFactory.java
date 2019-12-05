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
package org.jadice.jpeg2000.internal.debug.tier2;

import static org.jadice.jpeg2000.internal.debug.CompositeParameter.composite;
import static org.jadice.jpeg2000.internal.debug.IntegerParameter.integer;

import org.jadice.jpeg2000.internal.debug.CompositeParameter;
import org.jadice.jpeg2000.internal.debug.Parameter;
import org.jadice.jpeg2000.internal.debug.ParameterFactory;
import org.jadice.jpeg2000.internal.image.CodeBlock;

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
