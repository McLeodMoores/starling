/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.cogda.msg;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link CogdaLiveDataSnapshotResponseMessage}.
 */
@Test(groups = TestGroup.UNIT)
public class CogdaLiveDataSnapshotResponseMessageTest {
  private static final Long CORRELATION_ID = 123L;
  private static final CogdaCommandResponseResult RESULT = CogdaCommandResponseResult.INTERNAL_ERROR;
  private static final String USER_MESSAGE = "msg";
  private static final MutableFudgeMsg VALUES = OpenGammaFudgeContext.getInstance().newMessage();
  private static final String NORMALIZATION_SCHEME = "scheme";

  static {
    VALUES.add("field", "value");
  }

  /**
   * Tests that the result cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullResult() {
    new CogdaLiveDataSnapshotResponseMessage().setGenericResult(null);
  }

  /**
   * Tests that the values cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValues() {
    new CogdaLiveDataSnapshotResponseMessage().setValues(null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final CogdaLiveDataSnapshotResponseMessage message = new CogdaLiveDataSnapshotResponseMessage();
    message.setCorrelationId(CORRELATION_ID);
    message.setGenericResult(RESULT);
    message.setNormalizationScheme(NORMALIZATION_SCHEME);
    message.setValues(VALUES);
    message.setUserMessage(USER_MESSAGE);
    assertEquals(message, message);
    assertNotEquals(null, message);
    assertNotEquals(RESULT, message);
    assertEquals(message.toString(), "CogdaLiveDataSnapshotResponseMessage{correlationId=123, "
        + "genericResult=INTERNAL_ERROR, userMessage=msg, subscriptionId=null, normalizationScheme=scheme, values=FudgeMsg[field => value]}");
    final CogdaLiveDataSnapshotResponseMessage other = new CogdaLiveDataSnapshotResponseMessage();
    other.setCorrelationId(CORRELATION_ID);
    other.setGenericResult(RESULT);
    other.setNormalizationScheme(NORMALIZATION_SCHEME);
    other.setValues(VALUES);
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
    other.setUserMessage("user");
    assertNotEquals(message, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final CogdaLiveDataSnapshotResponseMessage message = new CogdaLiveDataSnapshotResponseMessage();
    message.setCorrelationId(CORRELATION_ID);
    message.setGenericResult(RESULT);
    message.setNormalizationScheme(NORMALIZATION_SCHEME);
    message.setValues(VALUES);
    message.setUserMessage(USER_MESSAGE);
    assertEquals(message.metaBean().correlationId().get(message), CORRELATION_ID);
    assertEquals(message.metaBean().genericResult().get(message), RESULT);
    assertEquals(message.metaBean().normalizationScheme().get(message), NORMALIZATION_SCHEME);
    assertEquals(message.metaBean().userMessage().get(message), USER_MESSAGE);
    assertEquals(message.metaBean().values().get(message), VALUES);
    assertEquals(message.property("correlationId").get(), CORRELATION_ID);
    assertEquals(message.property("genericResult").get(), RESULT);
    assertEquals(message.property("normalizationScheme").get(), NORMALIZATION_SCHEME);
    assertEquals(message.property("userMessage").get(), USER_MESSAGE);
    assertEquals(message.property("values").get(), VALUES);
  }
}
