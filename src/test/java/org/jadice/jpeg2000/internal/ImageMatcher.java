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
package org.jadice.jpeg2000.internal;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jadice.util.base.Exceptions;

import com.levigo.jadice.document.internal.BufferedImages;
import com.levigo.jadice.document.internal.render.debug.PartialResultsDebugger;
import org.jadice.util.log.Logger;
import org.jadice.util.log.LoggerFactory;

/**
 * Matcher working on {@link BufferedImage}s rendered from some kind of source.
 * {@link ImageMatcher}s are created via {@link #matchesReferenceImage(String)} with a name which is
 * used to identify an image resource specifying the expected rendering result.
 * <p>
 * Reference images (containing the expected result) are loaded from the resource path {@code
 * /image-comparison-asserts/<name>.png}. Therefore they can be contained in test-jars or be loaded
 * from an unpacked resource directory.
 * <p>
 * Alternatively, reference image names starting with a {@code /} are interpreted as absolute
 * resource paths. They are not prefixed with {@code /image-comparison-asserts/<name>.png} for the
 * lookup.
 * <p>
 * In order to facilitate the use of different reference images for different environments,
 * reference images are searched using suffixes derived from the following environment properties:
 * <ul>
 * <li>The operating system name (system property <code>os.name</code>)</li>
 * <li>The JDK name (first part of the system property <code>java.runtime.name</code>)</li>
 * <li>The JDK major and minor version number (first two parts of the system property
 * <code>java.runtime.version</code>)</li>
 * </ul>
 * The search suffixes are prioritized so that more specific variants take precedence over more
 * general ones. A sample search list might look like this:
 * <ol>
 * <li>SomeName_Linux_OpenJDK_1.7.png</li>
 * <li>SomeName_Linux_OpenJDK.png</li>
 * <li>SomeName_Linux_1.7.png</li>
 * <li>SomeName_OpenJDK_1.7.png</li>
 * <li>SomeName_Linux.png</li>
 * <li>SomeName_OpenJDK.png</li>
 * <li>SomeName_1.7.png</li>
 * <li>SomeName.png</li>
 * </ol>
 * <p>
 * In case of test failures (the rendering did not match the expected result), images containing the
 * actual result are placed under {@code failed-image-comparison-asserts/<name>.png} relative to the
 * current execution directory.
 * <p>
 * If either the system property <code>image-comparison-assert.show-diffs</code> or the environment
 * variable <code>ICA_SHOW_DIFFS</code> is set, an optional visualization GUI is shown upon the
 * first test failure. This GUI visualizes the failed tests with expected and actual results.
 */
