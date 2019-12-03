/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link VolatilityCubeKey}.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilityCubeKeyTest extends AbstractFudgeBuilderTestCase {
  private static final String DEFINITION = "def";
  private static final String SPECIFICATION = "spec";
  private static final String QUOTE_TYPE = "delta";
  private static final String QUOTE_UNITS = "bps";
  private static final VolatilityCubeKey KEY = VolatilityCubeKey.of(DEFINITION, SPECIFICATION, QUOTE_TYPE, QUOTE_UNITS);

  /**
   * Tests the comparator with a null input.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNull() {
    KEY.compareTo(null);
  }

  /**
   * Tests the comparator.
   */
  @Test
  public void testComparator() {
    assertEquals(KEY.compareTo(KEY), 0);
    VolatilityCubeKey key = VolatilityCubeKey.of("z" + DEFINITION, SPECIFICATION, QUOTE_TYPE, QUOTE_UNITS);
    assertTrue(KEY.compareTo(key) < 0);
    assertTrue(key.compareTo(KEY) > 0);
    key = VolatilityCubeKey.of(DEFINITION, "z" + SPECIFICATION, QUOTE_TYPE, QUOTE_UNITS);
    assertTrue(KEY.compareTo(key) < 0);
    assertTrue(key.compareTo(KEY) > 0);
    key = VolatilityCubeKey.of(DEFINITION, SPECIFICATION, "z" + QUOTE_TYPE, QUOTE_UNITS);
    assertTrue(KEY.compareTo(key) < 0);
    assertTrue(key.compareTo(KEY) > 0);
    key = VolatilityCubeKey.of(DEFINITION, SPECIFICATION, QUOTE_TYPE, "z" + QUOTE_UNITS);
    assertTrue(KEY.compareTo(key) < 0);
    assertTrue(key.compareTo(KEY) > 0);
  }

  /**
   * Tests an encoding / decoding cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(VolatilityCubeKey.class, KEY);
  }

  /**
   * Tests the visitor.
   */
  @Test
  public void testVisitor() {
    assertEquals(KEY.accept(TestKeyVisitor.INSTANCE), "VolatilityCubeKey");
  }

  /**
   * Tests the hashCode method.
   */
  @Test
  public void testHashCode() {
    assertEquals(KEY.hashCode(), VolatilityCubeKey.of(DEFINITION, SPECIFICATION, QUOTE_TYPE, QUOTE_UNITS).hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final VolatilityCubeKey key = VolatilityCubeKey.of(DEFINITION, SPECIFICATION, QUOTE_TYPE, QUOTE_UNITS);
    assertEquals(key, key);
    assertNotEquals(null, key);
    assertNotEquals(key, DEFINITION);
    VolatilityCubeKey other = VolatilityCubeKey.of(DEFINITION, SPECIFICATION, QUOTE_TYPE, QUOTE_UNITS);
    assertEquals(key, other);
    other = VolatilityCubeKey.of(SPECIFICATION, SPECIFICATION, QUOTE_TYPE, QUOTE_UNITS);
    assertNotEquals(key, other);
    other  = VolatilityCubeKey.of(DEFINITION, DEFINITION, QUOTE_TYPE, QUOTE_UNITS);
    assertNotEquals(key, other);
    other  = VolatilityCubeKey.of(DEFINITION, SPECIFICATION, QUOTE_UNITS, QUOTE_UNITS);
    assertNotEquals(key, other);
    other  = VolatilityCubeKey.of(DEFINITION, SPECIFICATION, QUOTE_TYPE, QUOTE_TYPE);
    assertNotEquals(key, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(KEY.metaBean());
    assertNotNull(KEY.metaBean().definitionName());
    assertNotNull(KEY.metaBean().specificationName());
    assertNotNull(KEY.metaBean().quoteType());
    assertNotNull(KEY.metaBean().quoteUnits());
    assertEquals(KEY.metaBean().definitionName().get(KEY), DEFINITION);
    assertEquals(KEY.metaBean().specificationName().get(KEY), SPECIFICATION);
    assertEquals(KEY.metaBean().quoteType().get(KEY), QUOTE_TYPE);
    assertEquals(KEY.metaBean().quoteUnits().get(KEY), QUOTE_UNITS);
    assertEquals(KEY.property("definitionName").get(), DEFINITION);
    assertEquals(KEY.property("specificationName").get(), SPECIFICATION);
    assertEquals(KEY.property("quoteType").get(), QUOTE_TYPE);
    assertEquals(KEY.property("quoteUnits").get(), QUOTE_UNITS);
  }

}
