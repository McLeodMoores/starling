/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link SimplePortfolio}.
 */
@Test(groups = TestGroup.UNIT)
public class SimplePortfolioTest {

  /**
   * Tests construction of an empty portfolio.
   */
  public void testConstructionString() {
    final SimplePortfolio test = new SimplePortfolio("Name");
    assertEquals(null, test.getUniqueId());
    assertEquals("Name", test.getName());
    assertEquals(true, SimplePortfolioNode.class.isAssignableFrom(test.getRootNode().getClass()));
    assertEquals(0, test.getRootNode().size());
    assertEquals("Portfolio[]", test.toString());
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructionStringNull() {
    new SimplePortfolio((String) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests construction of an empty portfolio.
   */
  public void testConstructionPortfolioIdString() {
    final SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    assertEquals(id("Scheme", "Id"), test.getUniqueId());
    assertEquals("Name", test.getName());
    assertEquals(true, SimplePortfolioNode.class.isAssignableFrom(test.getRootNode().getClass()));
    assertEquals(0, test.getRootNode().size());
    assertEquals("Portfolio[Scheme~Id]", test.toString());
  }

  /**
   * Tests that the identifier cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructionPortfolioIdStringNullId() {
    new SimplePortfolio(null, "Name");
  }

  /**
   * Tests that the portfolio name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructionPortfolioIdStringNullName() {
    new SimplePortfolio(id("Scheme", "Id"), null);
  }

  //-------------------------------------------------------------------------
  /**
   * Test construction with a root node.
   */
  public void testConstructionPortfolioIdStringNode() {
    final SimplePortfolioNode root = new SimplePortfolioNode();
    final SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name", root);
    assertEquals(id("Scheme", "Id"), test.getUniqueId());
    assertEquals("Name", test.getName());
    assertEquals(true, test.getRootNode() == root);
    assertEquals("Portfolio[Scheme~Id]", test.toString());
  }

  /**
   * Tests that the id cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructionPortfolioIdStringNodeNullId() {
    new SimplePortfolio(null, "Name", new SimplePortfolioNode());
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructionPortfolioIdStringNodeNullName() {
    new SimplePortfolio(id("Scheme", "Id"), null, new SimplePortfolioNode());
  }

  /**
   * Tests that the node cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructionPortfolioIdStringNodeNullRoot() {
    new SimplePortfolio(id("Scheme", "Id"), "Name", null);
  }

  private static UniqueId id(final String scheme, final String value) {
    return UniqueId.of(scheme, value);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the unique id can be set.
   */
  public void testSetUniqueId() {
    final SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    test.setUniqueId(id("Scheme2", "Id2"));
    assertEquals(id("Scheme2", "Id2"), test.getUniqueId());
  }

  /**
   * Tests that the unique id cannot be nul.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetUniqueIdNull() {
    final SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    test.setUniqueId(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the name can be set.
   */
  public void testSetName() {
    final SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    test.setName("Name2");
    assertEquals("Name2", test.getName());
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetNameNull() {
    final SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    test.setName(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the root node can be set.
   */
  public void testSetRootNode() {
    final SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    final SimplePortfolioNode root = new SimplePortfolioNode();
    test.setRootNode(root);
    assertSame(root, test.getRootNode());
  }

  /**
   * Tests that the root node cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetRootNodeNull() {
    final SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    test.setRootNode(null);
  }

}
