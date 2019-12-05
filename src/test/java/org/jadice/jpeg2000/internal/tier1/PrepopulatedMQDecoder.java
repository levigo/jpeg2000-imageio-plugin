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
import java.util.Locale;

import org.junit.Assert;
import org.jadice.jpeg2000.internal.tier1.ContextContainer;
import org.jadice.jpeg2000.internal.tier1.MQDecoder;
import org.jadice.util.log.message.DefaultMessageResolver;
import org.jadice.util.log.message.MessageResolver;

public class PrepopulatedMQDecoder implements MQDecoder {
  public final int[] decisions;
  public int decisionIdx;

  public final Expectation<ContextContainer>[] ctxExpectations;
  public int expectationIdx;

  public PrepopulatedMQDecoder(int[] predefinedDecisions, Expectation<ContextContainer>[] ctxExpectations) {
    this.decisions = predefinedDecisions;
    this.ctxExpectations = ctxExpectations;
    decisionIdx = 0;
    expectationIdx = 0;
  }

  @Override
  public int decode(ContextContainer cx) throws IOException {
    if (ctxExpectations != null && ctxExpectations.length > 0) {
      final Expectation<ContextContainer> expectation = ctxExpectations[expectationIdx];

      try {
        expectation.validate(cx);
      } catch (ExpectationNotSatisfiedException e) {
        final MessageResolver messageResolver = new DefaultMessageResolver();
        Assert.fail("Expectation failed for index " + expectationIdx + ". " //
            + messageResolver.resolveMessage(e.getMessageID(), Locale.ENGLISH, e.getArguments()));
      }
    }

    return decodeRaw();
  }

  @Override
  public int decodeRaw() throws IOException {
    final Integer decision = decisions[decisionIdx];

    decisionIdx++;

    if (ctxExpectations != null && ctxExpectations.length > 0) {
      expectationIdx++;
    }

    return decision;
  }

  @Override
  public boolean checkPredictableTermination() throws IOException {
    return false;
  }
}
