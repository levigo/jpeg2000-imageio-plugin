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
package com.levigo.jadice.format.jpeg2000.internal.codestream;

import com.levigo.jadice.format.jpeg2000.internal.marker.MarkerSegment;

import java.util.Iterator;
import java.util.LinkedList;

public class MarkerAccessGraph extends LinkedList<MarkerAccessNode> {

  public MarkerSegment access() {
    final Iterator<MarkerAccessNode> iterator = iterator();
    MarkerSegment markerSegment = null;

    while (markerSegment == null && iterator.hasNext()) {
      final MarkerAccessNode node = iterator.next();
      markerSegment = node.access();
    }

    return markerSegment;
  }

}