public final class ImageMatcher extends TypeSafeMatcher<BufferedImage> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ImageMatcher.class);

  public static ImageMatcher matchesReferenceImage(String name) {
    return new ImageMatcher(name);
  }

  public static ImageMatcher matchesImage(String name, BufferedImage expected) {
    return new ImageMatcher(name, expected);
  }

  public static final String VISUALIZATION_SYSTEM_PROPERTY = "image-comparison-assert.show-diffs";
  public static final String VISUALIZATION_ENVIRONMENT_VARIABLE = "ICA_SHOW_DIFFS";

  private final String name;

  private final File failedDir = new File("failed-image-comparison-asserts");

  /**
   * The leniency of the comparison in per-million-pixels (i.e. parts per million - PPM).
   */
  // by default we accept no difference whatsoever
  protected int leniencyPPM;

  protected Rectangle roi;

  protected int componentLeniency;

  private final BufferedImage expected;

  private final String effectiveVariant;

  // In case of mismatch, the ImageMatcher can dump the following image files: actual, expected,
  // difference and highlighted difference.
  // In the future, we might consider to put these options in some kind of Settings-Object.
  private boolean onMismatchWriteActualImage = true;
  private boolean onMismatchWriteExpectedImage = false;
  private boolean onMismatchWriteDiffImage = false;
  private boolean onMismatchWriteHighlightDiffImage = false;

  protected ImageMatcher(String name) {
    this.name = name;

    String baseName = getBaseName(name);
    String[] suffixVariants = getVariantSuffixes();

    String effectiveVariant = null;
    BufferedImage expected = null;
    for (String suffix : suffixVariants) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Trying " + baseName + suffix + ".png");
      }
      final InputStream is = getClass().getResourceAsStream(baseName + suffix + ".png");
      if (null != is) {
        effectiveVariant = baseName + suffix;
        try {
          expected = ImageIO.read(is);
        } catch (IOException e) {
          final String stackTrace = Exceptions.getStackTrace(e);
          throw new AssertionError("Can't load expected image: " + stackTrace);
        }
        break;
      }
    }

    if (null == effectiveVariant)
      effectiveVariant = "[No reference image found: " + name + "]";

    this.effectiveVariant = effectiveVariant;
    this.expected = preprocessImageForComparison(expected);
  }

  protected ImageMatcher(String name, BufferedImage expected) {
    this.name = name;
    this.expected = preprocessImageForComparison(expected);
    effectiveVariant = "";
  }

  protected String getBaseName(String name) {
    return !name.startsWith("/") ? "/image-comparison-asserts/" + sanitizeName(name) : name;
  }

  protected String[] getVariantSuffixes() {
    String searchListComponents[] = {
        sanitizeName(System.getProperty("os.name")),
        // keep just the first part of the runtime name
        sanitizeName(System.getProperty("java.runtime.name").replaceAll("\\s+.*", "")),
        // keep just the first two components of the version number
        System.getProperty("java.runtime.version").replaceAll("\\.\\d+_.*", "")
    };

    // sort patterns by number of set bits (descending) to let more specific variants take
    // precedence over more general ones.
    Integer suffixBits[] = new Integer[1 << searchListComponents.length];
    for (int i = 0; i < suffixBits.length; i++) {
      suffixBits[i] = i;
    }
    Arrays.sort(suffixBits, new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return Integer.bitCount(o1) - Integer.bitCount(o2);
      }
    });

    String suffixVariants[] = new String[1 << searchListComponents.length];
    for (int i = suffixVariants.length - 1; i >= 0; i--) {
      StringBuilder suffix = new StringBuilder();
      for (int j = 0; j < searchListComponents.length; j++) {
        if ((suffixBits[i] >> (searchListComponents.length - j - 1) & 0x1) != 0) {
          suffix.append("_").append(searchListComponents[j]);
        }
      }

      suffixVariants[suffixVariants.length - i - 1] = suffix.toString();
    }

    return suffixVariants;
  }

  /**
   * Set the region-of-interest specified as the given rectangle.
   *
   * @return an {@link ImageMatcher}
   */
  public ImageMatcher at(int x, int y, int w, int h) {
    roi = new Rectangle(x, y, w, h);
    return this;
  }

  /**
   * Set the region-of-interest specified as the given rectangle.
   *
   * @return an {@link ImageMatcher}
   */
  public ImageMatcher at(Rectangle roi) {
    this.roi = new Rectangle(roi);
    return this;
  }

  /**
   * Set the leniency of the comparison in per-million-pixels (i.e. parts per million - PPM).
   *
   * @return an {@link ImageMatcher}
   */
  public ImageMatcher withLeniencyPPM(int l) {
    this.leniencyPPM = l;
    return this;
  }

  /**
   * Set the leniency of the comparison in bits. Component value differences of up to the given value
   * are ignored.
   * 
   * @return an {@link ImageMatcher}
   */
  public ImageMatcher withComponentLeniencyLSB(int c) {
    this.componentLeniency = c;
    return this;
  }

  @Override
  protected boolean matchesSafely(BufferedImage actual) {
    boolean result;

    actual = preprocessImageForComparison(actual);

    if (null == expected) {
      result = false;
    } else if (!sizeOf(actual).equals(sizeOf(expected))) {
      result = false;
    } else {
      result = BufferedImages.countDifferingPixels(actual, expected) <= getAcceptableDifferingPixelCount();
    }

    if (!result) {
      if (onMismatchWriteActualImage)
        saveResultImage(actual, name + "-actual");
      if (onMismatchWriteExpectedImage)
        saveResultImage(expected, name + "-expected");
      if (onMismatchWriteDiffImage)
        saveResultImage(BufferedImages.computeDifference(actual, expected, false), name + "-diff");
      if (onMismatchWriteHighlightDiffImage)
        saveResultImage(BufferedImages.computeDifference(actual, expected, true), name + "-highlightDiff");

      PartialResultsDebugger.getInstance().addDebugResult(actual, expected, name);
    }

    return result;
  }

  private long getAcceptableDifferingPixelCount() {
    return getRoiArea() * leniencyPPM / 1000000L;
  }

  private long getRoiArea() {
    final int w = null != roi ? roi.width : expected.getWidth();
    final int h = null != roi ? roi.height : expected.getHeight();
    long area = (long) w * (long) h;
    return area;
  }

  private static Dimension sizeOf(BufferedImage image) {
    return new Dimension(image.getWidth(), image.getHeight());
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("Image rendering matches " + effectiveVariant.replaceAll(".*/", ".../"));
  }

  @Override
  protected void describeMismatchSafely(BufferedImage actual, Description mismatchDescription) {
    actual = preprocessImageForComparison(actual);

    if (null == expected) {
      mismatchDescription.appendText("Expected image could not be found");
    } else if (null == actual) {
      mismatchDescription.appendText("Actual image was null");
    } else if (!sizeOf(actual).equals(sizeOf(expected))) {
      mismatchDescription.appendText("Sizes do not match. Expected: " + sizeOf(expected) + ", was: " + sizeOf(actual));
    } else {
      long acceptableDifferingPixelCount = getAcceptableDifferingPixelCount();
      long differingPixels = BufferedImages.countDifferingPixels(actual, expected, componentLeniency);
      if (differingPixels > acceptableDifferingPixelCount) {
        mismatchDescription.appendText("Contents did not match. Difference of " + differingPixels + " pixels ≙ "
            + ((float) differingPixels / (float) getRoiArea() * 1000000f) + " ppm exceeded leniency threshold "
            + leniencyPPM + " ppm, " + componentLeniency + " LSB per component");
      }
    }
  }

  private void saveResultImage(final BufferedImage image, String name) {
    if (null == failedDir)
      return;
    try {
      failedDir.mkdirs();

      final File actualFile = new File(failedDir, (!name.startsWith("/") ? sanitizeName(name) : name) + ".png");
      actualFile.delete(); // we may want to replace it

      if (!actualFile.getParentFile().exists())
        actualFile.getParentFile().mkdirs();

      Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("PNG");
      if (!writers.hasNext())
        throw new RuntimeException("Can't write actual image: PNG writer not found");

      ImageWriter writer = writers.next();

      final File absoluteFile = actualFile.getAbsoluteFile();
      MemoryCacheImageOutputStream os = new MemoryCacheImageOutputStream(new FileOutputStream(absoluteFile));
      writer.setOutput(os);
      writer.write(image);
      os.close();
    } catch (final IOException e) {
      throw new RuntimeException("Can't save actual result image", e);
    }
  }

  /**
   * Replaces all characters which might cause in a file name with underscores. This method really
   * just allows digits, ASCII characters as well as the following characters: "()-_'".
   *
   * @param name the string to sanitize
   *
   * @return the resulting string.
   */
  private static String sanitizeName(String name) {
    return name.replaceAll("[^0-9a-zA-Z_\\-\\(\\)']", "_");
  }

  private static BufferedImage preprocessImageForComparison(BufferedImage img) {
    if (img.getColorModel() instanceof ComponentColorModel || img.getColorModel() instanceof IndexColorModel) {
      final BufferedImage tmp = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
      tmp.createGraphics().drawImage(img, 0, 0, null);
      return tmp;
    }
    return img;
  }
}