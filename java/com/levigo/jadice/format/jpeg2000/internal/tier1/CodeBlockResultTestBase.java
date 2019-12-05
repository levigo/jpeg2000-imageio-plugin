package com.levigo.jadice.format.jpeg2000.internal.tier1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jadice.util.log.LoggerFactory.getQualifiedLogger;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jadice.util.log.qualified.QualifiedLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.Tests;
import com.levigo.jadice.format.jpeg2000.internal.buffer.Buffers;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.BlockContributionStage;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.ForwardReading;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.PacketBodyReading;
import com.levigo.jadice.format.jpeg2000.internal.image.BandPrecinct;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.Component;
import com.levigo.jadice.format.jpeg2000.internal.image.Pair;
import com.levigo.jadice.format.jpeg2000.internal.image.Precinct;
import com.levigo.jadice.format.jpeg2000.internal.image.Region;
import com.levigo.jadice.format.jpeg2000.internal.image.Resolution;
import com.levigo.jadice.format.jpeg2000.internal.image.Subband;
import com.levigo.jadice.format.jpeg2000.internal.image.Tile;
import com.levigo.jadice.format.jpeg2000.internal.image.TileComponent;

public abstract class CodeBlockResultTestBase {
  
  private static final QualifiedLogger LOGGER = getQualifiedLogger(CodeBlockResultTestBase.class);

  protected SampleAssert sampleAssert;

  protected abstract void fillExpectedSamples(SampleAssert sampleAssert);

  protected abstract String getResourcePath();

  @BeforeEach
  public void setup() {
    sampleAssert = new SampleAssert();
    fillExpectedSamples(sampleAssert);
  }

  @Test
  public void runTest() throws IOException, JPEG2000Exception {
    final SeekableInputStream stream = Tests.openResource(getResourcePath());

    final Codestream codestream = new Codestream(stream);
    codestream.init();

    final DecoderParameters parameters = new DecoderParameters();
    parameters.region = codestream.region().absolute();
    parameters.validate = true;

    final BlockContributionStage blockContributionStage = new BlockContributionStage(sampleAssert);
    final PacketBodyReading packetBodyReading = new PacketBodyReading(blockContributionStage);
    final ForwardReading forwardReading = new ForwardReading(packetBodyReading);
    forwardReading.receive(codestream, parameters);

    for (Map.Entry<String, TestPair> testPairEntry : sampleAssert.pairMap.entrySet()) {
      final String key = testPairEntry.getKey();
      Assertions.assertNotNull(key);

      final TestPair testPair = testPairEntry.getValue();
      Assertions.assertNotNull(testPair, key + " value");
      Assertions.assertTrue(testPair.checked, key + " checked");
    }
  }

  public static String getBlockIdentifier(CodeBlock block) {
    final BandPrecinct bandPrecinct = block.bandPrecinct;
    final Precinct precinct = bandPrecinct.precinct;
    final Subband subband = bandPrecinct.subband;
    final Resolution resolution = subband.resolution;
    final TileComponent tileComp = resolution.tileComp;
    final Tile tile = tileComp.tile;
    final Component comp = tileComp.comp;
    final Pair indices = block.indices;

    return "t" + tile.idx
        + "c" + comp.idx
        + "r" + resolution.resLevel
        + "b" + subband.type
        + "p" + precinct.idx
        + "cb" + indices.x + "." + indices.y;
  }

  protected static class TestPair {
    public CodeBlock codeBlock;
    public int[] expectedSamples;
    public boolean checked;

    @Override
    public String toString() {
      final Region region = codeBlock.region().absolute();
      return "\n"+Buffers.samples(codeBlock.state.sampleBuffer, region.size.y, region.size.x, 12);
    }
  }

  protected static class SampleAssert implements Receiver<CodeBlock> {

    private final Map<String, TestPair> pairMap;

    public SampleAssert() {
      pairMap = new HashMap<>();
    }

    @Override
    public void receive(CodeBlock block, DecoderParameters parameters) throws JPEG2000Exception {
      final String blockIdentifier = getBlockIdentifier(block);
      if (block.state.finished) {
        final StringBuilder stringBuilder = new StringBuilder("Code-block " + blockIdentifier + " testing... ");
        
        final TestPair testPair = pairMap.get(blockIdentifier);
        if (testPair != null) {
          testPair.codeBlock = block;
          
          stringBuilder.append("bitplane " + block.state.bitplane + " ... ");

          assertThat(testPair, new TestPairMatcher(stringBuilder));
          Assertions.assertArrayEquals(testPair.expectedSamples, block.state.sampleBuffer, blockIdentifier);

          testPair.checked = true;
        } else {
          LOGGER.info("Expected samples missing.");
        }
        
        LOGGER.info(stringBuilder.toString());
      }
    }

    public void addExpectedSamples(String blockIdentifier, int[] expectedSamples) {
      final TestPair testPair = new TestPair();
      testPair.checked = false;
      testPair.expectedSamples = expectedSamples;
      pairMap.put(blockIdentifier, testPair);
    }

    private class TestPairMatcher extends BaseMatcher<TestPair> {

      private final StringBuilder stringBuilder;

      private TestPair testPair;

      public TestPairMatcher(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
      }

      @Override
      public boolean matches(Object item) {
        testPair = (TestPair) item;
        final boolean equals = Arrays.equals(testPair.expectedSamples, testPair.codeBlock.state.sampleBuffer);
        if (equals) {
          stringBuilder.append("OK.");
        } else {
          stringBuilder.append("FAILED.\n");
          final int[] diff = Buffers.diff(testPair.expectedSamples, testPair.codeBlock.state.sampleBuffer);
          final Pair blockSize = testPair.codeBlock.region().absolute().size;
          Buffers.dumpSamples(diff, blockSize.y, blockSize.x, 12);
        }
        return equals;
      }

      @Override
      public void describeTo(Description description) {
        final Region region = testPair.codeBlock.region().absolute();
        description.appendText("\n").appendText(
            Buffers.samples(
                testPair.expectedSamples,
                region.size.y,
                region.size.x,
                12));
      }

      @Override
      public void describeMismatch(Object item, Description description) {
        final Region region = testPair.codeBlock.region().absolute();
        description.appendText("\n").appendText(
            Buffers.samples(
                testPair.codeBlock.state.sampleBuffer,
                region.size.y,
                region.size.x,
                12));
        description.appendText("\nDifferences:\n").appendText(
            Buffers.samples(
                Buffers.diff(testPair.expectedSamples, testPair.codeBlock.state.sampleBuffer),
                region.size.y,
                region.size.x,
                12));
      }
    }
  }
}
