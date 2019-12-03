/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.link;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Tests for {@link ResolvedSecurityLink}.
 */
public class ResolvedSecurityLinkTest extends AbstractFudgeBuilderTestCase {
  private static final ExternalIdBundle IDS = ExternalIdBundle.of(Arrays.asList(
      ExternalId.of("eid", "1"), ExternalId.of("eid", "2"), ExternalId.of("eid", "3")));
  private static final SimpleSecurity SECURITY = new SimpleSecurity(UniqueId.of("cnv", "1"), IDS, "One", "test");
  private static final ResolvedSecurityLink<SimpleSecurity> RESOLVED = new ResolvedSecurityLink<>(SECURITY);

  /**
   * Tests that the security cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecurity() {
    new ResolvedSecurityLink<>(null);
  }

  /**
   * Tests the resolve method.
   */
  @Test
  public void testResolve() {
    assertEquals(RESOLVED.resolve(), SECURITY);
  }

  /**
   * Tests the target type.
   */
  @Test
  public void testTargetType() {
    assertEquals(RESOLVED.getTargetType(), SimpleSecurity.class);
  }

  /**
   * Tests the hashCode and equals method.
   */
  @Test
  public void testHashCodeEquals() {
    ResolvedSecurityLink<SimpleSecurity> other = new ResolvedSecurityLink<>(new SimpleSecurity(UniqueId.of("cnv", "1"), IDS, "One", "test"));
    assertEquals(RESOLVED, RESOLVED);
    assertNotEquals(null, RESOLVED);
    assertNotEquals(SECURITY, RESOLVED);
    assertEquals(RESOLVED, other);
    assertEquals(RESOLVED.hashCode(), other.hashCode());
    other = new ResolvedSecurityLink<>(new SimpleSecurity(UniqueId.of("cnv", "2"), IDS, "One", "test"));
    assertNotEquals(RESOLVED, other);
  }

  /**
   * Tests an encoding / decoding cycle.
   */
  @Test
  public void testCycle() {
    assertEquals(cycleObjectJodaXml(ResolvedSecurityLink.class, RESOLVED), RESOLVED);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(RESOLVED.metaBean());
    assertNotNull(RESOLVED.metaBean().value());
    assertEquals(RESOLVED.metaBean().value().get(RESOLVED), SECURITY);
    assertEquals(RESOLVED.property("value").get(), SECURITY);
  }

  /**
   * Tests getting the identifier.
   */
  @Test
  public void testGetIdentifier() {
    final ResolvedSecurityLink<SimpleSecurity> link = new ResolvedSecurityLink<>(new SimpleSecurity(UniqueId.of("cnv", "1"), IDS, "One", "test"));
    assertEquals(link.getIdentifier(), link.getValue().getExternalIdBundle());
  }

  /**
   * Tests getting the identifier when the security does not have one.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetIdentifierNoIdentifierOnSecurity() {
    final ResolvedSecurityLink<SimpleSecurity> link = new ResolvedSecurityLink<>(new SimpleSecurity("TYPE"));
    link.getIdentifier();
  }
}
