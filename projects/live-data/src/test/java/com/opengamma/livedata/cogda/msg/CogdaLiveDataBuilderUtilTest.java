/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.cogda.msg;

import static org.testng.Assert.assertEquals;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link CogdaLiveDataBuilderUtil}.
 */
@Test(groups = TestGroup.UNIT)
public class CogdaLiveDataBuilderUtilTest {
  private static final CogdaLiveDataSubscriptionResponseMessage SUBSCRIPTION_MSG = new CogdaLiveDataSubscriptionResponseMessage();
  private static final CogdaLiveDataSnapshotResponseMessage SNAPSHOT_MSG = new CogdaLiveDataSnapshotResponseMessage();
  private static final long CORRELATION_ID = 123L;
  private static final CogdaCommandResponseResult RESULT = CogdaCommandResponseResult.SUCCESSFUL;
  private static final String USER_MESSAGE = "msg";
  private static final MutableFudgeMsg VALUES = OpenGammaFudgeContext.getInstance().newMessage();
  private static final MutableFudgeMsg SNAPSHOT = OpenGammaFudgeContext.getInstance().newMessage();
  private static final ExternalId SUBSCRIPTION_ID = ExternalId.of("a", "b");
  private static final String NORMALIZATION_SCHEME = "scheme";

  static {
    VALUES.add("field1", "value1");
    SNAPSHOT.add("field2", "value2");
    SUBSCRIPTION_MSG.setCorrelationId(CORRELATION_ID);
    SUBSCRIPTION_MSG.setGenericResult(RESULT);
    SUBSCRIPTION_MSG.setNormalizationScheme(NORMALIZATION_SCHEME);
    SUBSCRIPTION_MSG.setSnapshot(SNAPSHOT);
    SUBSCRIPTION_MSG.setSubscriptionId(SUBSCRIPTION_ID);
    SUBSCRIPTION_MSG.setUserMessage(USER_MESSAGE);
    SNAPSHOT_MSG.setCorrelationId(CORRELATION_ID);
    SNAPSHOT_MSG.setGenericResult(RESULT);
    SNAPSHOT_MSG.setNormalizationScheme(NORMALIZATION_SCHEME);
    SNAPSHOT_MSG.setSubscriptionId(SUBSCRIPTION_ID);
    SNAPSHOT_MSG.setUserMessage(USER_MESSAGE);
    SNAPSHOT_MSG.setValues(VALUES);
  }

  /**
   * Tests adding an external id to the message.
   */
  @Test
  public void testAddExternalId() {
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    CogdaLiveDataBuilderUtil.addExternalId(msg, SUBSCRIPTION_ID, null);
    assertEquals(msg.getAllFields().size(), 2);
    assertEquals(msg.getByName("subscriptionIdScheme").getValue(), SUBSCRIPTION_ID.getScheme().getName());
    assertEquals(msg.getByName("subscriptionIdValue").getValue(), SUBSCRIPTION_ID.getValue());
    msg = OpenGammaFudgeContext.getInstance().newMessage();
    CogdaLiveDataBuilderUtil.addExternalId(msg, SUBSCRIPTION_ID, NORMALIZATION_SCHEME);
    assertEquals(msg.getAllFields().size(), 3);
    assertEquals(msg.getByName("subscriptionIdScheme").getValue(), SUBSCRIPTION_ID.getScheme().getName());
    assertEquals(msg.getByName("subscriptionIdValue").getValue(), SUBSCRIPTION_ID.getValue());
    assertEquals(msg.getByName("normalizationScheme").getValue(), NORMALIZATION_SCHEME);
  }

  /**
   * Tests adding a response to a message.
   */
  @Test
  public void testAddResponseField() {
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    CogdaLiveDataBuilderUtil.addResponseFields(msg, SNAPSHOT_MSG);
    assertEquals(msg.getAllFields().size(), 6);
    assertEquals(((Number) msg.getByName("correlationId").getValue()).longValue(), CORRELATION_ID);
    assertEquals(msg.getByName("subscriptionIdScheme").getValue(), SUBSCRIPTION_ID.getScheme().getName());
    assertEquals(msg.getByName("subscriptionIdValue").getValue(), SUBSCRIPTION_ID.getValue());
    assertEquals(msg.getByName("normalizationScheme").getValue(), NORMALIZATION_SCHEME);
    assertEquals(msg.getByName("genericResult").getValue(), RESULT.name());
    assertEquals(msg.getByName("userMessage").getValue(), USER_MESSAGE);
    msg = OpenGammaFudgeContext.getInstance().newMessage();
    CogdaLiveDataBuilderUtil.addResponseFields(msg, SUBSCRIPTION_MSG);
    assertEquals(msg.getAllFields().size(), 6);
    assertEquals(((Number) msg.getByName("correlationId").getValue()).longValue(), CORRELATION_ID);
    assertEquals(msg.getByName("subscriptionIdScheme").getValue(), SUBSCRIPTION_ID.getScheme().getName());
    assertEquals(msg.getByName("subscriptionIdValue").getValue(), SUBSCRIPTION_ID.getValue());
    assertEquals(msg.getByName("normalizationScheme").getValue(), NORMALIZATION_SCHEME);
    assertEquals(msg.getByName("genericResult").getValue(), RESULT.name());
    assertEquals(msg.getByName("userMessage").getValue(), USER_MESSAGE);
  }

