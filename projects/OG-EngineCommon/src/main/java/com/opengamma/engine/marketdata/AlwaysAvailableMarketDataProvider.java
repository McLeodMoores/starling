/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Set;

import com.opengamma.engine.marketdata.availability.DefaultMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.OptimisticMarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.spec.AlwaysAvailableMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;

/**
 * MarketDataProvider that returns an availability provider that says yes to any Market_* fields.
 * It can't actually provide data and is intended to be used to determine market data requirements.
 */
public class AlwaysAvailableMarketDataProvider extends AbstractMarketDataProvider {
  private MarketDataAvailabilityProvider _availabilityProvider;
  private PermissiveMarketDataPermissionProvider _permissionsProvider;

  public AlwaysAvailableMarketDataProvider() {
    super();
    OptimisticMarketDataAvailabilityFilter filter = new OptimisticMarketDataAvailabilityFilter();
    _availabilityProvider = filter.withProvider(new DefaultMarketDataAvailabilityProvider());
    _permissionsProvider = new PermissiveMarketDataPermissionProvider();
  }

  @Override
  public void subscribe(ValueSpecification valueSpecification) {
    throw new UnsupportedOperationException("This data provider only exists to produce a valid dependency graph build, not to provide data.");
  }

  @Override
  public void subscribe(Set<ValueSpecification> valueSpecifications) {
    throw new UnsupportedOperationException("This data provider only exists to produce a valid dependency graph build, not to provide data.");
  }
  
  @Override
  public void unsubscribe(ValueSpecification valueSpecification) {
    throw new UnsupportedOperationException("This data provider only exists to produce a valid dependency graph build, not to provide data.");
  }

  @Override
  public void unsubscribe(Set<ValueSpecification> valueSpecifications) {
    throw new UnsupportedOperationException("This data provider only exists to produce a valid dependency graph build, not to provide data.");
  }

  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider(MarketDataSpecification marketDataSpec) {
    return _availabilityProvider;
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionsProvider;
  }

  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof AlwaysAvailableMarketDataSpecification)) {
      return false;
    }
    return true;
  }

  @Override
  public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
    //return new MockMarketDataSnapshot(new MockMarketDataProvider("AlwaysAvailableMarketDataProvider", true, 0));
    throw new UnsupportedOperationException("This data provider only exists to produce a valid dependency graph build, not to provide data.");
  }

}
