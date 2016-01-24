/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link PortfolioKey}.
 */
@Test(groups = TestGroup.UNIT)
public class PortfolioKeyTest {

  /**
   * Tests key construction.
   */
  @Test
  public void testOf() {
    final PortfolioKey key = PortfolioKey.of("TEST");
    Assert.assertNotNull(key);
    Assert.assertEquals(key.getName(), "TEST");
    Assert.assertNull(key.getUniqueId());
  }

  /**
   * Tests the behaviour when the name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfNull() {
    PortfolioKey.of(null);
  }

  /**
   * Tests creation of a key.
   */
  @Test
  public void testOf1() {
    final PortfolioKey key = PortfolioKey.of("TEST", UniqueId.of("A", "B"));
    Assert.assertNotNull(key);
    Assert.assertEquals(key.getName(), "TEST");
    Assert.assertEquals(key.getUniqueId(), UniqueId.of("A", "B"));
  }

  /**
   * Tests that a null unique id can be supplied when constructing a key.
   */
  @Test
  public void testOf1Null() {
    PortfolioKey.of("OK", null);
  }

  /**
   * Tests that the name is not null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOf1Null1() {
    PortfolioKey.of(null, null);
  }

  /**
   * Tests getting the unique id.
   */
  @Test
  public void testGetUniqueId() {
    Assert.assertEquals(PortfolioKey.of("Hello", UniqueId.of("A", "B")).getUniqueId(), UniqueId.of("A", "B"));
  }

  /**
   * Tests that the unique id check is correct.
   */
  @Test
  public void testHasUniqueId() {
    Assert.assertTrue(PortfolioKey.of("Hello", UniqueId.of("A", "B")).hasUniqueId());
    Assert.assertFalse(PortfolioKey.of("Hello").hasUniqueId());
  }

  /**
   * Tests getting the name.
   */
  @Test
  public void testGetName() {
    Assert.assertEquals(PortfolioKey.of("Hello", UniqueId.of("A", "B")).getName(), "Hello");
  }

  /**
   * Tests the hashcode.
   */
  @Test
  public void testHashCode() {
    Assert.assertEquals(PortfolioKey.of("Hello").hashCode(), PortfolioKey.of("Hello").hashCode());
    Assert.assertEquals(PortfolioKey.of("Goodbye").hashCode(), PortfolioKey.of("Goodbye").hashCode());
    Assert.assertEquals(PortfolioKey.of("Goodbye", UniqueId.of("A", "B")).hashCode(), PortfolioKey.of("Goodbye").hashCode());
    Assert.assertEquals(PortfolioKey.of("Goodbye", UniqueId.of("A", "B")).hashCode(), PortfolioKey.of("Goodbye", UniqueId.of("A", "B")).hashCode());
    Assert.assertEquals(PortfolioKey.of("Goodbye", UniqueId.of("A", "B")).hashCode(), PortfolioKey.of("Goodbye", UniqueId.of("A", "C")).hashCode());
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEquals() {
    final PortfolioKey key = PortfolioKey.of("Hello");
    Assert.assertEquals(key, key);
    Assert.assertEquals(key, PortfolioKey.of("Hello"));
    Assert.assertNotEquals(null, key);
    Assert.assertNotEquals(new Object(), key);
    Assert.assertEquals(PortfolioKey.of("Goodbye"), PortfolioKey.of("Goodbye"));
    Assert.assertNotEquals(key, PortfolioKey.of("Goodbye"));
    Assert.assertEquals(PortfolioKey.of("Goodbye", UniqueId.of("A", "B")), PortfolioKey.of("Goodbye"));
    Assert.assertEquals(PortfolioKey.of("Goodbye", UniqueId.of("A", "B")), PortfolioKey.of("Goodbye", UniqueId.of("A", "B")));
    Assert.assertEquals(PortfolioKey.of("Goodbye", UniqueId.of("A", "B")), PortfolioKey.of("Goodbye", UniqueId.of("A", "C")));
    Assert.assertNotEquals(PortfolioKey.of("Hello", UniqueId.of("A", "B")), PortfolioKey.of("Goodbye", UniqueId.of("A", "B")));
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    Assert.assertEquals("PortfolioKey[Goodbye(A~B)]", PortfolioKey.of("Goodbye", UniqueId.of("A", "B")).toString());
    Assert.assertEquals("PortfolioKey[Goodbye]", PortfolioKey.of("Goodbye").toString());
  }
}