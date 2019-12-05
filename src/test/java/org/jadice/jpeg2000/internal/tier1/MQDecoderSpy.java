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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Locale;

import org.jadice.jpeg2000.internal.tier1.ContextContainer;
import org.jadice.jpeg2000.internal.tier1.DefaultMQDecoder;
import org.jadice.jpeg2000.internal.tier1.MQDecoder;
import org.jadice.jpeg2000.internal.tier1.BlockDecodingSpyTestBase.MQState;
import org.jadice.util.log.message.DefaultMessageResolver;
import org.jadice.util.log.message.MessageResolver;

public class MQDecoderSpy implements MQDecoder {
  private final int[] expectedDecisions;
  private final Expectation<ContextContainer>[] expectedCtxContainers;
  private final MQState[] expectedStates;

  public int expectationIdx;

  private MQDecoder spyTarget;
  
  public MQDecoderSpy(int[] decisions, Expectation<ContextContainer>[] contexts, MQState[] states) {
    this.expectedDecisions = decisions;
    this.expectedCtxContainers = contexts;
    this.expectedStates = states;

    expectationIdx = 0;
  }

  public void setTarget(MQDecoder wrapped) {
    this.spyTarget = wrapped;
  }

  public MQDecoder getTarget() {
    if (spyTarget == null) {
      throw new IllegalStateException("spyTarget mq decoder should not be null");
    }

    return spyTarget;
  }

  @Override
  public int decode(ContextContainer cx) throws IOException {
    final Expectation<ContextContainer> expectation = expectedCtxContainers[expectationIdx];

    try {
      expectation.validate(cx);
    } catch (ExpectationNotSatisfiedException e) {
      failWithException(e);
    }

    final String idx = " #" + expectationIdx;
    
    final int expectedDecision = expectedDecisions[expectationIdx];
    final int actualDecision = getTarget().decode(cx);
    assertThat("decision #" + expectationIdx, actualDecision, is(equalTo(expectedDecision)));

    if (spyTarget instanceof DefaultMQDecoder && expectedStates != null) {
      final DefaultMQDecoder mq = (DefaultMQDecoder) spyTarget;
      final MQState expectedState = expectedStates[expectationIdx];
      assertThat("a" + idx, mq.a, is(equalTo(expectedState.a)));
      assertThat("c" + idx, mq.c, is(equalTo(expectedState.c & 0xffffffffL)));
      assertThat("cT" + idx, mq.ct, is(equalTo(expectedState.cT)));
    }

    expectationIdx++;

    return actualDecision;
  }

  private void failWithException(ExpectationNotSatisfiedException e) {
    final MessageResolver messageResolver = new DefaultMessageResolver();
    final String message = messageResolver.resolveMessage(e.getMessageID(), Locale.ENGLISH, e.getArguments());
    fail("Expectation failed for index " + expectationIdx + ". " + message);
  }

  @Override
  public int decodeRaw() throws IOException {
    final int expectedDecision = expectedDecisions[expectationIdx++];
    final int actualDecision = getTarget().decodeRaw();
    assertThat(actualDecision, is(equalTo(expectedDecision)));
    return actualDecision;
  }

  @Override
  public boolean checkPredictableTermination() throws IOException {
    return getTarget().checkPredictableTermination();
  }
}
