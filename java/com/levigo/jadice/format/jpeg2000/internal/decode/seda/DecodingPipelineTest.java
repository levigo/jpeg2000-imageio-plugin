package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

import java.io.InputStream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.levigo.jadice.document.io.IOUtils;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.JPEG2000RenderingTest;
import com.levigo.jadice.format.jpeg2000.internal.codestream.BlockContribution;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Receiver;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.ForwardReading;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.stage.PacketBodyReading;

public class DecodingPipelineTest {

  @Test
  @Disabled
  public void fooTest() throws Exception {
    final String resourcePath = "/codestreams/profile0/p0_01.j2k";

    final Class<JPEG2000RenderingTest> cls = JPEG2000RenderingTest.class;
    final InputStream inputStream = cls.getResourceAsStream(resourcePath);
    final SeekableInputStream source = IOUtils.wrap(inputStream);

    final Codestream codestream = new Codestream(source);
    codestream.init();

    new ForwardReading(new PacketBodyReading(new Receiver<BlockContribution>() {
      @Override
      public void receive(BlockContribution pushable, DecoderParameters parameters) throws JPEG2000Exception {
        System.out.println(pushable);
      }
    })).receive(codestream, new DecoderParameters());
  }

  @Test
  @Disabled
  public void testDecodingPipeline() throws Exception {
    final String resourcePath = "/codestreams/profile0/p0_01.j2k";

    final Class<JPEG2000RenderingTest> cls = JPEG2000RenderingTest.class;
    final InputStream inputStream = cls.getResourceAsStream(resourcePath);
    final SeekableInputStream source = IOUtils.wrap(inputStream);

    final Codestream codestream = new Codestream(source);
    codestream.init();

    final DecoderParameters parameters = new DecoderParameters();
    parameters.region = codestream.region().absolute();

    final Pipeline<JPEG2000Exception> pipeline = Pipeline.startWith(new Producer<Codestream, JPEG2000Exception>() {
      @Override
      public void run(Consumer<Codestream, JPEG2000Exception> next) throws JPEG2000Exception {
        next.consume(codestream);
      }
    })//
    .append(new ForwardReadingStage())//
    .append(new BlockContributionStage())//
    .append(new InverseQuantizationStage())//
    .append(new InverseDWTStage())//
    .append(new LevelShiftStage())//
    .append(new InverseMCTStage())//
    .finishWith(new Consumer<Object, JPEG2000Exception>() {
      @Override
      public void consume(Object it) {
        System.out.println("Got: " + it);
      }
    });

    StageConfigurer.configure(pipeline, parameters);

    final ExecutionStrategy xs = new StageByStageBufferedExecutionStrategy();
    xs.execute(pipeline);
  }
}
