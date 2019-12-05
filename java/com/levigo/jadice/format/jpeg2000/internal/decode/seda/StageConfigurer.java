package com.levigo.jadice.format.jpeg2000.internal.decode.seda;

import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;

// part of an experiment. Currently not in use.
public class StageConfigurer {

  private StageConfigurer() {
  }

  public static void configure(Pipeline<?> pipeline, DecoderParameters parameters) {
    for (final Stage s : pipeline.getStages()) {
      if (s instanceof Configurable)
        ((Configurable) s).configure(parameters);
    }
  }

}
