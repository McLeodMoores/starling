/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.id;

import java.util.Comparator;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.ExternalScheme;

/**
 * Comparator to define order in which schemes are sorted in ExternalIdBundles that work best for display purposes.
 * Here we're defining a table of scores for each scheme
 */
public class ExternalIdWithDatesDisplayComparator implements Comparator<ExternalIdWithDates> {
  static final Map<ExternalScheme, Integer> SCORE_MAP = Maps.newHashMap();
  private Map<ExternalScheme, Integer> _scoreMap;
  private final ExternalIdOrderConfig _orderConfig;

  /**
   * Uses hard-coded default information about scores.
   */
  public ExternalIdWithDatesDisplayComparator() {
    _orderConfig = ExternalIdOrderConfig.DEFAULT_CONFIG;
  }

  /**
   * Initialize comparator using configuration object stored in config database.
   * @param orderConfig sourced from a ConfigSource
   */
  public ExternalIdWithDatesDisplayComparator(final ExternalIdOrderConfig orderConfig) {
    _orderConfig = orderConfig;
    _scoreMap = _orderConfig.getRateMap();
  }

  private int scoreExternalId(final ExternalIdWithDates id) {
    if (_scoreMap.containsKey(id.getExternalId().getScheme())) {
      return _scoreMap.get(id.getExternalId().getScheme());
    }
    return 0;
  }

  @Override
  public int compare(final ExternalIdWithDates id0, final ExternalIdWithDates id1) {
    final int score0 = scoreExternalId(id0);
    final int score1 = scoreExternalId(id1);
    if (score1 - score0 != 0) {
      return score1 - score0;
    }
    return id0.compareTo(id1);
  }
}
