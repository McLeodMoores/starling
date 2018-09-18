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
 * Tests for {@link ConnectionRequestMessage}.
 */
@Test(groups = TestGroup.UNIT)
public class ConnectionRequestMessageTest {
  private static final String USER_NAME = "user";
  private static final String PASSWORD = "Password";
  private static final MutableFudgeMsg MSG = OpenGammaFudgeContext.getInstance().newMessage();
  static {
    MSG.add("field1", "value1");
    MSG.add("field2", "value2");
    MSG.add("field3", "value3");
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new ConnectionRequestMessage().setUserName(null);
  }

  /**
   * Tests that the capabilities cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCapabilities() {
    new ConnectionRequestMessage().setCapabilities(null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final ConnectionRequestMessage msg = new ConnectionRequestMessage();
    msg.setCapabilities(MSG);
    msg.setPassword(PASSWORD);
    msg.setUserName(USER_NAME);
    assertEquals(msg, msg);
    assertNotEquals(null, msg);
    assertNotEquals(MSG, msg);
    assertEquals(msg.getCapabilities(), MSG);
    assertEquals(msg.getPassword(), PASSWORD);
    assertEquals(msg.getUserName(), USER_NAME);
    assertEquals(msg.toString(), "ConnectionRequestMessage{userName=user, password=Password, "
        + "capabilities=FudgeMsg[field1 => value1, field2 => value2, field3 => value3]}");
    ConnectionRequestMessage other = new ConnectionRequestMessage();
    other.setCapabilities(MSG);
    other.setPassword(PASSWORD);
    other.setUserName(USER_NAME);
    assertEquals(msg, other);
    assertEquals(msg.hashCode(), other.hashCode());
    other = new ConnectionRequestMessage();
    other.setPassword(PASSWORD);
    other.setUserName(USER_NAME);
    other.applyCapabilities(MSG);
    assertEquals(msg, other);
    other.setCapabilities(OpenGammaFudgeContext.getInstance().newMessage());
    assertNotEquals(msg, other);
    other.setCapabilities(MSG);
    other.setPassword(USER_NAME);
    assertNotEquals(msg, other);
    other.setPassword(PASSWORD);
    other.setUserName(PASSWORD);
    assertNotEquals(msg, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final ConnectionRequestMessage msg = new ConnectionRequestMessage();
    msg.setCapabilities(MSG);
    msg.setPassword(PASSWORD);
    msg.setUserName(USER_NAME);
    assertEquals(msg.metaBean().capabilities().get(msg), MSG);
    assertEquals(msg.metaBean().password().get(msg), PASSWORD);
    assertEquals(msg.metaBean().userName().get(msg), USER_NAME);
    assertEquals(msg.property("capabilities").get(), MSG);
    assertEquals(msg.property("password").get(), PASSWORD);
    assertEquals(msg.property("userName").get(), USER_NAME);
  }
}