  /**
   * Tests adding values to the response.
   */
  @Test
  public void testSetSnapshotResponseFields() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    CogdaLiveDataBuilderUtil.addResponseFields(msg, SNAPSHOT_MSG);
    final CogdaLiveDataSnapshotResponseMessage snapshot = new CogdaLiveDataSnapshotResponseMessage();
    CogdaLiveDataBuilderUtil.setResponseFields(msg, snapshot);
    final CogdaLiveDataSnapshotResponseMessage expected = new CogdaLiveDataSnapshotResponseMessage();
    expected.setCorrelationId(CORRELATION_ID);
    expected.setGenericResult(RESULT);
    expected.setNormalizationScheme(NORMALIZATION_SCHEME);
    expected.setSubscriptionId(SUBSCRIPTION_ID);
    expected.setUserMessage(USER_MESSAGE);
    assertEquals(snapshot, expected);
  }

  /**
   * Tests adding values to the response.
   */
  @Test
  public void testSetSubscriptionResponseFields() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    CogdaLiveDataBuilderUtil.addResponseFields(msg, SUBSCRIPTION_MSG);
    final CogdaLiveDataSubscriptionResponseMessage snapshot = new CogdaLiveDataSubscriptionResponseMessage();
    CogdaLiveDataBuilderUtil.setResponseFields(msg, snapshot);
    final CogdaLiveDataSubscriptionResponseMessage expected = new CogdaLiveDataSubscriptionResponseMessage();
    expected.setCorrelationId(CORRELATION_ID);
    expected.setGenericResult(RESULT);
    expected.setNormalizationScheme(NORMALIZATION_SCHEME);
    expected.setSubscriptionId(SUBSCRIPTION_ID);
    expected.setUserMessage(USER_MESSAGE);
    assertEquals(snapshot, expected);
  }

  /**
   * Tests building a command response.
   */
  @Test
  public void testBuildSnapshotCommandResponseMessage() {
    final FudgeMsg msg = CogdaLiveDataBuilderUtil.buildCommandResponseMessage(OpenGammaFudgeContext.getInstance(), SNAPSHOT_MSG);
    assertEquals(msg.getAllFields().size(), 8);
    assertEquals(((Number) msg.getByName("correlationId").getValue()).longValue(), CORRELATION_ID);
    assertEquals(msg.getByName("subscriptionIdScheme").getValue(), SUBSCRIPTION_ID.getScheme().getName());
    assertEquals(msg.getByName("subscriptionIdValue").getValue(), SUBSCRIPTION_ID.getValue());
    assertEquals(msg.getByName("normalizationScheme").getValue(), NORMALIZATION_SCHEME);
    assertEquals(msg.getByName("genericResult").getValue(), RESULT.name());
    assertEquals(msg.getByName("userMessage").getValue(), USER_MESSAGE);
    assertEquals(msg.getByName("values").getValue(), VALUES);
    assertEquals(msg.getByName("MESSAGE_TYPE").getValue(), CogdaMessageType.SNAPSHOT_RESPONSE.name());
  }

  /**
   * Tests building a command response.
   */
  @Test
  public void testBuildSubscriptionCommandResponseMessage() {
    final FudgeMsg msg = CogdaLiveDataBuilderUtil.buildCommandResponseMessage(OpenGammaFudgeContext.getInstance(), SUBSCRIPTION_MSG);
    assertEquals(msg.getAllFields().size(), 8);
    assertEquals(((Number) msg.getByName("correlationId").getValue()).longValue(), CORRELATION_ID);
    assertEquals(msg.getByName("subscriptionIdScheme").getValue(), SUBSCRIPTION_ID.getScheme().getName());
    assertEquals(msg.getByName("subscriptionIdValue").getValue(), SUBSCRIPTION_ID.getValue());
    assertEquals(msg.getByName("normalizationScheme").getValue(), NORMALIZATION_SCHEME);
    assertEquals(msg.getByName("genericResult").getValue(), RESULT.name());
    assertEquals(msg.getByName("snapshot").getValue(), SNAPSHOT);
    assertEquals(msg.getByName("userMessage").getValue(), USER_MESSAGE);
    assertEquals(msg.getByName("MESSAGE_TYPE").getValue(), CogdaMessageType.SUBSCRIPTION_RESPONSE.name());
  }
}
