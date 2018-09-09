/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.link;

import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ResolvableSnapshotLink}.
 */
@Test(groups = TestGroup.UNIT)
public class ResolvableSnapshotLinkTest {
  private static final String ID = "id";
  private static final LinkResolver<String, StructuredMarketDataSnapshot> LINK_RESOLVER = new ServiceContextSnapshotLinkResolver<>();

  /**
   * Tests that the identifiers cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdentifiers() {
    new ResolvableSnapshotLink(null, StructuredMarketDataSnapshot.class, LINK_RESOLVER);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType() {
    new ResolvableSnapshotLink<>(ID, null, LINK_RESOLVER);
  }

  /**
   * Tests that the link resolver cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullResolver() {
    new ResolvableSnapshotLink<>(ID, StructuredMarketDataSnapshot.class, null);
  }

}
