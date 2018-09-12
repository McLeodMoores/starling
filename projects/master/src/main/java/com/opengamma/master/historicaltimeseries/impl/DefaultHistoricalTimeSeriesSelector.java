/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;

/**
 * A time-series resolver for a specific data source which uses configuration to interpret the resolution key.
 */
public class DefaultHistoricalTimeSeriesSelector implements HistoricalTimeSeriesSelector {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHistoricalTimeSeriesSelector.class);

  /**
   * The source of configuration.
   */
  private final ConfigSource _configSource;

  public DefaultHistoricalTimeSeriesSelector(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableHistoricalTimeSeriesInfo select(final Collection<ManageableHistoricalTimeSeriesInfo> candidates, final String selectionKey) {
    String sKey = selectionKey;
    sKey = MoreObjects.firstNonNull(sKey, HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME);

    //IGN-139 - avoid rating unless we have to
    switch (candidates.size()) {
      case 0:
        return null;
      case 1:
        return Iterables.getOnlyElement(candidates);
      default:
        // Pick best using rules from configuration
        final HistoricalTimeSeriesRating rating = _configSource.getLatestByName(HistoricalTimeSeriesRating.class, sKey);
        if (rating == null) {
          LOGGER.warn("Resolver failed to find configuration: {}", sKey);
          return null;
        }
        return bestMatch(candidates, rating);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Choose the best match using the configured rules.
   *
   * @param matches  the list of matches, not null
   * @param rating  the rules for scoring the matches, not null
   * @return the best match, null if no match
   */
  private static ManageableHistoricalTimeSeriesInfo bestMatch(final Collection<ManageableHistoricalTimeSeriesInfo> matches,
      final HistoricalTimeSeriesRating rating) {
    LOGGER.debug("Find best match using rules: {}", rating);
    int currentScore = Integer.MIN_VALUE;
    ManageableHistoricalTimeSeriesInfo bestMatch = null;
    for (final ManageableHistoricalTimeSeriesInfo match : matches) {
      final int score = rating.rate(match);
      LOGGER.debug("Score: {} for info: {}", score, match);
      if (score > currentScore) {
        currentScore = score;
        bestMatch = match;
      }
    }
    return bestMatch;
  }

}
