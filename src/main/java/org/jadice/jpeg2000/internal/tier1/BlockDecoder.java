/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jadice.jpeg2000.internal.tier1;

import java.io.IOException;
import java.util.Collection;

import org.jadice.jpeg2000.JPEG2000Exception;
import org.jadice.jpeg2000.internal.concurrent.ThreadPool;
import org.jadice.jpeg2000.internal.image.CodeBlock;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;

/**
 * Performs EBCoT Decoding
 * 
 * Note: This class is part of an attempt to create asynchronous decoding logic. It doesn't work yet.
 */
@Refer(to = Spec.J2K_CORE, page = 84, section = "Annex D", called = "Coefficient Bit Modelling")
public interface BlockDecoder {
  Collection<CodeBlock> decodeBlocks(ThreadPool threadPool) throws JPEG2000Exception, IOException;
}
