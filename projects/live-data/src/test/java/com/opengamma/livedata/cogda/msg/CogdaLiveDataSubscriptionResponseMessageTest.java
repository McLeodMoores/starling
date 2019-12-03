/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.cogda.msg;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link CogdaLiveDataSubscriptionResponseMessage}.
 */
@Test(groups = TestGroup.UNIT)
public class CogdaLiveDataSubscriptionResponseMessageTest {
  private static final Long CORRELATION_ID = 123L;
  private static final CogdaCommandResponseResult RESULT = CogdaCommandResponseResult.INTERNAL_ERROR;
  private static final String USER_MESSAGE = "msg";
  private static final MutableFudgeMsg SNAPSHOT = OpenGammaFudgeContext.getInstance().newMessage();
  private static final ExternalId SUBSCRIPTION_ID = ExternalId.of("a", "b");
  private static final String NORMALIZATION_SCHEME = "scheme";

  static {
    SNAPSHOT.add("field", "value");
  }

  /**
   * Tests that the result cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullResult() {
    new CogdaLiveDataSubscriptionResponseMessage().setGenericResult(null);
  }

  /**
   * Tests that the snapshot cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSnapshot() {
    new CogdaLiveDataSubscriptionResponseMessage().setSnapshot(null);
  }

  /**
   * Tests that the subscription id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSubscriptionId() {
    new CogdaLiveDataSubscriptionResponseMessage().setSubscriptionId(null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final CogdaLiveDataSubscriptionResponseMessage message = new CogdaLiveDataSubscriptionResponseMessage();
    message.setCorrelationId(CORRELATION_ID);
    message.setGenericResult(RESULT);
    message.setNormalizationScheme(NORMALIZATION_SCHEME);
    message.setSnapshot(SNAPSHOT);
    message.setSubscriptionId(SUBSCRIPTION_ID);
    message.setUserMessage(USER_MESSAGE);
    assertEquals(message, message);
    assertNotEquals(null, message);
    assertNotEquals(RESULT, message);
    assertEquals(message.toString(), "CogdaLiveDataSubscriptionResponseMessage{correlationId=123, "
        + "genericResult=INTERNAL_ERROR, userMessage=msg, subscriptionId=a~b, normalizationScheme=scheme, snapshot=FudgeMsg[field => value]}");
    final CogdaLiveDataSubscriptionResponseMessage other = new CogdaLiveDataSubscriptionResponseMessage();
    other.setCorrelationId(CORRELATION_ID);
    other.setGenericResult(RESULT);
    other.setNormalizationScheme(NORMALIZATION_SCHEME);
    other.setSnapshot(SNAPSHOT);
    other.setSubscriptionId(SUBSCRIPTION_ID);
    other.setUserMessage(USER_MESSAGE);
    assertEquals(message, other);
    assertEquals(message.hashCode(), other.hashCode());
    other.setCorrelationId(234L);
    assertNotEquals(message, other);
    other.setCorrelationId(CORRELATION_ID);
    other.setGenericResult(CogdaCommandResponseResult.NOT_AUTHORIZED);
    assertNotEquals(message, other);
    other.setGenericResult(RESULT);
    other.setNormalizationScheme("scheme2");
    assertNotEquals(message, other);
    other.setNormalizationScheme(NORMALIZATION_SCHEME);
    other.setSubscriptionId(ExternalId.of("e", "r"));
    assertNotEquals(message, other);
    other.setSubscriptionId(SUBSCRIPTION_ID);
    other.setUserMessage("user");
    assertNotEquals(message, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final CogdaLiveDataSubscriptionResponseMessage message = new CogdaLiveDataSubscriptionResponseMessage();
    message.setCorrelationId(CORRELATION_ID);
    message.setGenericResult(RESULT);
    message.setNormalizationScheme(NORMALIZATION_SCHEME);
    message.setSnapshot(SNAPSHOT);
    message.setSubscriptionId(SUBSCRIPTION_ID);
    message.setUserMessage(USER_MESSAGE);
    assertEquals(message.metaBean().correlationId().get(message), CORRELATION_ID);
    assertEquals(message.metaBean().genericResult().get(message), RESULT);
    assertEquals(message.metaBean().normalizationScheme().get(message), NORMALIZATION_SCHEME);
    assertEquals(message.metaBean().snapshot().get(message), SNAPSHOT);
    assertEquals(message.metaBean().subscriptionId().get(message), SUBSCRIPTION_ID);
    assertEquals(message.metaBean().userMessage().get(message), USER_MESSAGE);
    assertEquals(message.property("correlationId").get(), CORRELATION_ID);
    assertEquals(message.property("genericResult").get(), RESULT);
    assertEquals(message.property("normalizationScheme").get(), NORMALIZATION_SCHEME);
    assertEquals(message.property("snapshot").get(), SNAPSHOT);
    assertEquals(message.property("subscriptionId").get(), SUBSCRIPTION_ID);
    assertEquals(message.property("userMessage").get(), USER_MESSAGE);
  }
}
