package com.levigo.jadice.format.jpeg2000.internal.codestream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.junit.jupiter.api.Test;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;

public class CodestreamTest {

  @Test
  public void createForReading() throws IOException, JPEG2000Exception {
    final byte[] minimalMainHeaderBytes = new byte[]{
        // SOC:
        (byte) 0xFF, 0x4F,
        // SIZ:
        (byte) 0xFF, 0x51, 0x00, 0x29, 0x00, 0x01, 0x00, 0x00, 0x00, (byte) 0x80, 0x00, 0x00, 0x00, (byte) 0x80, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x00, 0x00, 0x00, (byte) 0x80, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x07, 0x01, 0x01, //
        // SOT:
        (byte) 0xFF, (byte) 0x90
    };

    ImageInputStream source = new MemoryCacheImageInputStream(new ByteArrayInputStream(minimalMainHeaderBytes));

    Codestream codestream = new Codestream(source);
    codestream.init();

    assertNotNull(codestream.source, "Codestream source");
    assertNotNull(codestream.region().absolute(), "Canvas");
    assertNotNull(codestream.tilePartition, "Tile partition");
    assertNotNull(codestream.numTiles, "Tile span");
    assertNotNull(codestream.comps, "Components");
  }
}
