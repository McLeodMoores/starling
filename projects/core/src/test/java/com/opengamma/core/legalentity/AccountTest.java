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
 * Tests for {@link Account}.
 */
@Test(groups = TestGroup.UNIT)
public class AccountTest extends AbstractFudgeBuilderTestCase {
  private static final String NAME = "CORP";
  private static final ObjectId ID = ObjectId.of("oid", "1");

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final Account account = new Account();
    account.setName(NAME);
    account.setPortfolio(ID);
    assertEquals(account, account);
    assertNotEquals(null, account);
    assertNotEquals(ID, account);
    final Account other = new Account();
    other.setName(NAME);
    other.setPortfolio(ID);
    assertEquals(account, other);
    assertEquals(account.hashCode(), other.hashCode());
    other.setName("COMP");
    assertNotEquals(account, other);
    other.setName(NAME);
    other.setPortfolio(ObjectId.of("oid", "2"));
    assertNotEquals(account, other);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final Account account = new Account();
    account.setName(NAME);
    account.setPortfolio(ID);
    assertEncodeDecodeCycle(Account.class, account);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final Account account = new Account();
    account.setName(NAME);
    account.setPortfolio(ID);
    assertNotNull(account.metaBean());
    assertNotNull(account.metaBean().name());
    assertNotNull(account.metaBean().portfolio());
    assertEquals(account.metaBean().name().get(account), NAME);
    assertEquals(account.metaBean().portfolio().get(account), ID);
    assertEquals(account.property("name").get(), NAME);
    assertEquals(account.property("portfolio").get(), ID);
  }

}
