/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine.marketdata;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Collections;

import org.testng.annotations.Test;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.snapshot.UserMarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.AlwaysAvailableMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for {@link AlwaysAvailableMarketDataProvider}.
 */
@Test(groups = TestGroup.UNIT)
public class AlwaysAvailableMarketDataProviderTest {
  /** An always available market data provider */
  private static final MarketDataProvider PROVIDER = new AlwaysAvailableMarketDataProvider();

  /**
   * Tests this provider does not allow subscriptions.
   */
  @Test
  public void testSubscribeFails() {
    final ValueSpecification spec = new ValueSpecification(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetSpecification.of(UniqueId.of("TEST", "1")),
        ValueProperties.with(ValuePropertyNames.FUNCTION, "TEST").get());
    try {
      PROVIDER.subscribe(spec);
      fail();
    } catch (final UnsupportedOperationException e) {
      // expected
    }
    try {
      PROVIDER.subscribe(Collections.singleton(spec));
      fail();
    } catch (final UnsupportedOperationException e) {
      // expected
    }
    try {
      PROVIDER.unsubscribe(spec);
      fail();
    } catch (final UnsupportedOperationException e) {
      // expected
    }
    try {
      PROVIDER.unsubscribe(Collections.singleton(spec));
      fail();
    } catch (final UnsupportedOperationException e) {
      // expected
    }
  }

  /**
   * Tests the market data specification compatibility check.
   */
  @Test
  public void testIsCompatible() {
    assertTrue(PROVIDER.isCompatible(AlwaysAvailableMarketDataSpecification.builder().build()));
    assertFalse(PROVIDER.isCompatible(LiveMarketDataSpecification.of("SOURCE")));
  }

  /**
   * Checks that the snapshot returned is empty.
   */
  @Test
  public void testSnapshot() {
    final MarketDataSnapshot snapshot = PROVIDER.snapshot(AlwaysAvailableMarketDataSpecification.builder().build());
    assertTrue(snapshot instanceof UserMarketDataSnapshot);
    final UserMarketDataSnapshot userSnapshot = (UserMarketDataSnapshot) snapshot;
    assertFalse(userSnapshot.isInitialized());
  }
}
