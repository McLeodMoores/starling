/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Tests for {@link SurfaceKey}.
 */
public class SurfaceKeyTest extends AbstractFudgeBuilderTestCase {
  private static final String NAME = "FOO";

  /**
   * Tests the hashCode method.
   */
  @Test
  public void testHashCode() {
    assertEquals(SurfaceKey.of(NAME).hashCode(), SurfaceKey.of(NAME).hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final SurfaceKey key = SurfaceKey.of(NAME);
    assertEquals(key, key);
    assertNotEquals(null, key);
    assertNotEquals(key, NAME);
    SurfaceKey other = SurfaceKey.of("FOO");
    assertEquals(key, other);
    other = SurfaceKey.of("BAR");
    assertNotEquals(key, other);
  }

  /**
   * Tests the compareTo method.
   */
  @Test
  public void testCompareTo() {
    final SurfaceKey key = SurfaceKey.of(NAME);
    assertEquals(key.compareTo(SurfaceKey.of(NAME)), 0);
    assertNotEquals(key.compareTo(SurfaceKey.of("z" + NAME)), 0);
    assertNotEquals(key.compareTo(SurfaceKey.of("a" + NAME)), 0);
  }

  /**
   * Tests the visitor.
   */
  @Test
  public void testVisitor() {
    final SurfaceKey key = SurfaceKey.of(NAME);
    assertEquals(key.accept(TestKeyVisitor.INSTANCE), "SurfaceKey");
  }

  /**
   * Tests a message cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(SurfaceKey.class, SurfaceKey.of(NAME));
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final SurfaceKey key = SurfaceKey.of(NAME);
    assertNotNull(key.metaBean());
    assertNotNull(key.metaBean().name());
    assertEquals(key.metaBean().name().get(key), NAME);
    assertEquals(key.property("name").get(), NAME);
  }

}
