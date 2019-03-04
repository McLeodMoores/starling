/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.CompareUtils;

/**
 * Function to classify positions by Currency.
 *
 */
public class RegionAggregationFunction implements AggregationFunction<String> {
  private boolean _useAttributes;
  private boolean _includeEmptyCategories;

  private static final Logger LOGGER = LoggerFactory.getLogger(RegionAggregationFunction.class);
  private static final String NAME = "Region";
  private static final String OTHER = "Other";
  private static final String NO_REGION = "N/A";

  private static final List<String> TOP_LEVEL_REGIONS = Arrays.asList("Africa", "Asia", "South America", "Europe");
  private static final List<String> SPECIAL_COUNTRIES_REGIONS = Arrays.asList("United States", "Canada");
  private static final List<String> REQUIRED_ENTRIES = Lists.newArrayList();

  static {
    REQUIRED_ENTRIES.addAll(TOP_LEVEL_REGIONS);
    REQUIRED_ENTRIES.addAll(SPECIAL_COUNTRIES_REGIONS);
    REQUIRED_ENTRIES.add(OTHER);
    REQUIRED_ENTRIES.add(NO_REGION);
  }

  private SecuritySource _secSource;
  private final RegionSource _regionSource;
  private final ExchangeSource _exchangeSource;
  private final Comparator<Position> _comparator = new SimplePositionComparator();


  public RegionAggregationFunction(final SecuritySource secSource, final RegionSource regionSource, final ExchangeSource exchangeSource) {
    this(secSource, regionSource, exchangeSource, false);
  }

  public RegionAggregationFunction(final SecuritySource secSource, final RegionSource regionSource, final ExchangeSource exchangeSource, final boolean useAttributes) {
    this(secSource, regionSource, exchangeSource, useAttributes, true);
  }

  public RegionAggregationFunction(final SecuritySource secSource, final RegionSource regionSource, final ExchangeSource exchangeSource, final boolean useAttributes, final boolean includeEmptyCategories) {
    _secSource = secSource;
    _regionSource = regionSource;
    _exchangeSource = exchangeSource;
    _useAttributes = useAttributes;
    _includeEmptyCategories = includeEmptyCategories;
  }

  /**
   * Can use this when no RegionSource available and will get the ISO code instead of the pretty string.
   */
  public RegionAggregationFunction() {
    _regionSource = null;
    _exchangeSource = null;
  }

  @Override
  public String classifyPosition(final Position position) {
    if (_useAttributes) {
      final Map<String, String> attributes = position.getAttributes();
      LOGGER.warn("attributes on " + position + " = " + attributes.entrySet());
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      }
      return NO_REGION;
    }
    try {
      Security security = position.getSecurityLink().getTarget();
      if (security == null) {
        security = position.getSecurityLink().resolve(_secSource);
      }
      final ExternalId id = FinancialSecurityUtils.getRegion(security);
      if (_regionSource != null) {
        if (id != null) {
          final Region highestLevelRegion = _regionSource.getHighestLevelRegion(id);
          if (highestLevelRegion != null) {
            return highestLevelRegion.getName();
          }
          return id.getValue();
        } else if (_exchangeSource != null) {
          final ExternalId exchangeId = FinancialSecurityUtils.getExchange(security);
          if (exchangeId != null) {
            final Exchange exchange = _exchangeSource.getSingle(exchangeId);
            if (exchange == null) {
              LOGGER.info("No exchange could be found with ID {}", exchangeId);
              return NO_REGION;
            }
            if (exchange.getRegionIdBundle() == null) {
              LOGGER.info("Exchange " + exchange.getName() + " region bundle was null");
              return NO_REGION;
            }
            final Region highestLevelRegion = _regionSource.getHighestLevelRegion(exchange.getRegionIdBundle());
            if (SPECIAL_COUNTRIES_REGIONS.contains(highestLevelRegion.getName())) {
              return highestLevelRegion.getName();
            }
            final Set<UniqueId> parentRegionIds = highestLevelRegion.getParentRegionIds();
            LOGGER.info("got " + highestLevelRegion + ", looking for parent");
            final String parent = findTopLevelRegion(parentRegionIds);
            LOGGER.info("parent was " + parent);
            return parent;
          }
        }
        return NO_REGION;
      }
      if (id != null) {
        return id.getValue();
      }
      return NO_REGION;
    } catch (final UnsupportedOperationException ex) {
      return NO_REGION;
    }
  }

  private String findTopLevelRegion(final Set<?> parentRegions) {
    for (final Object parentRegion : parentRegions) {
      Region region;
      if (parentRegion instanceof String) {
        region = _regionSource.get(UniqueId.parse((String) parentRegion));
      } else {
        region = _regionSource.get((UniqueId) parentRegion);
      }
      if (region != null) {
        if (TOP_LEVEL_REGIONS.contains(region.getName())) {
          return region.getName();
        }
      }
    }
    return OTHER;
  }

  @Override
  public String getName() {
    return NAME;
  }



  @Override
  public Collection<String> getRequiredEntries() {
    if (_includeEmptyCategories) {
      return REQUIRED_ENTRIES;
    }
    return Collections.emptyList();
  }

  @Override
  public int compare(final String o1, final String o2) {
    return CompareUtils.compareByList(REQUIRED_ENTRIES, o1, o2);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }
}
