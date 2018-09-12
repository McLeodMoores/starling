/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.legalentity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link Capability}.
 */
@Test(groups = TestGroup.UNIT)
public class CapabilityTest extends AbstractFudgeBuilderTestCase {
  private static final String NAME = "NAME";

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final Capability capability = new Capability(NAME);
    assertEquals(capability, capability);
    assertNotEquals(null, capability);
    assertNotEquals(NAME, capability);
    Capability other = new Capability(NAME);
    assertEquals(other, capability);
    assertEquals(other.hashCode(), capability.hashCode());
    other = new Capability("OTHER");
    assertNotEquals(other, capability);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(Capability.class, new Capability(NAME));
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final Capability capability = new Capability(NAME);
    assertNotNull(capability.metaBean());
    assertNotNull(capability.metaBean().name());
    assertEquals(capability.metaBean().name().get(capability), NAME);
    assertEquals(capability.property("name").get(), NAME);
  }
}
