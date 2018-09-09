/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.link;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ResolvableSnapshotLink}.
 */
@SuppressWarnings("unchecked")
@Test(groups = TestGroup.UNIT)
public class ResolvableSnapshotLinkTest extends AbstractFudgeBuilderTestCase {
  private static final String NAME_1 = "S 1";
  private static final String NAME_2 = "S 2";
  private static final StructuredMarketDataSnapshot SNAPSHOT_1 = Mockito.mock(StructuredMarketDataSnapshot.class);
  private static final StructuredMarketDataSnapshot SNAPSHOT_2 = Mockito.mock(StructuredMarketDataSnapshot.class);
  private static final MarketDataSnapshotSource SNAPSHOT_SOURCE = Mockito.mock(MarketDataSnapshotSource.class);
  private static final ServiceContext CONTEXT;
  static {
    Mockito.when(SNAPSHOT_SOURCE.getSingle(StructuredMarketDataSnapshot.class, NAME_1, null)).thenReturn(SNAPSHOT_1);
    Mockito.when(SNAPSHOT_SOURCE.getSingle(StructuredMarketDataSnapshot.class, NAME_2, null)).thenReturn(SNAPSHOT_2);
    Mockito.when(SNAPSHOT_SOURCE.getSingle(StructuredMarketDataSnapshot.class, "S 3", null)).thenThrow(DataNotFoundException.class);
    final Map<Class<?>, Object> services = new HashMap<>();
    services.put(VersionCorrectionProvider.class, Mockito.mock(VersionCorrectionProvider.class));
    services.put(MarketDataSnapshotSource.class, SNAPSHOT_SOURCE);
    CONTEXT = ServiceContext.of(services);
  }
  private static final LinkResolver<String, StructuredMarketDataSnapshot> LINK_RESOLVER = new ServiceContextSnapshotLinkResolver<>(CONTEXT);
  private static final ResolvableSnapshotLink<StructuredMarketDataSnapshot> RESOLVER =
      new ResolvableSnapshotLink<>(NAME_1, StructuredMarketDataSnapshot.class, LINK_RESOLVER);

  /**
   * Tests that the identifiers cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdentifiers() {
    new ResolvableSnapshotLink<>(null, StructuredMarketDataSnapshot.class, LINK_RESOLVER);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType() {
    new ResolvableSnapshotLink<>(NAME_1, null, LINK_RESOLVER);
  }

  /**
   * Tests that the link resolver cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullResolver() {
    new ResolvableSnapshotLink<>(NAME_1, StructuredMarketDataSnapshot.class, null);
  }

  /**
   * Tests the type of the link identifier.
   */
  @Test
  public void testGetTargetType() {
    assertEquals(RESOLVER.getTargetType(), StructuredMarketDataSnapshot.class);
  }

  /**
   * Tests the resolution of the snapshot.
   */
  @Test
  public void testResolve() {
    ThreadLocalServiceContext.init(CONTEXT);
    assertEquals(RESOLVER.resolve(), SNAPSHOT_1);
    assertEquals(SnapshotLink.resolvable(NAME_1, StructuredMarketDataSnapshot.class).resolve(), SNAPSHOT_1);
    assertEquals(SnapshotLink.resolvable(NAME_2, StructuredMarketDataSnapshot.class).resolve(), SNAPSHOT_2);
    assertEquals(SnapshotLink.resolvable(NAME_1, StructuredMarketDataSnapshot.class, CONTEXT).resolve(), SNAPSHOT_1);
    assertEquals(SnapshotLink.resolvable(NAME_2, StructuredMarketDataSnapshot.class, CONTEXT).resolve(), SNAPSHOT_2);
  }

  /**
   * Tests the snapshot link that is returned.
   */
  @Test
  public void testSnapshotLink() {
    final SnapshotLink<StructuredMarketDataSnapshot> resolved = SnapshotLink.resolved(SNAPSHOT_1);
    assertTrue(StructuredMarketDataSnapshot.class.isAssignableFrom(resolved.getTargetType()));
  }

  /**
   * Tests the behaviour when the name does not resolve to a snapshot.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testNameCannotResolve() {
    final ResolvableSnapshotLink<StructuredMarketDataSnapshot> resolver =
        new ResolvableSnapshotLink<>("S 3", StructuredMarketDataSnapshot.class, LINK_RESOLVER);
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
    ResolvableSnapshotLink<? extends NamedSnapshot> other = new ResolvableSnapshotLink<>(NAME_1, StructuredMarketDataSnapshot.class, LINK_RESOLVER);
    assertEquals(other, RESOLVER);
    assertEquals(other.hashCode(), RESOLVER.hashCode());
    other = new ResolvableSnapshotLink<>(NAME_2, StructuredMarketDataSnapshot.class, LINK_RESOLVER);
    assertNotEquals(other, RESOLVER);
  }

  /**
   * Tests an encoding / decoding cycle.
   */
  @Test
  public void testCycle() {
    assertEquals(cycleObjectJodaXml(ResolvableSnapshotLink.class, RESOLVER), RESOLVER);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(RESOLVER.metaBean());
    assertNotNull(RESOLVER.metaBean().identifier());
    assertEquals(RESOLVER.metaBean().identifier().get(RESOLVER), LinkIdentifier.of(NAME_1, StructuredMarketDataSnapshot.class));
    assertEquals(RESOLVER.property("identifier").get(), LinkIdentifier.of(NAME_1, StructuredMarketDataSnapshot.class));
    try {
      assertNull(RESOLVER.property("resolver").get());
      fail();
    } catch (final NoSuchElementException e) {
      // expected
    }
  }
}
