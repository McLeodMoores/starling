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
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.convention.impl.MockConvention;
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
 * Tests for {@link ResolvableConventionLink}.
 */
@SuppressWarnings("unchecked")
@Test(groups = TestGroup.UNIT)
public class ResolvableConventionLinkTest extends AbstractFudgeBuilderTestCase {
  private static final ExternalIdBundle IDS_1 = ExternalIdBundle.of(Arrays.asList(
      ExternalId.of("eid", "1"), ExternalId.of("eid", "2"), ExternalId.of("eid", "3")));
  private static final ExternalIdBundle IDS_2 = ExternalIdBundle.of(Arrays.asList(
      ExternalId.of("eid", "10"), ExternalId.of("eid", "20"), ExternalId.of("eid", "30")));
  private static final MockConvention CONVENTION_1 = new MockConvention("test", UniqueId.of("cnv", "1"), "One", IDS_1);
  private static final ExternalIdBundle IDS_3 = ExternalIdBundle.of("eid", "100");
  private static final MockConvention CONVENTION_2 = new MockConvention("test", UniqueId.of("cnv", "2"), "Two", IDS_2);
  private static final ConventionSource CONVENTION_SOURCE = Mockito.mock(ConventionSource.class);
  private static final ServiceContext CONTEXT;
  static {
    CONVENTION_1.setExternalIdBundle(IDS_1);
    CONVENTION_2.setExternalIdBundle(IDS_2);
    Mockito.when(CONVENTION_SOURCE.getSingle(IDS_1, (VersionCorrection) null)).thenReturn(CONVENTION_1);
    Mockito.when(CONVENTION_SOURCE.getSingle(IDS_2, (VersionCorrection) null)).thenReturn(CONVENTION_2);
    Mockito.when(CONVENTION_SOURCE.getSingle(ExternalId.of("eid", "1").toBundle(), (VersionCorrection) null)).thenReturn(CONVENTION_1);
    Mockito.when(CONVENTION_SOURCE.getSingle(ExternalId.of("eid", "10").toBundle(), (VersionCorrection) null)).thenReturn(CONVENTION_2);
    Mockito.when(CONVENTION_SOURCE.getSingle(IDS_3, (VersionCorrection) null)).thenThrow(DataNotFoundException.class);
    final Map<Class<?>, Object> services = new HashMap<>();
    services.put(VersionCorrectionProvider.class, Mockito.mock(VersionCorrectionProvider.class));
    services.put(ConventionSource.class, CONVENTION_SOURCE);
    CONTEXT = ServiceContext.of(services);
  }
  private static final LinkResolver<ExternalIdBundle, MockConvention> LINK_RESOLVER = new ServiceContextConventionLinkResolver<>(CONTEXT);
  private static final ResolvableConventionLink<MockConvention> RESOLVER = new ResolvableConventionLink<>(IDS_1, MockConvention.class, LINK_RESOLVER);

  /**
   * Tests that the identifiers cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdentifiers() {
    new ResolvableConventionLink<>(null, MockConvention.class, LINK_RESOLVER);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType() {
    new ResolvableConventionLink<>(IDS_1, null, LINK_RESOLVER);
  }

  /**
   * Tests that the link resolver cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullResolver() {
    new ResolvableConventionLink<>(IDS_1, MockConvention.class, null);
  }

  /**
   * Tests the type of the link identifier.
   */
  @Test
  public void testGetTargetType() {
    assertEquals(RESOLVER.getTargetType(), MockConvention.class);
  }

  /**
   * Tests the resolution of the convention.
   */
  @Test
  public void testResolve() {
    ThreadLocalServiceContext.init(CONTEXT);
    assertEquals(RESOLVER.resolve(), CONVENTION_1);
    assertEquals(ConventionLink.resolvable(ExternalId.of("eid", "1")).resolve(), CONVENTION_1);
    assertEquals(ConventionLink.resolvable(ExternalId.of("eid", "10")).resolve(), CONVENTION_2);
    assertEquals(ConventionLink.resolvable(ExternalId.of("eid", "1"), MockConvention.class).resolve(), CONVENTION_1);
    assertEquals(ConventionLink.resolvable(ExternalId.of("eid", "10"), MockConvention.class).resolve(), CONVENTION_2);
    assertEquals(ConventionLink.resolvable(ExternalId.of("eid", "1"), MockConvention.class, CONTEXT).resolve(), CONVENTION_1);
    assertEquals(ConventionLink.resolvable(ExternalId.of("eid", "10"), MockConvention.class, CONTEXT).resolve(), CONVENTION_2);
    assertEquals(ConventionLink.resolvable(IDS_1).resolve(), CONVENTION_1);
    assertEquals(ConventionLink.resolvable(IDS_2).resolve(), CONVENTION_2);
    assertEquals(ConventionLink.resolvable(IDS_1, MockConvention.class).resolve(), CONVENTION_1);
    assertEquals(ConventionLink.resolvable(IDS_2, MockConvention.class).resolve(), CONVENTION_2);
    assertEquals(ConventionLink.resolvable(IDS_1, MockConvention.class, CONTEXT).resolve(), CONVENTION_1);
    assertEquals(ConventionLink.resolvable(IDS_2, MockConvention.class, CONTEXT).resolve(), CONVENTION_2);
  }

  /**
   * Tests the convention link that is returned.
   */
  @Test
  public void testConventionLink() {
    final ConventionLink<MockConvention> resolved = ConventionLink.resolved(CONVENTION_1);
    assertEquals(resolved.getTargetType(), MockConvention.class);
  }

  /**
   * Tests the behaviour when the ids do not resolve to a convention.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testIdsCannotResolve() {
    final ResolvableConventionLink<MockConvention> resolver = new ResolvableConventionLink<>(IDS_3, MockConvention.class, LINK_RESOLVER);
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
    ResolvableConventionLink<? extends Convention> other = new ResolvableConventionLink<>(IDS_1, MockConvention.class, LINK_RESOLVER);
    assertEquals(other, RESOLVER);
    assertEquals(other.hashCode(), RESOLVER.hashCode());
    other = new ResolvableConventionLink<>(IDS_2, MockConvention.class, LINK_RESOLVER);
    assertNotEquals(other, RESOLVER);
    final LinkResolver<ExternalIdBundle, Convention> link = new ServiceContextConventionLinkResolver<>(CONTEXT);
    assertNotEquals(new ResolvableConventionLink<>(IDS_1, Convention.class, link), RESOLVER);
  }

  /**
   * Tests an encoding / decoding cycle.
   */
  @Test
  public void testCycle() {
    assertEquals(cycleObjectJodaXml(ResolvableConventionLink.class, RESOLVER), RESOLVER);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(RESOLVER.metaBean());
    assertNotNull(RESOLVER.metaBean().identifier());
    assertEquals(RESOLVER.metaBean().identifier().get(RESOLVER), LinkIdentifier.of(IDS_1, MockConvention.class));
    assertEquals(RESOLVER.property("identifier").get(), LinkIdentifier.of(IDS_1, MockConvention.class));
    try {
      assertNull(RESOLVER.property("resolver").get());
      fail();
    } catch (final NoSuchElementException e) {
      // expected
    }
  }

}
