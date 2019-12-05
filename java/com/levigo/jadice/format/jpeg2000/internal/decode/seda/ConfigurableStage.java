package com.levigo.jadice.format.jpeg2000.internal.decode.seda;


import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;

// part of an experiment. Currently not in use.
public abstract class ConfigurableStage implements Stage, Configurable {

  protected DecoderParameters parameters;

  public ConfigurableStage() {
    super();
  }

  @Override
  public void configure(DecoderParameters param) {
    parameters = param;
  }

}