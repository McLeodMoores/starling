/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link CurveKey}.
 */
@Test(groups = TestGroup.UNIT)
public class CurveKeyTest extends AbstractFudgeBuilderTestCase {
  private static final String NAME = "FOO";

  /**
   * Tests the hashCode method.
   */
  @Test
  public void testHashCode() {
    assertEquals(CurveKey.of(NAME).hashCode(), CurveKey.of(NAME).hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final CurveKey key = CurveKey.of(NAME);
    assertEquals(key, key);
    assertNotEquals(null, key);
    assertNotEquals(key, NAME);
    CurveKey other = CurveKey.of("FOO");
    assertEquals(key, other);
    other = CurveKey.of("BAR");
    assertNotEquals(key, other);
  }

  /**
   * Tests the compareTo method.
   */
  @Test
  public void testCompareTo() {
    final CurveKey key = CurveKey.of(NAME);
    assertEquals(key.compareTo(CurveKey.of(NAME)), 0);
    assertNotEquals(key.compareTo(CurveKey.of("z" + NAME)), 0);
    assertNotEquals(key.compareTo(CurveKey.of("a" + NAME)), 0);
  }

  /**
   * Tests the visitor.
   */
  @Test
  public void testVisitor() {
    final CurveKey key = CurveKey.of(NAME);
    assertEquals(key.accept(TestKeyVisitor.INSTANCE), "CurveKey");
  }

  /**
   * Tests a message cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(CurveKey.class, CurveKey.of(NAME));
  }

  /**
   * Tests Fudge builder.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testFudgeBuilder() {
    final CurveKey key = CurveKey.of(NAME);
    final MutableFudgeMsg msg = CurveKeyFudgeBuilder.INSTANCE.buildMessage(getFudgeSerializer(), key);
    assertEquals(key.toFudgeMsg(getFudgeSerializer()), msg);
    assertEquals(CurveKey.fromFudgeMsg(getFudgeDeserializer(), msg), key);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final CurveKey key = CurveKey.of(NAME);
    assertNotNull(key.metaBean());
    assertNotNull(key.metaBean().name());
    assertEquals(key.metaBean().name().get(key), NAME);
    assertEquals(key.property("name").get(), NAME);
  }

}
