/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.server;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link FieldHistoryStore}.
 */
@Test(groups = TestGroup.UNIT)
public class FieldHistoryStoreTest {
  private static final MutableFudgeMsg EMPTY_MSG = OpenGammaFudgeContext.getInstance().newMessage();
  private static final MutableFudgeMsg MSG = OpenGammaFudgeContext.getInstance().newMessage();
  static {
    MSG.add("name", "security 1");
    MSG.add("exchange", "ABC");
    MSG.add("field", "price");
    MSG.add("price", "123");
    MSG.add("price", "124");
    MSG.add("price", "125");
  }

  /**
   * Tests that the message cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullMessageConstructor() {
    new FieldHistoryStore((FudgeMsg) null);
  }

  /**
   * Tests that the history store cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullHistoryStore() {
    new FieldHistoryStore((FieldHistoryStore) null);
  }

  /**
   * Tests that the message cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullMessage() {
    new FieldHistoryStore().liveDataReceived(null);
  }

  /**
   * Tests isEmpty().
   */
  public void testIsEmpty() {
    assertFalse(new FieldHistoryStore(MSG).isEmpty());
    assertTrue(new FieldHistoryStore(EMPTY_MSG).isEmpty());
    assertTrue(new FieldHistoryStore().isEmpty());
  }

  /**
   * Tests clear().
   */
  public void testClear() {
    final FieldHistoryStore store = new FieldHistoryStore(MSG);
    assertFalse(store.isEmpty());
    store.clear();
    assertTrue(store.isEmpty());
  }

  /**
   * Tests the copy constructor.
   */
  public void testCopyConstructor() {
    final FieldHistoryStore store = new FieldHistoryStore(MSG);
    final FieldHistoryStore copied = new FieldHistoryStore(store);
    assertEquals(store.getLastKnownValues(), copied.getLastKnownValues());
  }

  /**
   * Tests that only the last value of each field is stored.
   */
  public void testLastKnownValue() {
    final FieldHistoryStore store = new FieldHistoryStore(MSG);
    assertEquals(MSG.getAllByName("name").size(), 1);
    assertEquals(MSG.getAllByName("exchange").size(), 1);
    assertEquals(MSG.getAllByName("field").size(), 1);
    assertEquals(MSG.getAllByName("price").size(), 3);
    final FudgeMsg msg = store.getLastKnownValues();
    assertEquals(MSG.getAllByName("name").size(), 1);
    assertEquals(MSG.getAllByName("exchange").size(), 1);
    assertEquals(MSG.getAllByName("field").size(), 1);
    assertEquals(MSG.getAllByName("price").size(), 3);
    assertEquals(msg.getAllByName("name").size(), 1);
    assertEquals(msg.getAllByName("exchange").size(), 1);
    assertEquals(msg.getAllByName("field").size(), 1);
    assertEquals(msg.getAllByName("price").size(), 1);
  }

  /**
   * Tests the behaviour when a field does not have a name.
   */
  public void testNoNameInField() {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("name", "x");
    msg.add(1, "price");
    final FieldHistoryStore store = new FieldHistoryStore(msg);
    final FudgeMsg lastValues = store.getLastKnownValues();
    assertEquals(lastValues.getByName("name").getValue(), "x");
    assertEquals(lastValues.getByName(null).getValue(), "price");
  }
}
