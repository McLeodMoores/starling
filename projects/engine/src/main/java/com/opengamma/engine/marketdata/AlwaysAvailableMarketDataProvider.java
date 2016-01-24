/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Set;

import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.DefaultMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.OptimisticMarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.snapshot.UserMarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.AlwaysAvailableMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;

/**
 * MarketDataProvider that returns an availability provider that says yes to any Market_* fields.
 * This provider cannot actually provide data and is intended to be used to determine market data requirements.
 */
public class AlwaysAvailableMarketDataProvider extends AbstractMarketDataProvider {
  /** The availability provider */
  private final MarketDataAvailabilityProvider _availabilityProvider;
  /** The permissions provider */
  private final PermissiveMarketDataPermissionProvider _permissionsProvider;

  /**
   * Creates an instance.
   */
  public AlwaysAvailableMarketDataProvider() {
    super();
    final OptimisticMarketDataAvailabilityFilter filter = new OptimisticMarketDataAvailabilityFilter();
    _availabilityProvider = filter.withProvider(new DefaultMarketDataAvailabilityProvider());
    _permissionsProvider = new PermissiveMarketDataPermissionProvider();
  }

  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    throw new UnsupportedOperationException("This data provider only exists to produce a valid dependency graph build, not to provide data.");
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    throw new UnsupportedOperationException("This data provider only exists to produce a valid dependency graph build, not to provide data.");
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    throw new UnsupportedOperationException("This data provider only exists to produce a valid dependency graph build, not to provide data.");
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    throw new UnsupportedOperationException("This data provider only exists to produce a valid dependency graph build, not to provide data.");
  }

  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider(final MarketDataSpecification marketDataSpec) {
    return _availabilityProvider;
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionsProvider;
  }

  @Override
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof AlwaysAvailableMarketDataSpecification)) {
      return false;
    }
    return true;
  }

  @Override
  public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    return new UserMarketDataSnapshot(new ManageableMarketDataSnapshot());
  }

}
