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
package com.levigo.jadice.format.jpeg2000;

import static org.hamcrest.MatcherAssert.assertThat;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.internal.ImageMatcher;
import com.levigo.jadice.format.jpeg2000.internal.ReferenceImage;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestreams;

import junit.framework.AssertionFailedError;

public class SpecialJPEG2000RenderingTests {

  @Rule
  public ReferenceImage referenceImage = new ReferenceImage();

  /**
   * Verify that the correct color space and ComponentColorModel is used for grayscale images.
   * 
   * @throws Exception
   */
  @Test
  public void testGrayscaleImageUsesCorrectColorSpaceAndModel() throws Exception {
    final RenderedImage rendering = renderAndVerifyAppearance("/files/02_weisses_Dokument_01.jp2");

    Assert.assertEquals(ColorSpace.TYPE_GRAY, rendering.getColorModel().getColorSpace().getType());
    Assert.assertTrue(rendering.getColorModel() instanceof ComponentColorModel);
    Assert.assertEquals(false, (rendering.getColorModel()).hasAlpha());
    Assert.assertEquals(1, (rendering.getColorModel()).getNumComponents());
    Assert.assertEquals(8, (rendering.getColorModel()).getComponentSize(0));
  }

  /**
   * Verify that the correct color space and ComponentColorModel is used for grayscale images.
   * 
   * @throws Exception
   */
  @Test
  @Ignore // This test is half-baked (copy-paste from
          // testGrayscaleImageUsesCorrectColorSpaceAndModel())
  public void testFoo() throws Exception {
    final RenderedImage rendering = renderAndVerifyAppearance("/files/bestellformular_5143_Im0.jp2");

    Assert.assertEquals(ColorSpace.TYPE_GRAY, rendering.getColorModel().getColorSpace().getType());
    Assert.assertTrue(rendering.getColorModel() instanceof ComponentColorModel);
    Assert.assertEquals(false, (rendering.getColorModel()).hasAlpha());
    Assert.assertEquals(1, (rendering.getColorModel()).getNumComponents());
    Assert.assertEquals(8, (rendering.getColorModel()).getComponentSize(0));
  }

  protected RenderedImage renderAndVerifyAppearance(final String resourcePath)
      throws IOException, JPEG2000Exception, AssertionFailedError, ClassNotFoundException {
    final SeekableInputStream source = Codestreams.createCodestreamSource(Tests.openResource(resourcePath));
    final Codestream codestream = new Codestream(source);
    codestream.init();

    final JPEG2000Rable rable = new JPEG2000Rable(codestream);
    final RenderedImage rendering = rable.createDefaultRendering();
    final Raster raster = rendering.getData();
    final WritableRaster writableRaster = writable(raster);
    final BufferedImage actual = asBufferedImage(rendering, writableRaster);

    final BufferedImage expected = referenceImage.get(actual, resourcePath);

    assertThat(actual, ImageMatcher.matchesImage(resourcePath, expected));

    return rendering;
  }

  private BufferedImage asBufferedImage(final RenderedImage rendering, final WritableRaster raster) {
    final ColorModel colorModel = rendering.getColorModel();
    return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
  }

  private WritableRaster writable(final Raster raster) {
    if (raster instanceof WritableRaster) {
      return (WritableRaster) raster;
    } else {
      // Create a WritableRaster in the same coordinate system as the original raster.
      final Point location = new Point(raster.getMinX(), raster.getMinY());
      return Raster.createWritableRaster(raster.getSampleModel(), raster.getDataBuffer(), location);
    }
  }
}

