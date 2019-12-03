/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.link;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ResolvableSecurityLink}.
 */
@Test(groups = TestGroup.UNIT)
public class ResolvableSecurityLinkTest extends AbstractFudgeBuilderTestCase {
  private static final ExternalIdBundle IDS_1 = ExternalIdBundle.of(Arrays.asList(
      ExternalId.of("eid", "1"), ExternalId.of("eid", "2"), ExternalId.of("eid", "3")));
  private static final ExternalIdBundle IDS_2 = ExternalIdBundle.of(Arrays.asList(
      ExternalId.of("eid", "10"), ExternalId.of("eid", "20"), ExternalId.of("eid", "30")));
  private static final SimpleSecurity SECURITY_1 = new SimpleSecurity(UniqueId.of("sec", "1"), IDS_1, "One", "test");
  private static final ExternalIdBundle IDS_3 = ExternalIdBundle.of("eid", "100");
  private static final SimpleSecurity SECURITY_2 = new SimpleSecurity(UniqueId.of("cnv", "2"), IDS_2, "Two", "test");
  private static final SecuritySource SECURITY_SOURCE = Mockito.mock(SecuritySource.class);
  private static final ServiceContext CONTEXT;
  static {
    SECURITY_1.setExternalIdBundle(IDS_1);
    SECURITY_2.setExternalIdBundle(IDS_2);
    Mockito.when(SECURITY_SOURCE.getSingle(IDS_1, (VersionCorrection) null)).thenReturn(SECURITY_1);
    Mockito.when(SECURITY_SOURCE.getSingle(IDS_2, (VersionCorrection) null)).thenReturn(SECURITY_2);
    Mockito.when(SECURITY_SOURCE.getSingle(ExternalId.of("eid", "1").toBundle(), (VersionCorrection) null)).thenReturn(SECURITY_1);
    Mockito.when(SECURITY_SOURCE.getSingle(ExternalId.of("eid", "10").toBundle(), (VersionCorrection) null)).thenReturn(SECURITY_2);
    //Mockito.when(SECURITY_SOURCE.getSingle(IDS_3, (VersionCorrection) null)).thenThrow(DataNotFoundException.class);
    final Map<Class<?>, Object> services = new HashMap<>();
    services.put(VersionCorrectionProvider.class, Mockito.mock(VersionCorrectionProvider.class));
    services.put(SecuritySource.class, SECURITY_SOURCE);
    CONTEXT = ServiceContext.of(services);
  }
  private static final LinkResolver<ExternalIdBundle, SimpleSecurity> LINK_RESOLVER = new ServiceContextSecurityLinkResolver<>(CONTEXT);
  private static final ResolvableSecurityLink<SimpleSecurity> RESOLVER = new ResolvableSecurityLink<>(IDS_1, SimpleSecurity.class, LINK_RESOLVER);

  /**
   * Tests that the identifiers cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdentifiers() {
    new ResolvableSecurityLink<>(null, SimpleSecurity.class, LINK_RESOLVER);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType() {
    new ResolvableSecurityLink<>(IDS_1, null, LINK_RESOLVER);
  }

  /**
   * Tests that the link resolver cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullResolver() {
    new ResolvableSecurityLink<>(IDS_1, SimpleSecurity.class, null);
  }

  /**
   * Tests the type of the link identifier.
   */
  @Test
  public void testGetTargetType() {
    assertEquals(RESOLVER.getTargetType(), SimpleSecurity.class);
  }

  /**
   * Tests the resolution of the security.
   */
  @Test
  public void testResolve() {
    ThreadLocalServiceContext.init(CONTEXT);
    assertEquals(RESOLVER.resolve(), SECURITY_1);
    assertEquals(SecurityLink.resolvable(ExternalId.of("eid", "1")).resolve(), SECURITY_1);
    assertEquals(SecurityLink.resolvable(ExternalId.of("eid", "10")).resolve(), SECURITY_2);
    assertEquals(SecurityLink.resolvable(ExternalId.of("eid", "1"), SimpleSecurity.class).resolve(), SECURITY_1);
    assertEquals(SecurityLink.resolvable(ExternalId.of("eid", "10"), SimpleSecurity.class).resolve(), SECURITY_2);
    assertEquals(SecurityLink.resolvable(ExternalId.of("eid", "1"), SimpleSecurity.class, CONTEXT).resolve(), SECURITY_1);
    assertEquals(SecurityLink.resolvable(ExternalId.of("eid", "10"), SimpleSecurity.class, CONTEXT).resolve(), SECURITY_2);
    assertEquals(SecurityLink.resolvable(IDS_1).resolve(), SECURITY_1);
    assertEquals(SecurityLink.resolvable(IDS_2).resolve(), SECURITY_2);
    assertEquals(SecurityLink.resolvable(IDS_1, SimpleSecurity.class).resolve(), SECURITY_1);
    assertEquals(SecurityLink.resolvable(IDS_2, SimpleSecurity.class).resolve(), SECURITY_2);
    assertEquals(SecurityLink.resolvable(IDS_1, SimpleSecurity.class, CONTEXT).resolve(), SECURITY_1);
    assertEquals(SecurityLink.resolvable(IDS_2, SimpleSecurity.class, CONTEXT).resolve(), SECURITY_2);
  }

  /**
   * Tests the security link that is returned.
   */
  @Test
  public void testSecurityLink() {
    final SecurityLink<SimpleSecurity> resolved = SecurityLink.resolved(SECURITY_1);
    assertEquals(resolved.getTargetType(), SimpleSecurity.class);
  }

  /**
   * Tests the behaviour when the ids do not resolve to a security.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testIdsCannotResolve() {
    final ResolvableSecurityLink<SimpleSecurity> resolver = new ResolvableSecurityLink<>(IDS_3, SimpleSecurity.class, LINK_RESOLVER);
    resolver.resolve();
  }

  /**
   * Tests hashCode and equals.
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(RESOLVER, RESOLVER);
    assertNotEquals(null, RESOLVER);
    assertNotEquals(LINK_RESOLVER, RESOLVER);
    ResolvableSecurityLink<? extends Security> other = new ResolvableSecurityLink<>(IDS_1, SimpleSecurity.class, LINK_RESOLVER);
    assertEquals(other, RESOLVER);
    assertEquals(other.hashCode(), RESOLVER.hashCode());
    other = new ResolvableSecurityLink<>(IDS_2, SimpleSecurity.class, LINK_RESOLVER);
    assertNotEquals(other, RESOLVER);
    final LinkResolver<ExternalIdBundle, Security> link = new ServiceContextSecurityLinkResolver<>(CONTEXT);
    assertNotEquals(new ResolvableSecurityLink<>(IDS_1, Security.class, link), RESOLVER);
  }

  /**
   * Tests an encoding / decoding cycle.
   */
  @Test
  public void testCycle() {
    assertEquals(cycleObjectJodaXml(ResolvableSecurityLink.class, RESOLVER), RESOLVER);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(RESOLVER.metaBean());
    assertNotNull(RESOLVER.metaBean().linkIdentifier());
    assertEquals(RESOLVER.metaBean().linkIdentifier().get(RESOLVER), LinkIdentifier.of(IDS_1, SimpleSecurity.class));
    assertEquals(RESOLVER.property("linkIdentifier").get(), LinkIdentifier.of(IDS_1, SimpleSecurity.class));
    try {
      assertNull(RESOLVER.property("resolver").get());
      fail();
    } catch (final NoSuchElementException e) {
      // expected
    }
  }

}
