package com.levigo.jadice.format.jpeg2000.internal.tier1;

import java.io.IOException;
import java.util.Collection;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.concurrent.ThreadPool;
import com.levigo.jadice.format.jpeg2000.internal.image.CodeBlock;

/**
 * Performs EBCoT Decoding
 * 
 * Note: This class is part of an attempt to create asynchronous decoding logic. It doesn't work yet.
 */
@Refer(to = Spec.J2K_CORE, page = 84, section = "Annex D", called = "Coefficient Bit Modelling")
public interface BlockDecoder {
  Collection<CodeBlock> decodeBlocks(ThreadPool threadPool) throws JPEG2000Exception, IOException;
}
