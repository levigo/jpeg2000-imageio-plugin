package com.levigo.jadice.format.jpeg2000;

import static com.levigo.jadice.document.ImageMatcher.matchesImage;
import static org.hamcrest.MatcherAssert.assertThat;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderableImage;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.levigo.jadice.document.internal.model.renderable.ConvertColorSpaceRable;
import com.levigo.jadice.document.internal.render.debug.rule.ReferenceImage;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestreams;

@RunWith(Parameterized.class)
public class JPEG2000RenderingTest {

  @Parameters(name = "{index}: {0}")
  public static String[] data() {
    // @formatter:off
    return new String[]{
        "/codestreams/profile0/p0_01.j2k", 
        "/codestreams/profile0/p0_02.j2k", 
        "/files/jp2/file1.jp2",
        "/files/ebcot-using-causal-ctx.jp2", 
        "/files/Robinson_Crusoe-page13-J2i0.jp2", 
        "/files/AntragPK363.Im0-53.jp2",
        "/files/2009_Dekra_Fehler-page26-wpt1.jp2",
        "/files/2009_Dekra_Fehler-page26-wpt2.jp2", 
        "/files/bestellformular_5143_Im0.jp2",
        "/files/DOCPV-812_p62_Im5.jp2"
    };
    // @formatter:on
  }

  @Parameter
  public String resourcePath;

  @Rule
  public ReferenceImage referenceImage = new ReferenceImage();

  @Test
  public void render() throws Exception {
    final SeekableInputStream source = Codestreams.createCodestreamSource(Tests.openResource(resourcePath));
    final Codestream codestream = new Codestream(source);
    codestream.init();

    final RenderableImage rable = new ConvertColorSpaceRable(new JPEG2000Rable(codestream),
        Arrays.asList(ColorSpace.getInstance(ColorSpace.CS_sRGB), ColorSpace.getInstance(ColorSpace.CS_GRAY)));

    final RenderedImage rendering = rable.createDefaultRendering();
    final Raster raster = rendering.getData();
    final WritableRaster writableRaster = writable(raster);
    final BufferedImage actual = asBufferedImage(rendering, writableRaster);

    final BufferedImage expected = referenceImage.get(actual, resourcePath);
    assertThat(actual, matchesImage(resourcePath, expected));
  }

  private BufferedImage asBufferedImage(RenderedImage rendering, WritableRaster raster) {
    final ColorModel colorModel = rendering.getColorModel();
    return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
  }

  private WritableRaster writable(Raster raster) {
    if (raster instanceof WritableRaster) {
      return (WritableRaster) raster;
    } else {
      // Create a WritableRaster in the same coordinate system as the original raster.
      final Point location = new Point(raster.getMinX(), raster.getMinY());
      return Raster.createWritableRaster(raster.getSampleModel(), raster.getDataBuffer(), location);
    }
  }
}

