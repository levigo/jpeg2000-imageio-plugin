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
package com.levigo.jadice.format.jpeg2000.internal.marker;

import com.levigo.jadice.format.jpeg2000.internal.param.DirectParameterInfo;
import com.levigo.jadice.format.jpeg2000.internal.param.ParameterInfo;

import java.util.Collection;
import java.util.Formatter;
import java.util.LinkedList;

public class MarkerInfo {

  private MarkerSegment markerSegment;

  private final Collection<ParameterInfo> parameters;

  private int maxNameLength;
  private int maxValueLength;

  public MarkerInfo(MarkerSegment markerSegment) {
    this.markerSegment = markerSegment;
    parameters = new LinkedList<>();
  }

  public void add(ParameterInfo paramInfo) {
    parameters.add(paramInfo);

    final String name = paramInfo.getName();
    if (name.length() > maxNameLength) {
      maxNameLength = name.length();
    }

    final String valueString = paramInfo.getValue();
    if (valueString.length() > maxValueLength) {
      maxValueLength = valueString.length();
    }
  }

  public String getFormatted() {
    final StringBuilder sb = new StringBuilder();

    sb.append(markerSegment.getMarker() + "\n");

    final Formatter formatter = new Formatter(sb);

    final String formatFull = "%1$" + (maxNameLength + 2) + "s = %2$-" + maxValueLength + "s " + "(%3$s)" + "\n";
    final String formatLight = "%1$" + (maxNameLength + 2) + "s = %2$-" + maxValueLength + "s \n";

    for (ParameterInfo parameter : parameters) {
      if (parameter.getDescription() != null) {
        formatter.format(formatFull, parameter.getName(), parameter.getValue(), parameter.getDescription());
      } else {
        formatter.format(formatLight, parameter.getName(), parameter.getValue());
      }
    }

    return sb.toString();
  }

}
