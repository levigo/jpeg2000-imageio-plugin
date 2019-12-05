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
package com.levigo.jadice.format.jpeg2000.internal.debug;

import java.io.IOException;

public class Inspect {

  public static void main(String[] args) throws IOException {
    // final String filePath = "E:\\work\\support\\jsx\\2655\\im4.j2k";
    //
    // final File mySourceFile = new File(filePath + ".jad.ph");
    // final FileInputStream mySource = new FileInputStream(mySourceFile);
    // final FileOutputStream refSource = new FileOutputStream(new File(filePath + ".kdu.ph"));
    // try {
    // final BasicPacketHeaderProtocol protocol = new BasicPacketHeaderProtocol();
    //
    // final BasicProtocolReader myReader = new BasicProtocolReader(mySource, protocol);
    // final WritingProtocolListener refChannel = new WritingProtocolListener(jadSinkAndSource);
    // final BasicPacketHeaderProtocol refProtocol = new BasicPacketHeaderProtocol();
    // refProtocol.addProtocolListener(jadWriter);
    //
    // ProtocolElement element = myReader.nextRead();
    // ProtocolElement refElement = refChannel.nextRead();
    // do {
    // TODO feed the diff ui
    // System.out.println(element);
    // System.out.println(refElement);
    // } while ((element = myReader.nextRead()) != null && (refElement = refChannel.nextRead()) !=
    // null);
    // } finally {
    // refSource.close();
    // jadSink.close();
    // }
  }
}
