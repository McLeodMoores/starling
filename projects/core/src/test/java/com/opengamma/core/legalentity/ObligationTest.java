/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.legalentity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link Obligation}.
 */
@Test(groups = TestGroup.UNIT)
public class ObligationTest extends AbstractFudgeBuilderTestCase {
  private static final String NAME = "BOND";
  private static final ExternalIdBundle ID = ExternalIdBundle.of("eid", "1");

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final Obligation obligation = new Obligation();
    obligation.setName(NAME);
    obligation.setSecurity(ID);
    assertEquals(obligation, obligation);
    assertNotEquals(null, obligation);
    assertNotEquals(ID, obligation);
    final Obligation other = new Obligation();
    other.setName(NAME);
    other.setSecurity(ID);
    assertEquals(obligation, other);
    assertEquals(obligation.hashCode(), other.hashCode());
    other.setName("COMP");
    assertNotEquals(obligation, other);
    other.setName(NAME);
    other.setSecurity(ExternalIdBundle.of("eid", "2"));
    assertNotEquals(obligation, other);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final Obligation obligation = new Obligation();
    obligation.setName(NAME);
    obligation.setSecurity(ID);
    assertEncodeDecodeCycle(Obligation.class, obligation);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final Obligation obligation = new Obligation();
    obligation.setName(NAME);
    obligation.setSecurity(ID);
    assertNotNull(obligation.metaBean());
    assertNotNull(obligation.metaBean().name());
    assertNotNull(obligation.metaBean().security());
    assertEquals(obligation.metaBean().name().get(obligation), NAME);
    assertEquals(obligation.metaBean().security().get(obligation), ID);
    assertEquals(obligation.property("name").get(), NAME);
    assertEquals(obligation.property("security").get(), ID);
  }

}
