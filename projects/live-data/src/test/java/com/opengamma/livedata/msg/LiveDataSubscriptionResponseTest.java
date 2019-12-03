/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.msg;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test for {@link LiveDataSubscriptionResponse}.
 */
@Test(groups = TestGroup.UNIT)
public class LiveDataSubscriptionResponseTest {
  private static final LiveDataSpecification REQUEST = new LiveDataSpecification("rules", Arrays.asList(ExternalId.of("eid", "1"), ExternalId.of("eid", "2")));
  private static final String USER_MESSAGE = "user message";
  private static final LiveDataSpecification FULLY_QUALIFIED_REQUEST = new LiveDataSpecification("rules", Arrays.asList(ExternalId.of("eid", "1")));
  private static final String TICK_DISTRIBUTION = "tick distribution";
  private static final LiveDataValueUpdateBean SNAPSHOT = new LiveDataValueUpdateBean(100L, REQUEST,
      new FudgeSerializer(OpenGammaFudgeContext.getInstance()).newMessage());

  /**
   * Tests that the requested specification cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullRequest() {
    new LiveDataSubscriptionResponse(null, LiveDataSubscriptionResult.SUCCESS);
  }

  /**
   * Tests that the result cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullResponse() {
    new LiveDataSubscriptionResponse(REQUEST, null);
  }

  /**
   * Tests that the message must contain the specification key.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoSpecificationKey() {
    new LiveDataSubscriptionResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()),
        new FudgeSerializer(OpenGammaFudgeContext.getInstance()).newMessage());
  }

  /**
   * Tests that the message must contain a subscription result key.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongTypeInMessage() {
    final MutableFudgeMsg msg = new FudgeSerializer(OpenGammaFudgeContext.getInstance()).newMessage();
    msg.add(LiveDataSubscriptionResponse.REQUESTED_SPECIFICATION_KEY, "value");
    new LiveDataSubscriptionResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
  }

  /**
   * Tests that the message must contain a subscription result.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoSubscriptionKeyInMessage() {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.REQUESTED_SPECIFICATION_KEY, null, REQUEST);
    new LiveDataSubscriptionResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
  }

  /**
   * Tests that the message must contain a subscription key.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongSubscriptionKeyInMessage() {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.REQUESTED_SPECIFICATION_KEY, null, REQUEST);
    msg.add(LiveDataSubscriptionResponse.SUBSCRIPTION_RESULT_KEY, "value");
    new LiveDataSubscriptionResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
  }

  /**
   * Tests that the fully-qualified specification must be the correct type.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongFullyQualifiedSpecificationInMessage() {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.REQUESTED_SPECIFICATION_KEY, null, REQUEST);
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.SUBSCRIPTION_RESULT_KEY, null, LiveDataSubscriptionResult.NOT_PRESENT.name());
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.FULLY_QUALIFIED_SPECIFICATION_KEY, null, LiveDataSubscriptionResult.NOT_PRESENT.name());
    new LiveDataSubscriptionResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
  }

  /**
   * Tests that the snapshot key must be the correct type.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongSnapshotKeyInMessage() {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.REQUESTED_SPECIFICATION_KEY, null, REQUEST);
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.SUBSCRIPTION_RESULT_KEY, null, LiveDataSubscriptionResult.NOT_PRESENT.name());
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.SNAPSHOT_KEY, null, LiveDataSubscriptionResult.NOT_PRESENT.name());
    new LiveDataSubscriptionResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
  }

  /**
   * Tests that the user message can be null.
   */
  public void testNullUserMessageInMessage() {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.REQUESTED_SPECIFICATION_KEY, null, REQUEST);
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.SUBSCRIPTION_RESULT_KEY, null, LiveDataSubscriptionResult.NOT_PRESENT.name());
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.USER_MESSAGE_KEY, null, null);
    final LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
    assertNull(response.getUserMessage());
  }

  /**
   * Tests that the tick distribution specification key can be null.
   */
  public void testNullTickDistributionKeyInMessage() {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.REQUESTED_SPECIFICATION_KEY, null, REQUEST);
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.SUBSCRIPTION_RESULT_KEY, null, LiveDataSubscriptionResult.NOT_PRESENT.name());
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.TICK_DISTRIBUTION_SPECIFICATION_KEY, null, null);
    final LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
    assertNull(response.getTickDistributionSpecification());
  }

  /**
   * Tests that the requested specification cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullRequestedSpecification() {
    new LiveDataSubscriptionResponse(null, LiveDataSubscriptionResult.INTERNAL_ERROR, USER_MESSAGE, REQUEST, TICK_DISTRIBUTION, SNAPSHOT);
  }

  /**
   * Tests that the requested specification cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSetNullRequestedSpecification() {
    final LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(REQUEST, LiveDataSubscriptionResult.INTERNAL_ERROR, USER_MESSAGE,
        FULLY_QUALIFIED_REQUEST, TICK_DISTRIBUTION,
        SNAPSHOT);
    response.setRequestedSpecification(null);
  }

  /**
   * Tests that the subscription result cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullSubscriptionResult() {
    new LiveDataSubscriptionResponse(REQUEST, null, USER_MESSAGE, REQUEST, TICK_DISTRIBUTION, SNAPSHOT);
  }

  /**
   * Tests that the subscription result cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSetNullSubscriptionResult() {
    final LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(REQUEST, LiveDataSubscriptionResult.INTERNAL_ERROR, USER_MESSAGE, REQUEST,
        TICK_DISTRIBUTION, SNAPSHOT);
    response.setSubscriptionResult(null);
  }

  /**
   * Tests that the user message can be null.
   */
  public void testNullUserMessage() {
    new LiveDataSubscriptionResponse(REQUEST, LiveDataSubscriptionResult.INTERNAL_ERROR, null, REQUEST, TICK_DISTRIBUTION, SNAPSHOT);
  }

  /**
   * Tests that the user message can be null.
   */
  public void testSetNullUserMessage() {
    final LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(REQUEST, LiveDataSubscriptionResult.INTERNAL_ERROR, USER_MESSAGE, REQUEST,
        TICK_DISTRIBUTION, SNAPSHOT);
    response.setUserMessage(null);
  }

  /**
   * Tests that the fully-qualified requested specification can be null.
   */
  public void testNullFullyQualifiedRequestedSpecification() {
    new LiveDataSubscriptionResponse(REQUEST, LiveDataSubscriptionResult.INTERNAL_ERROR, USER_MESSAGE, null, TICK_DISTRIBUTION, SNAPSHOT);
  }

  /**
   * Tests that the fully-qualified requested specification can be null.
   */
  public void testSetNullFullyQualifiedRequestedSpecification() {
    final LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(REQUEST, LiveDataSubscriptionResult.INTERNAL_ERROR, USER_MESSAGE,
        FULLY_QUALIFIED_REQUEST, TICK_DISTRIBUTION, SNAPSHOT);
    response.setFullyQualifiedSpecification(null);
  }

  /**
   * Tests that the tick distribution can be null.
   */
  public void testNullTickDistribution() {
    new LiveDataSubscriptionResponse(REQUEST, LiveDataSubscriptionResult.INTERNAL_ERROR, USER_MESSAGE, REQUEST, null, SNAPSHOT);
  }

  /**
   * Tests that the tick distribution can be null.
   */
  public void testSetNullTickDistribution() {
    final LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(REQUEST, LiveDataSubscriptionResult.INTERNAL_ERROR, USER_MESSAGE, REQUEST,
        TICK_DISTRIBUTION, SNAPSHOT);
    response.setFullyQualifiedSpecification(null);
  }

  /**
   * Tests that the snapshot can be null.
   */
  public void testNullSnapshot() {
    new LiveDataSubscriptionResponse(REQUEST, LiveDataSubscriptionResult.INTERNAL_ERROR, USER_MESSAGE, REQUEST, TICK_DISTRIBUTION, null);
  }

  /**
   * Tests that the snapshot can be null.
   */
  public void testSetNullSnapshot() {
    final LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(REQUEST, LiveDataSubscriptionResult.INTERNAL_ERROR, USER_MESSAGE, REQUEST,
        TICK_DISTRIBUTION, null);
    response.setSnapshot(null);
  }

  /**
   * Tests that the response cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullSourceCopyConstructor() {
    new LiveDataSubscriptionResponse(null);
  }

  /**
   * Tests that the fields are set when converting from a message.
   */
  public void testFieldsSetFromMessage() {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.REQUESTED_SPECIFICATION_KEY, null, REQUEST);
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.SUBSCRIPTION_RESULT_KEY, null, LiveDataSubscriptionResult.NOT_PRESENT.name());
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.USER_MESSAGE_KEY, null, USER_MESSAGE);
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.FULLY_QUALIFIED_SPECIFICATION_KEY, null, FULLY_QUALIFIED_REQUEST);
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.TICK_DISTRIBUTION_SPECIFICATION_KEY, null, TICK_DISTRIBUTION);
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.SNAPSHOT_KEY, null, SNAPSHOT);
    final LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
    assertEquals(response.getFullyQualifiedSpecification(), FULLY_QUALIFIED_REQUEST);
    assertEquals(response.getRequestedSpecification(), REQUEST);
    assertEquals(response.getSnapshot(), SNAPSHOT);
    assertEquals(response.getSubscriptionResult(), LiveDataSubscriptionResult.NOT_PRESENT);
    assertEquals(response.getTickDistributionSpecification(), TICK_DISTRIBUTION);
    assertEquals(response.getUserMessage(), USER_MESSAGE);
  }

  /**
   * Tests that the fields are set in the copy constructor.
   */
  public void testFieldsSet() {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.REQUESTED_SPECIFICATION_KEY, null, REQUEST);
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.SUBSCRIPTION_RESULT_KEY, null, LiveDataSubscriptionResult.NOT_PRESENT.name());
    LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
    LiveDataSubscriptionResponse newResponse = new LiveDataSubscriptionResponse(response);
    assertNull(newResponse.getFullyQualifiedSpecification());
    assertEquals(newResponse.getRequestedSpecification(), REQUEST);
    assertNull(newResponse.getSnapshot());
    assertEquals(newResponse.getSubscriptionResult(), LiveDataSubscriptionResult.NOT_PRESENT);
    assertNull(newResponse.getTickDistributionSpecification());
    assertNull(newResponse.getUserMessage());
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.USER_MESSAGE_KEY, null, USER_MESSAGE);
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.FULLY_QUALIFIED_SPECIFICATION_KEY, null, FULLY_QUALIFIED_REQUEST);
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.TICK_DISTRIBUTION_SPECIFICATION_KEY, null, TICK_DISTRIBUTION);
    serializer.addToMessage(msg, LiveDataSubscriptionResponse.SNAPSHOT_KEY, null, SNAPSHOT);
    response = new LiveDataSubscriptionResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
    newResponse = new LiveDataSubscriptionResponse(response);
    assertEquals(newResponse.getFullyQualifiedSpecification(), FULLY_QUALIFIED_REQUEST);
    assertEquals(newResponse.getRequestedSpecification(), REQUEST);
    assertEquals(newResponse.getSnapshot(), SNAPSHOT);
    assertEquals(newResponse.getSubscriptionResult(), LiveDataSubscriptionResult.NOT_PRESENT);
    assertEquals(newResponse.getTickDistributionSpecification(), TICK_DISTRIBUTION);
    assertEquals(newResponse.getUserMessage(), USER_MESSAGE);
    newResponse = response.clone();
    assertEquals(newResponse.getFullyQualifiedSpecification(), FULLY_QUALIFIED_REQUEST);
    assertEquals(newResponse.getRequestedSpecification(), REQUEST);
    assertEquals(newResponse.getSnapshot(), SNAPSHOT);
    assertEquals(newResponse.getSubscriptionResult(), LiveDataSubscriptionResult.NOT_PRESENT);
    assertEquals(newResponse.getTickDistributionSpecification(), TICK_DISTRIBUTION);
    assertEquals(newResponse.getUserMessage(), USER_MESSAGE);
  }

  /**
   * Tests construction of a simple response.
   */
  public void simpleConstruction() {
    final LiveDataSpecification lds = new LiveDataSpecification("Foo", ExternalId.of("A", "B"));
    final LiveDataSubscriptionResponse ldsr = new LiveDataSubscriptionResponse(lds,
        LiveDataSubscriptionResult.SUCCESS,
        null,
        lds,
        null,
        null);
    assertEquals("Foo", lds.getNormalizationRuleSetId());
    assertEquals(ExternalIdBundle.of("A", "B"), lds.getIdentifiers());
    assertEquals(LiveDataSubscriptionResult.SUCCESS, ldsr.getSubscriptionResult());
    assertEquals(lds, ldsr.getRequestedSpecification());
    assertEquals(lds.toString(), "LiveDataSpecification[Bundle[A~B]:Foo]");
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    final LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(REQUEST, LiveDataSubscriptionResult.INTERNAL_ERROR, USER_MESSAGE,
        FULLY_QUALIFIED_REQUEST, TICK_DISTRIBUTION, SNAPSHOT);
    LiveDataSubscriptionResponse cycled = deserializer.fudgeMsgToObject(LiveDataSubscriptionResponse.class, serializer.objectToFudgeMsg(response));
    assertEquals(cycled.getFullyQualifiedSpecification(), FULLY_QUALIFIED_REQUEST);
    assertEquals(cycled.getRequestedSpecification(), REQUEST);
    assertEquals(cycled.getSnapshot(), SNAPSHOT);
    assertEquals(cycled.getSubscriptionResult(), LiveDataSubscriptionResult.INTERNAL_ERROR);
    assertEquals(cycled.getTickDistributionSpecification(), TICK_DISTRIBUTION);
    assertEquals(cycled.getUserMessage(), USER_MESSAGE);
    response.setFullyQualifiedSpecification(null);
    cycled = deserializer.fudgeMsgToObject(LiveDataSubscriptionResponse.class, serializer.objectToFudgeMsg(response));
    assertNull(cycled.getFullyQualifiedSpecification());
    assertEquals(cycled.getRequestedSpecification(), REQUEST);
    assertEquals(cycled.getSnapshot(), SNAPSHOT);
    assertEquals(cycled.getSubscriptionResult(), LiveDataSubscriptionResult.INTERNAL_ERROR);
    assertEquals(cycled.getTickDistributionSpecification(), TICK_DISTRIBUTION);
    assertEquals(cycled.getUserMessage(), USER_MESSAGE);
    response.setSnapshot(null);
    cycled = deserializer.fudgeMsgToObject(LiveDataSubscriptionResponse.class, serializer.objectToFudgeMsg(response));
    assertNull(cycled.getFullyQualifiedSpecification());
    assertEquals(cycled.getRequestedSpecification(), REQUEST);
    assertNull(cycled.getSnapshot());
    assertEquals(cycled.getSubscriptionResult(), LiveDataSubscriptionResult.INTERNAL_ERROR);
    assertEquals(cycled.getTickDistributionSpecification(), TICK_DISTRIBUTION);
    assertEquals(cycled.getUserMessage(), USER_MESSAGE);
    response.setTickDistributionSpecification(null);
    cycled = deserializer.fudgeMsgToObject(LiveDataSubscriptionResponse.class, serializer.objectToFudgeMsg(response));
    assertNull(cycled.getFullyQualifiedSpecification());
    assertEquals(cycled.getRequestedSpecification(), REQUEST);
    assertNull(cycled.getSnapshot());
    assertEquals(cycled.getSubscriptionResult(), LiveDataSubscriptionResult.INTERNAL_ERROR);
    assertNull(cycled.getTickDistributionSpecification());
    assertEquals(cycled.getUserMessage(), USER_MESSAGE);
    response.setUserMessage(null);
    cycled = deserializer.fudgeMsgToObject(LiveDataSubscriptionResponse.class, serializer.objectToFudgeMsg(response));
    assertNull(cycled.getFullyQualifiedSpecification());
    assertEquals(cycled.getRequestedSpecification(), REQUEST);
    assertNull(cycled.getSnapshot());
    assertEquals(cycled.getSubscriptionResult(), LiveDataSubscriptionResult.INTERNAL_ERROR);
    assertNull(cycled.getTickDistributionSpecification());
    assertNull(cycled.getUserMessage());
  }

}
