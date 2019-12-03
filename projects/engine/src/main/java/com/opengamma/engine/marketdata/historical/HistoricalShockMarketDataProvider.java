/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataNotSatisfiableException;
import com.opengamma.engine.marketdata.availability.ProviderMarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.spec.HistoricalShockMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Provider for market data derived from 3 underlying providers. This provider's values are derived by finding the difference between values in the first two
 * providers and applying it to the value from the third provider. The change applied to the base value can be the proportional or absolute difference between
 * the two other values.
 */
public class HistoricalShockMarketDataProvider extends AbstractMarketDataProvider {

  private final MarketDataProvider _historicalProvider1;
  private final MarketDataProvider _historicalProvider2;
  private final MarketDataProvider _baseProvider;
  private final MarketDataPermissionProvider _permissionProvider = new PermissionProvider();

  public HistoricalShockMarketDataProvider(final MarketDataProvider historicalProvider1,
      final MarketDataProvider historicalProvider2,
      final MarketDataProvider baseProvider) {
    ArgumentChecker.notNull(historicalProvider1, "historicalProvider1");
    ArgumentChecker.notNull(historicalProvider2, "historicalProvider2");
    ArgumentChecker.notNull(baseProvider, "baseProvider");
    _historicalProvider1 = historicalProvider1;
    _historicalProvider2 = historicalProvider2;
    _baseProvider = baseProvider;
    final Listener listener = new Listener();
    historicalProvider1.addListener(listener);
    historicalProvider2.addListener(listener);
    baseProvider.addListener(listener);
  }

  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    _historicalProvider1.subscribe(valueSpecification);
    _historicalProvider2.subscribe(valueSpecification);
    _baseProvider.subscribe(valueSpecification);
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    _historicalProvider1.subscribe(valueSpecifications);
    _historicalProvider2.subscribe(valueSpecifications);
    _baseProvider.subscribe(valueSpecifications);
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    _historicalProvider1.unsubscribe(valueSpecification);
    _historicalProvider2.unsubscribe(valueSpecification);
    _baseProvider.unsubscribe(valueSpecification);
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    _historicalProvider1.unsubscribe(valueSpecifications);
    _historicalProvider2.unsubscribe(valueSpecifications);
    _baseProvider.unsubscribe(valueSpecifications);
  }

  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider(final MarketDataSpecification marketDataSpec) {
    return new AvailabilityProvider((HistoricalShockMarketDataSpecification) marketDataSpec);
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionProvider;
  }

  /**
   * Returns true if marketDataSpec is equal to this object. This method isn't used anyway so it's academic.
   *
   * @param marketDataSpec
   *          describes the market data, not null
   * @return true if marketDataSpec is equal to this object
   */
  @Override
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    return false;
  }

  @Override
  public HistoricalShockMarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof HistoricalShockMarketDataSpecification)) {
      throw new IllegalArgumentException("Market data spec not HistoricalShockMarketDataSpecification: " + marketDataSpec);
    }
    final HistoricalShockMarketDataSpecification shockSpec = (HistoricalShockMarketDataSpecification) marketDataSpec;
    final MarketDataSnapshot snapshot1 = _historicalProvider1.snapshot(shockSpec.getHistoricalSpecification1());
    final MarketDataSnapshot snapshot2 = _historicalProvider2.snapshot(shockSpec.getHistoricalSpecification2());
    final MarketDataSnapshot baseSnapshot = _baseProvider.snapshot(shockSpec.getBaseSpecification());
    return new HistoricalShockMarketDataSnapshot(shockSpec.getShockType(), snapshot1, snapshot2, baseSnapshot);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_historicalProvider1, _historicalProvider2, _baseProvider);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final HistoricalShockMarketDataProvider other = (HistoricalShockMarketDataProvider) obj;
    return Objects.equals(this._historicalProvider1, other._historicalProvider1)
        && Objects.equals(this._historicalProvider2, other._historicalProvider2)
        && Objects.equals(this._baseProvider, other._baseProvider);
  }

  private class PermissionProvider implements MarketDataPermissionProvider {

    @Override
    public Set<ValueSpecification> checkMarketDataPermissions(final UserPrincipal user, final Set<ValueSpecification> specifications) {
      final Set<ValueSpecification> failedSpecs1 = _historicalProvider1.getPermissionProvider().checkMarketDataPermissions(user, specifications);
      final Set<ValueSpecification> failedSpecs2 = _historicalProvider2.getPermissionProvider().checkMarketDataPermissions(user, specifications);
      final Set<ValueSpecification> failedSpecs3 = _baseProvider.getPermissionProvider().checkMarketDataPermissions(user, specifications);
      // if a value fails permission checking in any of the providers then it fails
      return Sets.union(Sets.union(failedSpecs1, failedSpecs2), failedSpecs3);
    }
  }

  private final class AvailabilityProvider implements MarketDataAvailabilityProvider {

    private final HistoricalShockMarketDataSpecification _marketDataSpec;

    private AvailabilityProvider(final HistoricalShockMarketDataSpecification marketDataSpec) {
      ArgumentChecker.notNull(marketDataSpec, "marketDataSpecification");
      _marketDataSpec = marketDataSpec;
    }

    @Override
    public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec,
        final Object target,
        final ValueRequirement desiredValue) throws MarketDataNotSatisfiableException {
      final ValueSpecification spec1 = _historicalProvider1.getAvailabilityProvider(_marketDataSpec.getHistoricalSpecification1()).getAvailability(targetSpec,
          target, desiredValue);
      final ValueSpecification spec2 = _historicalProvider2.getAvailabilityProvider(_marketDataSpec.getHistoricalSpecification2()).getAvailability(targetSpec,
          target, desiredValue);
      final ValueSpecification spec3 = _baseProvider.getAvailabilityProvider(_marketDataSpec.getBaseSpecification()).getAvailability(targetSpec, target,
          desiredValue);
      if (Objects.equals(spec1, spec2) && Objects.equals(spec2, spec3)) {
        return spec1;
      }
      return null;
    }

    @Override
    public MarketDataAvailabilityFilter getAvailabilityFilter() {
      return new ProviderMarketDataAvailabilityFilter(this);
    }

    @Override
    public Serializable getAvailabilityHintKey() {
      return Triple.of(_historicalProvider1.getAvailabilityProvider(_marketDataSpec.getHistoricalSpecification1()).getAvailabilityHintKey(),
          _historicalProvider2.getAvailabilityProvider(_marketDataSpec.getHistoricalSpecification2()).getAvailabilityHintKey(),
          _baseProvider.getAvailabilityProvider(_marketDataSpec.getBaseSpecification()).getAvailabilityHintKey());
    }
  }

  private class Listener implements MarketDataListener {

    @Override
    public void subscriptionsSucceeded(final Collection<ValueSpecification> specifications) {
      HistoricalShockMarketDataProvider.this.subscriptionsSucceeded(specifications);
    }

    @Override
    public void subscriptionFailed(final ValueSpecification specification, final String msg) {
      HistoricalShockMarketDataProvider.this.subscriptionFailed(specification, msg);
    }

    @Override
    public void subscriptionStopped(final ValueSpecification specification) {
      HistoricalShockMarketDataProvider.this.subscriptionStopped(specification);
    }

    @Override
    public void valuesChanged(final Collection<ValueSpecification> specifications) {
      HistoricalShockMarketDataProvider.this.valuesChanged(specifications);
    }
  }
}
