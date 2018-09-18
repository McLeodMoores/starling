/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.cogda.msg;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link CogdaLiveDataSubscriptionRequestMessage}.
 */
@Test(groups = TestGroup.UNIT)
public class CogdaLiveDataSubscriptionRequestMessageTest {
  private static final Long CORRELATION_ID = 123L;
  private static final ExternalId SUBSCRIPTION_ID = ExternalId.of("a", "b");
  private static final String NORMALIZATION_SCHEME = "scheme";

  /**
   * Tests that the subscription id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSubscriptionId() {
    new CogdaLiveDataSubscriptionRequestMessage().setSubscriptionId(null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final CogdaLiveDataSubscriptionRequestMessage message = new CogdaLiveDataSubscriptionRequestMessage();
    message.setCorrelationId(CORRELATION_ID);
    message.setNormalizationScheme(NORMALIZATION_SCHEME);
    message.setSubscriptionId(SUBSCRIPTION_ID);
    assertEquals(message, message);
    assertNotEquals(null, message);
    assertNotEquals(SUBSCRIPTION_ID, message);
    assertEquals(message.toString(), "CogdaLiveDataSubscriptionRequestMessage{correlationId=123, subscriptionId=a~b, normalizationScheme=scheme}");
    final CogdaLiveDataSubscriptionRequestMessage other = new CogdaLiveDataSubscriptionRequestMessage();
    other.setCorrelationId(CORRELATION_ID);
    other.setNormalizationScheme(NORMALIZATION_SCHEME);
    other.setSubscriptionId(SUBSCRIPTION_ID);
    assertEquals(message, other);
    assertEquals(message.hashCode(), other.hashCode());
    other.setCorrelationId(234L);
    assertNotEquals(message, other);
    other.setCorrelationId(CORRELATION_ID);
    other.setNormalizationScheme("scheme2");
    assertNotEquals(message, other);
    other.setNormalizationScheme(NORMALIZATION_SCHEME);
    other.setSubscriptionId(ExternalId.of("e", "r"));
    assertNotEquals(message, other);
    other.setSubscriptionId(SUBSCRIPTION_ID);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final CogdaLiveDataSubscriptionRequestMessage message = new CogdaLiveDataSubscriptionRequestMessage();
    message.setCorrelationId(CORRELATION_ID);
    message.setNormalizationScheme(NORMALIZATION_SCHEME);
    message.setSubscriptionId(SUBSCRIPTION_ID);
    assertEquals(message.metaBean().correlationId().get(message), CORRELATION_ID);
    assertEquals(message.metaBean().normalizationScheme().get(message), NORMALIZATION_SCHEME);
    assertEquals(message.metaBean().subscriptionId().get(message), SUBSCRIPTION_ID);
    assertEquals(message.property("correlationId").get(), CORRELATION_ID);
    assertEquals(message.property("normalizationScheme").get(), NORMALIZATION_SCHEME);
    assertEquals(message.property("subscriptionId").get(), SUBSCRIPTION_ID);
  }
}
