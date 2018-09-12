/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.legalentity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link RootPortfolio}.
 */
@Test(groups = TestGroup.UNIT)
public class RootPortfolioTest extends AbstractFudgeBuilderTestCase {
  private static final ObjectId ID = ObjectId.of("oid", "1");

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final RootPortfolio rootPortfolio = new RootPortfolio();
    rootPortfolio.setPortfolio(ID);
    assertEquals(rootPortfolio, rootPortfolio);
    assertNotEquals(null, rootPortfolio);
    assertNotEquals(ID, rootPortfolio);
    final RootPortfolio other = new RootPortfolio();
    other.setPortfolio(ID);
    assertEquals(other, rootPortfolio);
    assertEquals(other.hashCode(), rootPortfolio.hashCode());
    other.setPortfolio(ObjectId.of("oid", "2"));
    assertNotEquals(other, rootPortfolio);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final RootPortfolio rootPortfolio = new RootPortfolio();
    rootPortfolio.setPortfolio(ID);
    assertEncodeDecodeCycle(RootPortfolio.class, rootPortfolio);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final RootPortfolio rootPortfolio = new RootPortfolio();
    rootPortfolio.setPortfolio(ID);
    assertNotNull(rootPortfolio.metaBean());
    assertNotNull(rootPortfolio.metaBean().portfolio());
    assertEquals(rootPortfolio.metaBean().portfolio().get(rootPortfolio), ID);
    assertEquals(rootPortfolio.property("portfolio").get(), ID);
  }

}
