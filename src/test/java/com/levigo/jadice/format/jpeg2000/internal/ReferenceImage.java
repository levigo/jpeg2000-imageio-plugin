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
package com.levigo.jadice.format.jpeg2000.internal;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import junit.framework.AssertionFailedError;

/**
 * A JUnit rule for tests which are using a reference image. This rule will load the expected
 * reference image.
 * <p>
 * 
 * See {@link #get(BufferedImage, String)} for more information.
 * 
 */
public class ReferenceImage extends TestWatcher {

  private Class<?> testClass;
  private String testMethodName;

  @Override
  protected void starting(final Description description) {
    testClass = description.getTestClass();
    testMethodName = description.getMethodName();
  }

  /**
   * Returns the reference image (expected image). This method will call
   * {@link #get(BufferedImage, String)} and uses the test method name as resource name.
   * <p>
   * 
   * If the reference file cannot be found, the actual image (if not null) will be written to the
   * error directory ".failed/testClassName/".
   * 
   * @param actual the actual image. On missing ref image, this image will be written to disk (if
   *          non-null)
   * @return the reference image
   * @throws FileNotFoundException
   * @throws ClassNotFoundException
   * @throws IOException
   * @see {@link #get(BufferedImage, String)}
   */
  public BufferedImage get(final BufferedImage actual) throws FileNotFoundException, ClassNotFoundException, IOException {
    return get(actual, testMethodName);
  }

  /**
   * Returns the reference image (expected image). The image will be loaded using the testClass'
   * class loader. The expected name of the image is "[given resourceName].ref". ".ref" will be
   * added automatically.
   * <p>
   * 
   * If the reference file cannot be found, the actual image (if not null) will be written to the
   * error directory ".failed/testClassName/".
   * 
   * @param actual the actual image. On missing ref image, this image will be written to disk (if
   *          non-null)
   * @param resourceName the resource name of the image to load
   * @return the reference image
   * @throws FileNotFoundException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public BufferedImage get(final BufferedImage actual, final String resourceName)
      throws FileNotFoundException, IOException, ClassNotFoundException {
    File failedDir = new File(new File(".failed"), testClass.getName());
    File exActDir = new File(new File(".expected-vs-actual"), testClass.getName());

    final InputStream refStream = testClass.getResourceAsStream(resourceName + ".ref");
    if (null == refStream) {
      String msg = "No reference data for " + resourceName;

      if (null != actual) {
        // FIXME: Saving the actual image here if the ref image is unavailable seems not optimal.
        // Do we really need this file or would it be sufficient to rely on the files written by the
        // ImageMatcher if they do not match?
        // The "actual" image parameter could be removed in all methods if we do not write the file
        // here.

        failedDir.mkdirs();
        exActDir.mkdirs();

        final String fileName = resourceName.substring(resourceName.lastIndexOf("/") + 1);
        final File savedRef = new File(failedDir, fileName + ".ref");
        ImageUtils.saveImage(actual, new FileOutputStream(savedRef));
        ImageUtils.saveDiff(actual, null, exActDir, fileName);

        msg += ".\n Actual result can be found in " + savedRef.getAbsolutePath();
      }

      throw new AssertionFailedError(msg);
    }

    final BufferedImage expected = ImageUtils.readImage(refStream);

    return expected;
  }
}
