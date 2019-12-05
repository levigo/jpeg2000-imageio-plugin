package com.levigo.jadice.format.jpeg2000.internal.codestream;

import com.levigo.jadice.format.jpeg2000.internal.decode.DecoderParameters;
import com.levigo.jadice.format.jpeg2000.internal.decode.push.Pushable;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;

import java.util.LinkedList;
import java.util.List;

/**
 * Holds information used while decoding codewords belonging to a code-block.
 */
public class BlockContribution implements Pushable {

  public final CodeBlock block;

  /** The amount of passes the current packet contributes to the code-block */
  public int passes;

  public int layer;

  public boolean isLastLayer;
  
  public final List<Codeword> codewords;

  public BlockContribution(CodeBlock block) {
    this.block = block;
    codewords = new LinkedList<>();
  }

  @Override
  public void start(DecoderParameters parameters) {
    
  }

  @Override
  public void free() {

  }
}
