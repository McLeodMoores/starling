/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.random;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.RandomizingMarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;

/**
 * TODO document the slightly awkward design because of the requirements: provider needs to know what's been updated so it can notify the listeners only the
 * snapshot knows the IDs of the data in the underlying snapshot.
 */
/* package */ class RandomizingMarketDataProvider extends AbstractMarketDataProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(RandomizingMarketDataProvider.class);

  private static final Timer TIMER = new Timer();

  private final MarketDataProvider _underlying;
  private final RandomizingMarketDataSpecification _marketDataSpec;
  // TODO does this need to be a class map? will it ever have to deal with subtyping?
  private final Map<Class<?>, Randomizer<?>> _randomizers = ImmutableMap.of(
      Double.class, new DoubleRandomizer(),
      SnapshotDataBundle.class, new SnapshotDataBundleRandomizer());

  /** Lock used to protect the two value maps. */
  private final Object _valuesLock = new Object();
  /** Values from the underlying provider, repopulated each time a snapshot is queried. */
  private final Map<ValueSpecification, Object> _values = Maps.newHashMap();
  /** Randomized values derived from {@link #_values}, repopulated each time the randomized task executes. */
  private final Map<ValueSpecification, Object> _randomizedValues = Maps.newHashMap();

  RandomizingMarketDataProvider(final RandomizingMarketDataSpecification spec, final MarketDataProvider underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(spec, "spec");
    _marketDataSpec = spec;
    _underlying = underlying;
    _underlying.addListener(new Listener());
    scheduleRandomizingTask();
  }

  @Override
  public void addListener(final MarketDataListener listener) {
    _underlying.addListener(listener);
  }

  @Override
  public void removeListener(final MarketDataListener listener) {
    _underlying.removeListener(listener);
  }

  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    _underlying.subscribe(valueSpecification);
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    _underlying.subscribe(valueSpecifications);
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    _underlying.unsubscribe(valueSpecification);
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    _underlying.unsubscribe(valueSpecifications);
  }

  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider(final MarketDataSpecification marketDataSpec) {
    return _underlying.getAvailabilityProvider(marketDataSpec);
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _underlying.getPermissionProvider();
  }

  @Override
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    return _marketDataSpec.equals(marketDataSpec);
  }

  // TODO document assumption - this is called once with all market data and the single arg version isn't used
  @Override
  public RandomizingMarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof RandomizingMarketDataSpecification)) {
      throw new IllegalArgumentException("Expected RandomizingMarketDataSpecification, got " + marketDataSpec);
    }
    final RandomizingMarketDataSpecification randomizingSpec = (RandomizingMarketDataSpecification) marketDataSpec;
    final MarketDataSnapshot underlyingSnapshot = _underlying.snapshot(randomizingSpec.getUnderlying());
    return new RandomizingMarketDataSnapshot(underlyingSnapshot);
  }

  @Override
  public Duration getRealTimeDuration(final Instant fromInstant, final Instant toInstant) {
    return _underlying.getRealTimeDuration(fromInstant, toInstant);
  }

  /**
   * @param value
   *          The value to randomize
   * @return The randomized object or null if its value wasn't changed
   */
  private Object randomize(final Object value) {
    @SuppressWarnings("unchecked")
    final Randomizer<Object> randomizer = (Randomizer<Object>) _randomizers.get(value.getClass());
    if (randomizer == null) {
      return null;
    }
    return randomizer.randomize(value);
  }

  /**
   * Schedules a new {@link RandomizingTask} to run after a delay of {@link RandomizingMarketDataSpecification#getAverageCycleInterval()} +/-50%
   */
  private void scheduleRandomizingTask() {
    TIMER.schedule(new RandomizingTask(), (long) (_marketDataSpec.getAverageCycleInterval() * (0.5 + Math.random())));
  }

  /**
   * Populates a map of randomized values by traversing the previous set of snapshot values and randomly permuting a random subset.
   */
  private void randomizeSnapshot() {
    final Set<ValueSpecification> updatedSpecs = Sets.newHashSet();
    synchronized (_valuesLock) {
      LOGGER.debug("Randomizing snapshot");
      _randomizedValues.clear();
      for (final Map.Entry<ValueSpecification, Object> entry : _values.entrySet()) {
        final ValueSpecification spec = entry.getKey();
        final Object value = entry.getValue();
        final Object randomizedValue = randomize(value);
        if (randomizedValue != null) {
          _randomizedValues.put(spec, randomizedValue);
          LOGGER.debug("Created random value {} for spec {}", randomizedValue, spec);
          updatedSpecs.add(spec);
        }
      }
    }
    valuesChanged(updatedSpecs);
    LOGGER.debug("Notified listeners of updates to specs {}", updatedSpecs);
    scheduleRandomizingTask();
  }

  private double randomizeDouble(final double value) {
    final double signum = Math.random() < 0.5 ? -1 : 1; // TODO can I get rid of the extra call to Math.random()?
    return value * (1 + signum * Math.random() * (double) _marketDataSpec.getMaxPercentageChange() / 100d);
  }

  private class RandomizingMarketDataSnapshot implements MarketDataSnapshot {

    private final MarketDataSnapshot _underlying;

    private volatile Instant _snapshotTime;

    /* package */ RandomizingMarketDataSnapshot(final MarketDataSnapshot underlying) {
      ArgumentChecker.notNull(underlying, "underlying");
      _underlying = underlying;
    }

    @Override
    public UniqueId getUniqueId() {
      // TODO this is nasty, see PLAT-4292
      return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "RandomizingMarketDataSnapshot:" + getSnapshotTime());
    }

    @Override
    public Instant getSnapshotTimeIndication() {
      return _snapshotTime == null ? OpenGammaClock.getInstance().instant() : _snapshotTime;
    }

    @Override
    public void init() {
      _underlying.init();
      _snapshotTime = OpenGammaClock.getInstance().instant();
    }

    @Override
    public void init(final Set<ValueSpecification> values, final long timeout, final TimeUnit unit) {
      _underlying.init(values, timeout, unit);
      _snapshotTime = OpenGammaClock.getInstance().instant();
    }

    @Override
    public boolean isInitialized() {
      return _underlying.isInitialized();
    }

    @Override
    public boolean isEmpty() {
      return _underlying.isEmpty();
    }

    @Override
    public Instant getSnapshotTime() {
      return _snapshotTime;
    }

    @Override
    public Object query(final ValueSpecification specification) {
      throw new UnsupportedOperationException("This method is never used and not supported");
    }

    @Override
    public Map<ValueSpecification, Object> query(final Set<ValueSpecification> specifications) {
      Map<ValueSpecification, Object> values;
      synchronized (_valuesLock) {
        final Map<ValueSpecification, Object> underlyingValues = _underlying.query(specifications);
        values = Maps.newHashMap();
        _values.clear();
        for (final Map.Entry<ValueSpecification, Object> entry : underlyingValues.entrySet()) {
          // store the underlying values so the randomizing task can use them in its next cycle
          final ValueSpecification spec = entry.getKey();
          final Object underlyingValue = entry.getValue();
          _values.put(spec, underlyingValue);
          Object value;
          // if there is a randomized value for a spec use that instead of the underlying value
          if (_randomizedValues.containsKey(spec)) {
            value = _randomizedValues.get(spec);
            LOGGER.debug("Using randomized value {} for spec {}", value, spec);
          } else {
            value = underlyingValue;
          }
          values.put(spec, value);
        }
      }
      return values;
    }
  }

  /**
   * Listener that receives notifications from the underlying provider and forwards them to listeners attached to this provider.
   */
  private class Listener implements MarketDataListener {

    @Override
    public void subscriptionsSucceeded(final Collection<ValueSpecification> specifications) {
      RandomizingMarketDataProvider.this.subscriptionsSucceeded(specifications);
    }

    @Override
    public void subscriptionFailed(final ValueSpecification specification, final String msg) {
      RandomizingMarketDataProvider.this.subscriptionFailed(specification, msg);
    }

    @Override
    public void subscriptionStopped(final ValueSpecification specification) {
      RandomizingMarketDataProvider.this.subscriptionStopped(specification);
    }

    @Override
    public void valuesChanged(final Collection<ValueSpecification> specifications) {
      RandomizingMarketDataProvider.this.valuesChanged(specifications);
    }
  }

  /**
   * Task to asynchronously invoke {@link #randomizeSnapshot()}.
   */
  private class RandomizingTask extends TimerTask {

    @Override
    public void run() {
      randomizeSnapshot();
    }
  }

  private interface Randomizer<T> {

    /**
     * Possibly randomizes a value, returning the value if it was randomized, null if not
     * 
     * @param value
     *          The value to randomize
     * @return The randomized value, null if it wasn't randomized
     */
    T randomize(T value);
  }

  /**
   * Randomizes a double value.
   */
  private class DoubleRandomizer implements Randomizer<Double> {

    @Override
    public Double randomize(final Double value) {
      if (Math.random() > _marketDataSpec.getUpdateProbability()) {
        return null;
      }
      final double signum = Math.random() < 0.5 ? -1 : 1;
      return value * (1 + signum * Math.random() * (double) _marketDataSpec.getMaxPercentageChange() / 100d);
    }
  }

  /**
   * Randomly chooses and permutes values in a {@link SnapshotDataBundle}.
   */
  private class SnapshotDataBundleRandomizer implements Randomizer<SnapshotDataBundle> {

    @Override
    public SnapshotDataBundle randomize(final SnapshotDataBundle value) {
      final SnapshotDataBundle randomBundle = new SnapshotDataBundle();
      boolean randomized = false;
      for (final Map.Entry<ExternalIdBundle, Double> entry : value.getDataPointSet()) {
        double newValue;
        if (Math.random() > _marketDataSpec.getUpdateProbability()) {
          newValue = entry.getValue();
        } else {
          newValue = randomizeDouble(entry.getValue());
          randomized = true;
        }
        randomBundle.setDataPoint(entry.getKey(), newValue);
      }
      if (randomized) {
        return randomBundle;
      }
      return null;
    }
  }

  /*
   * private class VolatilitySurfaceDataRandomizer implements Randomizer<VolatilitySurfaceData<Object, Object>> {
   * 
   * @Override public VolatilitySurfaceData<Object, Object> randomize(VolatilitySurfaceData<Object, Object> value) { // TODO implement randomize() throw new
   * UnsupportedOperationException("randomize not implemented"); } }
   * 
   * private class VolatilityCubeDataRandomizer implements Randomizer<VolatilityCubeData> {
   * 
   * @Override public VolatilityCubeData randomize(VolatilityCubeData value) { // TODO implement randomize() throw new
   * UnsupportedOperationException("randomize not implemented"); } }
   */
}
