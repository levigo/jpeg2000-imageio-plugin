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
package org.jadice.jpeg2000;

import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;

import org.jadice.jpeg2000.internal.codestream.Codestream;

import com.levigo.jadice.document.internal.model.renderable.BaseRable;
import com.levigo.jadice.document.internal.model.renderable.Rable;

public class JPEG2000Rable extends BaseRable {

  private final Codestream codestream;
  private final JPEG2000Image image;

  public JPEG2000Rable(Codestream codestream) {
    this(codestream, new JPEG2000Image(codestream));
  }

  private JPEG2000Rable(Codestream codestream, JPEG2000Image image) {
    this.codestream = codestream;
    this.image = image;
  }

  @Override
  public RenderedImage createRendering(RenderContext renderContext) {
    return image;
  }

  @Override
  public Rable createSnapshot() {
    return new JPEG2000Rable(codestream, image);
  }

  @Override
  public float getWidth() {
    return image.getWidth();
  }

  @Override
  public float getHeight() {
    return image.getHeight();
  }

  public Codestream getCodestream() {
    return codestream;
  }

  public JPEG2000Image getImage() {
    return image;
  }
}
