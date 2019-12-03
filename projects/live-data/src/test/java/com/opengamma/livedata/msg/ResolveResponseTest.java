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
 * Tests for {@link ResolveResponse}.
 */
@Test(groups = TestGroup.UNIT)
public class ResolveResponseTest {
  private static final LiveDataSpecification RESOLVED = new LiveDataSpecification("rules", Arrays.asList(ExternalId.of("eid", "1"), ExternalId.of("eid", "2")));

  /**
   * Tests that the resolved specification cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullResolvedSpecification() {
    new ResolveResponse((LiveDataSpecification) null);
  }

  /**
   * Tests that the resolved specification cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSetNullResolvedSpecification() {
    final ResolveResponse response = new ResolveResponse(RESOLVED);
    response.setResolvedSpecification(null);
  }

  /**
   * Tests that the source cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullSource() {
    new ResolveResponse((ResolveResponse) null);
  }

  /**
   * Tests that there must be a specification in the message.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoSpecificationInMessage() {
    new ResolveResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), new FudgeSerializer(OpenGammaFudgeContext.getInstance()).newMessage());
  }

  /**
   * Tests that the specification message must be the correct type.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongTypeInMessage() {
    final MutableFudgeMsg msg = new FudgeSerializer(OpenGammaFudgeContext.getInstance()).newMessage();
    msg.add(ResolveResponse.RESOLVED_SPECIFICATION_KEY, "value");
    new ResolveResponse(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
  }

  /**
   * Tests the copy constructor.
   */
  public void testCopyConstructor() {
    final ResolveResponse response = new ResolveResponse(RESOLVED);
    final ResolveResponse copied = new ResolveResponse(response);
    assertEquals(response.getResolvedSpecification(), copied.getResolvedSpecification());
  }

  /**
   * Tests the clone.
   */
  public void testClone() {
    final ResolveResponse response = new ResolveResponse(RESOLVED);
    final ResolveResponse clone = response.clone();
    assertEquals(response.getResolvedSpecification(), clone.getResolvedSpecification());
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    final ResolveResponse response = new ResolveResponse(RESOLVED);
    final ResolveResponse cycled = deserializer.fudgeMsgToObject(ResolveResponse.class, serializer.objectToFudgeMsg(response));
    assertEquals(response.getResolvedSpecification(), cycled.getResolvedSpecification());
  }
}
