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
package org.jadice.jpeg2000.internal.fileformat;

import static org.jadice.jpeg2000.internal.fileformat.BoxType.FileType;

import java.io.IOException;

import org.jadice.jpeg2000.JPEG2000Exception;

/**
 * Specified by <i>ITU-T.800, I.5.2</i>
 * <p>
 * The File Type box specifies the Recommendation | International Standard which completely defines all of the contents
 * of this file, as well as a separate list of readers, defined by other Recommendations | International Standards,
 * with which this file is compatible, and thus the file can be properly interpreted within the scope of that other
 * standard. This box shall immediately follow the JPEG 2000 Signature box. This differentiates between the standard
 * which completely describes the file, from other standards that interpret a subset of the file.
 */
public class FileTypeBox extends Box {
  @Override
  public BoxType getBoxType() {
    return FileType;
  }
  
  @Override
  protected boolean readDBox() throws JPEG2000Exception, IOException {
    return false;
  }
}
