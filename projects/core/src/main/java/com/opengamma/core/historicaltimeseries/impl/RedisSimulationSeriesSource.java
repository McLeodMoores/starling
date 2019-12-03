/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * An extremely minimal and lightweight {@code HistoricalTimeSeriesSource} that pulls data
 * directly from Redis for the purpose of historical simulations, where the full market data
 * series for that simulation can change every day..
 * This is <em>only</em> appropriate for use in conjunction with {@code HistoricalTimeSeriesFunction}
 * and requires its own specific API for publishing data. It is <strong>not</strong>
 * a general purpose component.
 * <p>
 * Effectively, there is a double-time series involved:
 * <ul>
 *   <li>The {@code SimulationExecution} series is one time series, representing the date
 *       that the simulation series was performed (and/or expected to be used).</li>
 *   <li>The {@code Value} series is one point inside a particular simulation.</li>
 * </ul>
 * So, for example, assume that every day a system generates a whole new time series,
 * where that time series is the simulation points that should be run. In that case,
 * this class may be appropriate.
 * <p>
 * The following constraints must hold for this Source to be of any utility whatsoever:
 * <ul>
 *   <li>Historical lookups are not required. Because they are not supported.</li>
 *   <li>Version corrections are not required. Because they are not supported.</li>
 *   <li>Each time series has a <b>single</b> {@link ExternalId} which then acts
 *       as the {@link UniqueId} internally.</li>
 *   <li>Each external ID has a single time series (thus there is not the capacity to store
 *       different Data Source, Data Provider, Observation Time, Data Field series).</li>
 * </ul>
 * <p>
 * Where a method is not supported semantically, an {@link UnsupportedOperationException}
 * will be thrown. Where use indicates that this class may be being used incorrectly,
 * a log message will be written at {@code WARN} level.
 * <p>
 * See <a href="http://jira.opengamma.com/browse/PLAT-3385">PLAT-3385</a> for the original
 * requirement.
 */
public class RedisSimulationSeriesSource extends NonVersionedRedisHistoricalTimeSeriesSource implements SimulationSeriesSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(RedisSimulationSeriesSource.class);
  private LocalDate _currentSimulationExecutionDate = LocalDate.now();

  public RedisSimulationSeriesSource(final JedisPool jedisPool) {
    this(jedisPool, "");
  }

  public RedisSimulationSeriesSource(final JedisPool jedisPool, final String redisPrefix) {
    super(jedisPool, redisPrefix, "RedisSimulationSeriesSource");
  }

  @Override
  public RedisSimulationSeriesSource withSimulationDate(final LocalDate date) {
    final RedisSimulationSeriesSource redisSimulationSeriesSource = new RedisSimulationSeriesSource(getJedisPool(), getRedisPrefix());
    redisSimulationSeriesSource.setCurrentSimulationExecutionDate(date);
    return redisSimulationSeriesSource;
  }

  /**
   * Gets the currentSimulationExecutionDate.
   * @return the currentSimulationExecutionDate
   */
  @Override
  public LocalDate getCurrentSimulationExecutionDate() {
    return _currentSimulationExecutionDate;
  }

  /**
   * Sets the currentSimulationExecutionDate.
   * This will be used in calls to load the simulation series.
   * @param currentSimulationExecutionDate  the currentSimulationExecutionDate
   */
  public void setCurrentSimulationExecutionDate(final LocalDate currentSimulationExecutionDate) {
    _currentSimulationExecutionDate = currentSimulationExecutionDate;
  }

  // ------------------------------------------------------------------------
  // REDIS MANIPULATION OPERATIONS:
  // ------------------------------------------------------------------------

  @Override
  public void updateTimeSeriesPoint(final UniqueId uniqueId, final LocalDate simulationExecutionDate, final LocalDate valueDate, final double value) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(simulationExecutionDate, "simulationExecutionDate");
    ArgumentChecker.notNull(valueDate, "valueDate");

    final String redisKey = toRedisKey(uniqueId, simulationExecutionDate);
    updateTimeSeriesPoint(redisKey, valueDate, value);
  }

  @Override
  public void updateTimeSeries(final UniqueId uniqueId, final LocalDate simulationExecutionDate, final LocalDateDoubleTimeSeries timeseries) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(simulationExecutionDate, "simulationExecutionDate");
    ArgumentChecker.notNull(timeseries, "timeseries");

    final String redisKey = toRedisKey(uniqueId, simulationExecutionDate);
    updateTimeSeries(redisKey, timeseries, false);
  }

  @Override
  public void replaceTimeSeries(final UniqueId uniqueId, final LocalDate simulationExecutionDate, final LocalDateDoubleTimeSeries timeSeries) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(timeSeries, "timeSeries");

    final String redisKey = toRedisKey(uniqueId, simulationExecutionDate);
    updateTimeSeries(redisKey, timeSeries, true);
  }

  @Override
  public void clearExecutionDate(final LocalDate simulationExecutionDate) {
    final String keysPattern = getRedisPrefix() + "*_" + simulationExecutionDate.toString();
    final Jedis jedis = getJedisPool().getResource();
    try {
      final Set<String> keys = jedis.keys(keysPattern);
      if (!keys.isEmpty()) {
        jedis.del(keys.toArray(new String[0]));
      }
      getJedisPool().close();
    } catch (final Exception e) {
      LOGGER.error("Unable to clear execution date " + simulationExecutionDate, e);
      getJedisPool().close();
      throw new OpenGammaRuntimeException("Unable to clear execution date " + simulationExecutionDate, e);
    }
  }

  @Override
  protected String toRedisKey(final UniqueId uniqueId) {
    return toRedisKey(uniqueId, getCurrentSimulationExecutionDate());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final RedisSimulationSeriesSource that = (RedisSimulationSeriesSource) o;
    return _currentSimulationExecutionDate.equals(that._currentSimulationExecutionDate);
  }

  @Override
  public int hashCode() {
    final int result = super.hashCode();
    return 31 * result + _currentSimulationExecutionDate.hashCode();
  }
}
