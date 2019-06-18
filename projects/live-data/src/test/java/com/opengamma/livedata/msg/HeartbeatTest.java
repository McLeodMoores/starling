/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.msg;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link Heartbeat}.
 */
@Test(groups = TestGroup.UNIT)
public class HeartbeatTest {
  private static final Collection<LiveDataSpecification> SPECS = Arrays.asList(
      new LiveDataSpecification("rules", Arrays.asList(ExternalId.of("eid", "1"), ExternalId.of("eid", "2"))),
      new LiveDataSpecification("rules", Arrays.asList(ExternalId.of("eid", "1"), ExternalId.of("eid", "2"))));

  /**
   * Tests that the specifications cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullSpecifications() {
    new Heartbeat((Collection<LiveDataSpecification>) null);
  }

  /**
   * Tests that the specifications cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptySpecifications() {
    new Heartbeat(Collections.<LiveDataSpecification> emptySet());
  }

  /**
   * Tests that the specifications cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSpecificationsWithNull() {
    new Heartbeat(Arrays.asList(SPECS.iterator().next(), null));
  }

  /**
   * Tests that the specifications cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSetNullSpecifications() {
    final Heartbeat response = new Heartbeat(SPECS);
    response.setLiveDataSpecifications((Collection<LiveDataSpecification>) null);
  }

  /**
   * Tests that the specifications cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetEmptySpecifications() {
    final Heartbeat response = new Heartbeat(SPECS);
    response.setLiveDataSpecifications(Collections.<LiveDataSpecification> emptySet());
  }

  /**
   * Tests that the specifications cannot be contain null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSetSpecificationsWithNull() {
    final Heartbeat response = new Heartbeat(SPECS);
    response.setLiveDataSpecifications(Arrays.asList(SPECS.iterator().next(), null));
  }

  /**
   * Tests that the specification cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testSetNullSpecification() {
    final Heartbeat response = new Heartbeat(SPECS);
    response.setLiveDataSpecifications((LiveDataSpecification) null);
  }

  /**
   * Tests that the specification cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testAddNullSpecification() {
    final Heartbeat response = new Heartbeat(SPECS);
    response.addLiveDataSpecifications(null);
  }

  /**
   * Tests that the source cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullSource() {
    new Heartbeat((Heartbeat) null);
  }

  /**
   * Tests that there must be a specification in the message.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoSpecificationInMessage() {
    new Heartbeat(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), new FudgeSerializer(OpenGammaFudgeContext.getInstance()).newMessage());
  }

  /**
   * Tests that the specification message must be the correct type.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongTypeInMessage() {
    final MutableFudgeMsg msg = new FudgeSerializer(OpenGammaFudgeContext.getInstance()).newMessage();
    msg.add(Heartbeat.LIVE_DATA_SPECIFICATIONS_KEY, "value");
    new Heartbeat(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg);
  }

  /**
   * Tests the copy constructor.
   */
  public void testCopyConstructor() {
    final Heartbeat response = new Heartbeat(SPECS);
    final Heartbeat copied = new Heartbeat(response);
    assertEquals(response.getLiveDataSpecifications(), copied.getLiveDataSpecifications());
  }

  /**
   * Tests the clone.
   */
  public void testClone() {
    final Heartbeat response = new Heartbeat(SPECS);
    final Heartbeat clone = response.clone();
    assertEquals(response.getLiveDataSpecifications(), clone.getLiveDataSpecifications());
  }

  /**
   * Tests adding specifications.
   */
  public void testAdd() {
    final Heartbeat response = new Heartbeat(SPECS);
    response.addLiveDataSpecifications(SPECS.iterator().next());
    final Collection<LiveDataSpecification> expected = new ArrayList<>(SPECS);
    expected.add(SPECS.iterator().next());
    assertEquals(response.getLiveDataSpecifications(), expected);
  }

  /**
   * Tests a cycle.
   */
  public void testCycle() {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    final Heartbeat response = new Heartbeat(SPECS);
    final Heartbeat cycled = deserializer.fudgeMsgToObject(Heartbeat.class, serializer.objectToFudgeMsg(response));
    assertEquals(response.getLiveDataSpecifications(), cycled.getLiveDataSpecifications());
  }
}
