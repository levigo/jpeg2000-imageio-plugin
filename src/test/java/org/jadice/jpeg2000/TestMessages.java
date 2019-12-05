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
package org.jadice.jpeg2000;

import org.jadice.util.log.message.LogMessage;
import org.jadice.util.log.qualified.MessageID;

public enum TestMessages implements MessageID {
  @LogMessage("Unexpected error: {0}")
  UNEXPECTED_ERROR,

  @LogMessage("Expectation not satisfied. Expected {0}. Actual {1}.")
  EXPECTATION_NOT_SATISFIED;

  private static final String COMPONENT_ID = "JPEG2000.TEST";

  @Override
  public String getComponentID() {
    return COMPONENT_ID;
  }
}
