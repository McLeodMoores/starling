/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.link;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.DateSet;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Tests for {@link ResolvedConfigLink}.
 */
public class ResolvedConfigLinkTest extends AbstractFudgeBuilderTestCase {
  private static final DateSet CONFIG = DateSet.of(Collections.singleton(LocalDate.of(2018, 1, 1)));
  private static final ResolvedConfigLink<DateSet> RESOLVED = new ResolvedConfigLink<>(CONFIG);

  /**
   * Tests that the config cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConfig() {
    new ResolvedConfigLink<>(null);
  }

  /**
   * Tests the resolve method.
   */
  @Test
  public void testResolve() {
    assertEquals(RESOLVED.resolve(), CONFIG);
  }

  /**
   * Tests the target type.
   */
  @Test
  public void testTargetType() {
    assertEquals(RESOLVED.getTargetType(), DateSet.class);
  }

  /**
   * Tests the hashCode and equals method.
   */
  @Test
  public void testHashCodeEquals() {
    ResolvedConfigLink<DateSet> other = new ResolvedConfigLink<>(DateSet.of(Collections.singleton(LocalDate.of(2018, 1, 1))));
    assertEquals(RESOLVED, RESOLVED);
    assertNotEquals(null, RESOLVED);
    assertNotEquals(CONFIG, RESOLVED);
    assertEquals(RESOLVED, other);
    assertEquals(RESOLVED.hashCode(), other.hashCode());
    other = new ResolvedConfigLink<>(DateSet.of(Collections.singleton(LocalDate.of(2018, 1, 12))));
    assertNotEquals(RESOLVED, other);
  }

  /**
   * Tests an encoding / decoding cycle.
   */
  @Test
  public void testCycle() {
    assertEquals(cycleObjectJodaXml(ResolvedConfigLink.class, RESOLVED), RESOLVED);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(RESOLVED.metaBean());
    assertNotNull(RESOLVED.metaBean().value());
    assertEquals(RESOLVED.metaBean().value().get(RESOLVED), CONFIG);
    assertEquals(RESOLVED.property("value").get(), CONFIG);
  }

}
