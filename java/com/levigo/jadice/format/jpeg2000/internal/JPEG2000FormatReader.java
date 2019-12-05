package com.levigo.jadice.format.jpeg2000.internal;

import java.io.IOException;
import java.util.Map;

import com.levigo.jadice.document.DocumentLayer;
import com.levigo.jadice.document.FormatReader;
import com.levigo.jadice.document.FormatReaderProcessor;
import com.levigo.jadice.document.JadiceException;
import com.levigo.jadice.document.PageSegmentSource;
import com.levigo.jadice.document.internal.read.DefaultPageSegmentSource;
import com.levigo.jadice.document.read.ReaderParameters;
import com.levigo.jadice.format.jpeg2000.JPEG2000Format;

public final class JPEG2000FormatReader extends FormatReader {

  @Override
  protected void doRead(ReaderParameters parameters, FormatReaderProcessor processor)
      throws IOException, JadiceException {
    processor.readStarted(0);

    final Map<String, Object> properties = parameters.getProperties();
    if (properties != null) {
      processor.processPageProperties(0, properties);
    }

    final PageSegmentSource source = new DefaultPageSegmentSource(parameters, new JPEG2000Format(), 0);
    final JPEG2000PageSegment pageSegment = new JPEG2000PageSegment(source);
    processor.processPageSegment(pageSegment, 0, DocumentLayer.DEFAULT);
  }

}
