/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.msg;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ResolveRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class ResolveRequestTest {
  private static final LiveDataSpecification RESOLVED = new LiveDataSpecification("rules", Arrays.asList(ExternalId.of("eid", "1"), ExternalId.of("eid", "2")));

  /**
   * Tests that the resolved specification cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullRequestedSpecification() {
    new ResolveRequest((LiveDataSpecification) null);
  }

  /**
   * Tests that the resolved specification cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSetNullRequestedSpecification() {
    final ResolveRequest response = new ResolveRequest(RESOLVED);
    response.setRequestedSpecification(null);
  }

  /**
   * Tests that the source cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullSource() {
    new ResolveRequest((ResolveRequest) null);
  }

  /**
   * Tests that there must be a specification in the message.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoSpecificationInMessage() {
    new ResolveRequest(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), new FudgeSerializer(OpenGammaFudgeContext.getInstance()).newMessage());
  }

  /**
   * Tests that the specification message must be the correct type.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongTypeInMessage() {
    final MutableFudgeMsg msg = new FudgeSerializer(OpenGammaFudgeContext.getInstance()).newMessage();
    msg.add(ResolveRequest.REQUESTED_SPECIFICATION_KEY, "value");
    new ResolveRequest(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
  }

  /**
   * Tests the copy constructor.
   */
  public void testCopyConstructor() {
    final ResolveRequest response = new ResolveRequest(RESOLVED);
    final ResolveRequest copied = new ResolveRequest(response);
    assertEquals(response.getRequestedSpecification(), copied.getRequestedSpecification());
  }

  /**
   * Tests the clone.
   */
  public void testClone() {
    final ResolveRequest response = new ResolveRequest(RESOLVED);
    final ResolveRequest clone = response.clone();
    assertEquals(response.getRequestedSpecification(), clone.getRequestedSpecification());
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    final ResolveRequest response = new ResolveRequest(RESOLVED);
    final ResolveRequest cycled = deserializer.fudgeMsgToObject(ResolveRequest.class, serializer.objectToFudgeMsg(response));
    assertEquals(response.getRequestedSpecification(), cycled.getRequestedSpecification());
  }
}
