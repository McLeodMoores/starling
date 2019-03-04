/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.UniqueId;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Function to classify positions by Currency.
 *
 */
public class LiquidityAggregationFunction implements AggregationFunction<String> {
  private static final double LIQUIDATE_FACTOR = 0.1;

  private final boolean _useAttributes;

  private static final String MORE_THAN_10_0 = "10+";
  private static final String FROM_3_0_TO_10_0 = "3 - 10";
  private static final String FROM_1_0_TO_3_0 = "1 - 3";
  private static final String FROM_0_5_TO_1_0 = "0.5 - 1";
  private static final String FROM_0_2_TO_0_5 = "0.2 - 0.5";
  private static final String LESS_THAN_0_2 = "< 0.2";
  private static final String NAME = "Liquidity";
  private static final String FIELD = "VOLUME";
  private static final String RESOLUTION_KEY = "DEFAULT_TSS_CONFIG";
  private static final String NO_LIQUIDITY = "N/A";

  private static final List<String> REQUIRED = Arrays.asList(MORE_THAN_10_0, FROM_3_0_TO_10_0, FROM_1_0_TO_3_0, FROM_0_5_TO_1_0, FROM_0_2_TO_0_5, LESS_THAN_0_2, NO_LIQUIDITY);

  private final HistoricalTimeSeriesSource _htsSource;
  private final SecuritySource _secSource;
  private final boolean _caching = true;
  private final Map<UniqueId, Double> _daysToLiquidateCache = new HashMap<>();
  private final Comparator<Position> _comparator = new SimplePositionComparator();

  public LiquidityAggregationFunction(final SecuritySource secSource, final HistoricalTimeSeriesSource htsSource) {
    this(secSource, htsSource, false);
  }

  public LiquidityAggregationFunction(final SecuritySource secSource, final HistoricalTimeSeriesSource htsSource, final boolean useAttributes) {
    _secSource = secSource;
    _htsSource = htsSource;
    _useAttributes = useAttributes;
  }

  @Override
  public String classifyPosition(final Position position) {
    if (_useAttributes) {
      final Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      }
      return NO_LIQUIDITY;
    }
    try {
      Security sec = position.getSecurityLink().getTarget();
      if (sec == null) {
        sec = position.getSecurityLink().resolve(_secSource); // side effect is
                                                              // it updates
                                                              // target.
      }
      final Double daysToLiquidate = getDaysToLiquidate(position);
      if (daysToLiquidate != null) {
        return classifyLiquidity(daysToLiquidate);
      }
      return NO_LIQUIDITY;
    } catch (final UnsupportedOperationException ex) {
      return NO_LIQUIDITY;
    }
  }

  private Double getDaysToLiquidate(final Position position) {
    final Security security = position.getSecurity();
    final UniqueId cacheKey = position.getUniqueId();
    if (_caching && cacheKey != null) {
      if (_daysToLiquidateCache.containsKey(cacheKey)) {
        return _daysToLiquidateCache.get(cacheKey);
      }
    }

    final Pair<LocalDate, Double> latestDataPoint = _htsSource.getLatestDataPoint(FIELD, security.getExternalIdBundle(), RESOLUTION_KEY);
    if (latestDataPoint != null) {
      final double volume = latestDataPoint.getSecond();
      final double daysToLiquidate = volume / position.getQuantity().doubleValue() * LIQUIDATE_FACTOR;

      if (_caching && cacheKey != null) {
        _daysToLiquidateCache.put(cacheKey, daysToLiquidate);
      }
      return daysToLiquidate;
    }
    if (_caching && cacheKey != null) {
      _daysToLiquidateCache.put(cacheKey, null);
    }
    return null;
  }

  private String classifyLiquidity(final Double daysToLiquidate) {
    if (daysToLiquidate != null) {
      if (daysToLiquidate < 0.2) {
        return LESS_THAN_0_2;
      } else if (daysToLiquidate < 0.5) {
        return FROM_0_2_TO_0_5;
      } else if (daysToLiquidate < 1.0) {
        return FROM_0_5_TO_1_0;
      } else if (daysToLiquidate < 3) {
        return FROM_1_0_TO_3_0;
      } else if (daysToLiquidate < 10) {
        return FROM_3_0_TO_10_0;
      } else {
        return MORE_THAN_10_0;
      }
    }
    return NO_LIQUIDITY;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return REQUIRED;
  }

  @Override
  public int compare(final String o1, final String o2) {
    return CompareUtils.compareByList(REQUIRED, o1, o2);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }
}
