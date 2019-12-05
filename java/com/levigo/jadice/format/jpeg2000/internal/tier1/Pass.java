package com.levigo.jadice.format.jpeg2000.internal.tier1;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.SubbandType;

import java.io.IOException;

/** Interface defining the default lifecycle of a coefficient bit modelling pass. */
public interface Pass {

  boolean run(MQDecoder mq, CodeBlock block, SubbandType type, int p, int width, int ctxRowGap)
      throws JPEG2000Exception, IOException;

  Pass next(CodeBlock block);
}
