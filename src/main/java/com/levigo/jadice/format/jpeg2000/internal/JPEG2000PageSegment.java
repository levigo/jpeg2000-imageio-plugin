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

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.util.Collection;

import org.jadice.util.log.LoggerFactory;
import org.jadice.util.log.qualified.QualifiedLogger;

import com.levigo.jadice.document.AbstractPageSegment;
import com.levigo.jadice.document.JadiceException;
import com.levigo.jadice.document.PageSegmentSource;
import com.levigo.jadice.document.Resolution;
import com.levigo.jadice.document.Unit;
import com.levigo.jadice.document.internal.model.Container;
import com.levigo.jadice.document.internal.model.Image;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.document.metadata.Metadata;
import com.levigo.jadice.document.util.GraphicsEnvironment;
import com.levigo.jadice.format.jpeg2000.JPEG2000Rable;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestreams;
import com.levigo.jadice.format.jpeg2000.internal.fileformat.Box;
import com.levigo.jadice.format.jpeg2000.internal.fileformat.BoxType;
import com.levigo.jadice.format.jpeg2000.internal.image.GridRegion;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;

public final class JPEG2000PageSegment extends AbstractPageSegment {

  private static final QualifiedLogger LOGGER = LoggerFactory.getQualifiedLogger(JPEG2000PageSegment.class);

  private Resolution resolution;

  private Dimension2D imageSize;

  private Codestream codestream;

  public JPEG2000PageSegment(PageSegmentSource source) {
    super();
    setSource(source);
    format = source.getFormat();

    try {
      final SeekableInputStream codestreamSource = Codestreams.createCodestreamSource(source.getStream());
      codestream = new Codestream(codestreamSource, 0);
      codestream.init();

      if (codestream.constructionFinalized) {
        initDimensions();
      }

    } catch (Exception e) {
      LOGGER.fatal(CodestreamMessages.UNABLE_TO_CREATE_CODESTREAM_REPRESENTATION, e);
    }
  }

  private SeekableInputStream findCodestreamSource(Collection<Box> boxes) {
    for (Box box : boxes) {
      if (box.TBox == BoxType.ContiguousCodestream.type) {
        return box.DBox;
      }
    }
    return null;
  }

  private void initDimensions() {
    resolution = new Resolution(GraphicsEnvironment.getScreenResolution());

    final GridRegion gridElement = codestream.region();
    final Region canvas = gridElement.absolute();
    final Dimension size = new Dimension(canvas.size.x, canvas.size.y);
    imageSize = resolution.getSize(size, Unit.JadiceDocumentUnit);
  }

  @Override
  protected CacheableRendering doCreateCacheableRendering() throws JadiceException {
    synchronized (codestream) {
      final AffineTransform tx = AffineTransform.getScaleInstance(imageSize.getWidth(), imageSize.getHeight());
      tx.scale(1, -1);
      tx.translate(0, -1);

      final Container root = new Container();
      root.setTransformation(tx);

      root.add(new Image(new JPEG2000Rable(codestream)));
      return new CacheableRendering(root, getEstimatedSize(), "JPEG2000 image");
    }
  }

  private int getEstimatedSize() {
    return (int) (codestream.firstTilePartAddress * 2) + 1000;
  }

  @Override
  public Resolution getResolution() {
    return resolution;
  }

  @Override
  public Dimension2D getSize() {
    return imageSize;
  }

  @Override
  public Metadata getMetadata() {
    // TODO
    // Set up the following information:
    // * information signalled in COM marker segments
    // * image width and height
    // * channel/color configuration

    return super.getMetadata();
  }
}
