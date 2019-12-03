/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.link;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Tests for {@link ResolvedSnapshotLink}.
 */
public class ResolvedSnapshotLinkTest extends AbstractFudgeBuilderTestCase {
  private static final StructuredMarketDataSnapshot SNAPSHOT = Mockito.mock(StructuredMarketDataSnapshot.class);
  private static final ResolvedSnapshotLink<StructuredMarketDataSnapshot> RESOLVED = new ResolvedSnapshotLink<>(SNAPSHOT);

  /**
   * Tests that the snapshot cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSnapshot() {
    new ResolvedSnapshotLink<>(null);
  }

  /**
   * Tests the resolve method.
   */
  @Test
  public void testResolve() {
    assertEquals(RESOLVED.resolve(), SNAPSHOT);
  }

  /**
   * Tests the target type.
   */
  @Test
  public void testTargetType() {
    assertTrue(StructuredMarketDataSnapshot.class.isAssignableFrom(RESOLVED.getTargetType()));
  }

  /**
   * Tests the hashCode and equals method.
   */
  @Test
  public void testHashCodeEquals() {
    ResolvedSnapshotLink<StructuredMarketDataSnapshot> other = new ResolvedSnapshotLink<>(SNAPSHOT);
    assertEquals(RESOLVED, RESOLVED);
    assertNotEquals(null, RESOLVED);
    assertNotEquals(SNAPSHOT, RESOLVED);
    assertEquals(RESOLVED, other);
    assertEquals(RESOLVED.hashCode(), other.hashCode());
    other = new ResolvedSnapshotLink<>(Mockito.mock(StructuredMarketDataSnapshot.class));
    assertNotEquals(RESOLVED, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(RESOLVED.metaBean());
    assertNotNull(RESOLVED.metaBean().value());
    assertEquals(RESOLVED.metaBean().value().get(RESOLVED), SNAPSHOT);
    assertEquals(RESOLVED.property("value").get(), SNAPSHOT);
  }

}
