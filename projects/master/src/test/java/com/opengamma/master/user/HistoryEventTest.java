/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link HistoryEvent}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoryEventTest {
  private static final HistoryEventType TYPE = HistoryEventType.ADDED;
  private static final UniqueId UID = UniqueId.of("uid", "1");
  private static final String USER_NAME = "user";
  private static final Instant INSTANT = Instant.ofEpochSecond(100L);
  private static final List<String> CHANGES = Arrays.asList("A", "B", "C");
  private static final HistoryEvent EVENT = HistoryEvent.of(TYPE, UID, USER_NAME, INSTANT, CHANGES);

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType() {
    HistoryEvent.of(null, UID, USER_NAME, INSTANT, CHANGES);
  }

  /**
   * Tests that the unique id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUid() {
    HistoryEvent.of(TYPE, null, USER_NAME, INSTANT, CHANGES);
  }

  /**
   * Tests that the user name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUserName() {
    HistoryEvent.of(TYPE, UID, null, INSTANT, CHANGES);
  }

  /**
   * Tests that the instant cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstant() {
    HistoryEvent.of(TYPE, UID, USER_NAME, null, CHANGES);
  }

  /**
   * Tests that the changes cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullChangeList() {
    HistoryEvent.of(TYPE, UID, USER_NAME, INSTANT, null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(EVENT, EVENT);
    assertNotEquals(null, EVENT);
    assertNotEquals(CHANGES, EVENT);
    assertEquals(EVENT.toString(), "HistoryEvent{type=ADDED, uniqueId=uid~1, userName=user, instant=1970-01-01T00:01:40Z, changes=[A, B, C]}");
    HistoryEvent other = HistoryEvent.of(TYPE, UID, USER_NAME, INSTANT, CHANGES);
    assertEquals(EVENT, other);
    assertEquals(EVENT.hashCode(), other.hashCode());
    other = HistoryEvent.of(HistoryEventType.CHANGED, UID, USER_NAME, INSTANT, CHANGES);
    assertNotEquals(EVENT, other);
    other = HistoryEvent.of(TYPE, UniqueId.of("uid", "2"), USER_NAME, INSTANT, CHANGES);
    assertNotEquals(EVENT, other);
    other = HistoryEvent.of(TYPE, UID, "name", INSTANT, CHANGES);
    assertNotEquals(EVENT, other);
    other = HistoryEvent.of(TYPE, UID, USER_NAME, INSTANT.plusSeconds(100), CHANGES);
    assertNotEquals(EVENT, other);
    other = HistoryEvent.of(TYPE, UID, USER_NAME, INSTANT, Collections.<String>emptyList());
    assertNotEquals(EVENT, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertEquals(EVENT.metaBean().changes().get(EVENT), CHANGES);
    assertEquals(EVENT.metaBean().instant().get(EVENT), INSTANT);
    assertEquals(EVENT.metaBean().type().get(EVENT), TYPE);
    assertEquals(EVENT.metaBean().uniqueId().get(EVENT), UID);
    assertEquals(EVENT.metaBean().userName().get(EVENT), USER_NAME);

    assertEquals(EVENT.property("changes").get(), CHANGES);
    assertEquals(EVENT.property("instant").get(), INSTANT);
    assertEquals(EVENT.property("type").get(), TYPE);
    assertEquals(EVENT.property("uniqueId").get(), UID);
    assertEquals(EVENT.property("userName").get(), USER_NAME);
  }
}
