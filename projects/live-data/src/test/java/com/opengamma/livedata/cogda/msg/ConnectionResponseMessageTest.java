/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.cogda.msg;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ConnectionResponseMessage}.
 */
@Test(groups = TestGroup.UNIT)
public class ConnectionResponseMessageTest {
  private static final ConnectionResult RESULT = ConnectionResult.NEW_CONNECTION_SUCCESS;
  private static final List<String> AVAILABLE_SERVERS = Arrays.asList("server1", "server2", "server3");
  private static final MutableFudgeMsg MSG = OpenGammaFudgeContext.getInstance().newMessage();
  static {
    MSG.add("field1", "value1");
    MSG.add("field2", "value2");
    MSG.add("field3", "value3");
  }

  /**
   * Tests that the result cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullResult() {
    new ConnectionResponseMessage().setResult(null);
  }

  /**
   * Tests that the servers cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullServers() {
    new ConnectionResponseMessage().setAvailableServers(null);
  }

  /**
   * Tests that the capabilities cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCapabilities() {
    new ConnectionResponseMessage().setCapabilities(null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ConnectionResponseMessage msg = new ConnectionResponseMessage();
    msg.setAvailableServers(AVAILABLE_SERVERS);
    msg.setCapabilities(MSG);
    msg.setResult(RESULT);
    assertEquals(msg, msg);
    assertNotEquals(null, msg);
    assertNotEquals(MSG, msg);
    assertEquals(msg.getAvailableServers(), AVAILABLE_SERVERS);
    assertEquals(msg.getCapabilities(), MSG);
    assertEquals(msg.getResult(), RESULT);
    assertEquals(msg.toString(), "ConnectionResponseMessage{result=NEW_CONNECTION_SUCCESS, "
        + "availableServers=[server1, server2, server3], capabilities=FudgeMsg[field1 => value1, field2 => value2, field3 => value3]}");
    ConnectionResponseMessage other = new ConnectionResponseMessage();
    other.setAvailableServers(AVAILABLE_SERVERS);
    other.setCapabilities(MSG);
    other.setResult(RESULT);
    assertEquals(msg, other);
    assertEquals(msg.hashCode(), other.hashCode());
    other = new ConnectionResponseMessage();
    other.setAvailableServers(AVAILABLE_SERVERS);
    other.setResult(RESULT);
    other.applyCapabilities(MSG);
    assertEquals(msg, other);
    other.setResult(ConnectionResult.NOT_AUTHORIZED);
    assertNotEquals(msg, other);
    other.setResult(RESULT);
    other.setCapabilities(OpenGammaFudgeContext.getInstance().newMessage());
    assertNotEquals(msg, other);
    other.setCapabilities(MSG);
    other.setAvailableServers(Collections.<String>emptyList());
    assertNotEquals(msg, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ConnectionResponseMessage msg = new ConnectionResponseMessage();
    msg.setAvailableServers(AVAILABLE_SERVERS);
    msg.setCapabilities(MSG);
    msg.setResult(RESULT);
    assertEquals(msg.metaBean().availableServers().get(msg), AVAILABLE_SERVERS);
    assertEquals(msg.metaBean().capabilities().get(msg), MSG);
    assertEquals(msg.metaBean().result().get(msg), RESULT);
    assertEquals(msg.property("availableServers").get(), AVAILABLE_SERVERS);
    assertEquals(msg.property("capabilities").get(), MSG);
    assertEquals(msg.property("result").get(), RESULT);
  }
}
