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
 * Tests for {@link CogdaLiveDataUpdateMessage}.
 */
@Test(groups = TestGroup.UNIT)
public class CogdaLiveDataUpdateMessageTest {
  private static final ExternalId SUBSCRIPTION_ID = ExternalId.of("a", "b");
  private static final String NORMALIZATION_SCHEME = "scheme";
  private static final MutableFudgeMsg MSG = OpenGammaFudgeContext.getInstance().newMessage();
  static {
    MSG.add("field", "value");
  }

  /**
   * Tests that the subscription id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullId() {
    new CogdaLiveDataUpdateMessage().setSubscriptionId(null);
  }

  /**
   * Tests that the values cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValues() {
    new CogdaLiveDataUpdateMessage().setValues(null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final CogdaLiveDataUpdateMessage message = new CogdaLiveDataUpdateMessage();
    message.setNormalizationScheme(NORMALIZATION_SCHEME);
    message.setSubscriptionId(SUBSCRIPTION_ID);
    message.setValues(MSG);
    assertEquals(message, message);
    assertNotEquals(null, message);
    assertNotEquals(MSG, message);
    assertEquals(message.toString(), "CogdaLiveDataUpdateMessage{subscriptionId=a~b, normalizationScheme=scheme, values=FudgeMsg[field => value]}");
    final CogdaLiveDataUpdateMessage other = new CogdaLiveDataUpdateMessage();
    other.setNormalizationScheme(NORMALIZATION_SCHEME);
    other.setSubscriptionId(SUBSCRIPTION_ID);
    other.setValues(MSG);
    assertEquals(message, other);
    assertEquals(message.hashCode(), other.hashCode());
    other.setNormalizationScheme("scheme1");
    assertNotEquals(message, other);
    other.setNormalizationScheme(NORMALIZATION_SCHEME);
    other.setSubscriptionId(ExternalId.of("b", "c"));
    assertNotEquals(message, other);
    other.setSubscriptionId(SUBSCRIPTION_ID);
    other.setValues(OpenGammaFudgeContext.getInstance().newMessage());
    assertNotEquals(message, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final CogdaLiveDataUpdateMessage message = new CogdaLiveDataUpdateMessage();
    message.setNormalizationScheme(NORMALIZATION_SCHEME);
    message.setSubscriptionId(SUBSCRIPTION_ID);
    message.setValues(MSG);
    assertEquals(message.metaBean().normalizationScheme().get(message), NORMALIZATION_SCHEME);
    assertEquals(message.metaBean().subscriptionId().get(message), SUBSCRIPTION_ID);
    assertEquals(message.metaBean().values().get(message), MSG);
    assertEquals(message.property("normalizationScheme").get(), NORMALIZATION_SCHEME);
    assertEquals(message.property("subscriptionId").get(), SUBSCRIPTION_ID);
    assertEquals(message.property("values").get(), MSG);
  }
}
