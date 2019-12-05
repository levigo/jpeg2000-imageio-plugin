package com.levigo.jadice.format.jpeg2000.internal.tier1;

import java.io.IOException;
import java.util.Locale;

import org.junit.Assert;

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
