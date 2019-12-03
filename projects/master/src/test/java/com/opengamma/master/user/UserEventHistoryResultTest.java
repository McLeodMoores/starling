/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link UserEventHistoryResult}.
 */
@Test(groups = TestGroup.UNIT)
public class UserEventHistoryResultTest {
  private static final List<HistoryEvent> EVENTS = Arrays.asList(
      HistoryEvent.of(HistoryEventType.ADDED, UniqueId.of("uid", "1"), "user", Instant.ofEpochSecond(100L), Arrays.asList("A", "B")),
      HistoryEvent.of(HistoryEventType.REMOVED, UniqueId.of("uid", "2"), "user", Instant.ofEpochSecond(1000L), Arrays.asList("C", "D")));

  /**
   * Tests that the events cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEvents1() {
    new UserEventHistoryResult(null);
  }

  /**
   * Tests that the events cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEvents2() {
    new UserEventHistoryResult().setEvents(null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final UserEventHistoryResult result = new UserEventHistoryResult(EVENTS);
    assertEquals(result, result);
    assertNotEquals(null, result);
    assertNotEquals(EVENTS, result);
    assertEquals(result.getEvents(), EVENTS);
    assertEquals(result.toString(), "UserEventHistoryResult{events=["
        + "HistoryEvent{type=ADDED, uniqueId=uid~1, userName=user, instant=1970-01-01T00:01:40Z, changes=[A, B]}, "
        + "HistoryEvent{type=REMOVED, uniqueId=uid~2, userName=user, instant=1970-01-01T00:16:40Z, changes=[C, D]}]}");
    UserEventHistoryResult other = new UserEventHistoryResult();
    other.setEvents(EVENTS);
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other = new UserEventHistoryResult(Collections.<HistoryEvent>emptyList());
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final UserEventHistoryResult result = new UserEventHistoryResult(EVENTS);
    assertNotNull(result.metaBean());
    assertEquals(result.metaBean().events().get(result), EVENTS);
    assertEquals(result.property("events").get(), EVENTS);
  }
}
