/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.link;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.core.convention.impl.MockConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Tests for {@link ResolvedConventionLink}.
 */
public class ResolvedConventionLinkTest extends AbstractFudgeBuilderTestCase {
  private static final ExternalIdBundle IDS = ExternalIdBundle.of(Arrays.asList(
      ExternalId.of("eid", "1"), ExternalId.of("eid", "2"), ExternalId.of("eid", "3")));
  private static final MockConvention CONVENTION = new MockConvention("test", UniqueId.of("cnv", "1"), "One", IDS);
  private static final ResolvedConventionLink<MockConvention> RESOLVED = new ResolvedConventionLink<>(CONVENTION);

  /**
   * Tests that the convention cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConvention() {
    new ResolvedConventionLink<>(null);
  }

  /**
   * Tests the resolve method.
   */
  @Test
  public void testResolve() {
    assertEquals(RESOLVED.resolve(), CONVENTION);
  }

  /**
   * Tests the target type.
   */
  @Test
  public void testTargetType() {
    assertEquals(RESOLVED.getTargetType(), MockConvention.class);
  }

  /**
   * Tests the hashCode and equals method.
   */
  @Test
  public void testHashCodeEquals() {
    ResolvedConventionLink<MockConvention> other = new ResolvedConventionLink<>(new MockConvention("test", UniqueId.of("cnv", "1"), "One", IDS));
    assertEquals(RESOLVED, RESOLVED);
    assertNotEquals(null, RESOLVED);
    assertNotEquals(CONVENTION, RESOLVED);
    assertEquals(RESOLVED, other);
    assertEquals(RESOLVED.hashCode(), other.hashCode());
    other = new ResolvedConventionLink<>(new MockConvention("test", UniqueId.of("cnv", "2"), "One", IDS));
    assertNotEquals(RESOLVED, other);
  }

  /**
   * Tests an encoding / decoding cycle.
   */
  @Test
  public void testCycle() {
    assertEquals(cycleObjectJodaXml(ResolvedConventionLink.class, RESOLVED), RESOLVED);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(RESOLVED.metaBean());
    assertNotNull(RESOLVED.metaBean().convention());
    assertEquals(RESOLVED.metaBean().convention().get(RESOLVED), CONVENTION);
    assertEquals(RESOLVED.property("convention").get(), CONVENTION);
  }

}
