package com.levigo.jadice.format.jpeg2000.internal.tier1;

import java.io.IOException;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;
import com.levigo.jadice.format.jpeg2000.internal.image.SubbandType;

public class SignificanceRawPass implements Pass {

  @Override
  public boolean run(MQDecoder mq, CodeBlock block, SubbandType type, int p, int width,
      int ctxRowGap)
      throws JPEG2000Exception, IOException {

    // FIXME not yet implemented. No test files available.
    
    return false;
  }

  @Override
  public Pass next(CodeBlock block) {
    return null;
  }
}
