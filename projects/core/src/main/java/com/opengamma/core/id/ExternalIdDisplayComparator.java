/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.id;

import java.util.Comparator;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

/**
 * Comparator to define order in which schemes are sorted in ExternalIdBundles that work best for display purposes.
 * Here we're defining a table of scores for each scheme
 */
@SuppressWarnings("deprecation")
public class ExternalIdDisplayComparator implements Comparator<ExternalId> {

  /**
   * The map of scores.
   */
  static final Map<ExternalScheme, Integer> SCORE_MAP = Maps.newHashMap();
  static {
    SCORE_MAP.put(ExternalSchemes.BLOOMBERG_TCM, 20); // because if there's both ticker and tcm, you want to see tcm.
    SCORE_MAP.put(ExternalSchemes.BLOOMBERG_TICKER, 19);
    SCORE_MAP.put(ExternalSchemes.RIC, 17);
    SCORE_MAP.put(ExternalSchemes.BLOOMBERG_TICKER_WEAK, 16);
    SCORE_MAP.put(ExternalSchemes.ACTIVFEED_TICKER, 15);
    SCORE_MAP.put(ExternalSchemes.SURF, 14);
    SCORE_MAP.put(ExternalSchemes.ISIN, 13);
    SCORE_MAP.put(ExternalSchemes.CUSIP, 12);
    SCORE_MAP.put(ExternalSchemes.SEDOL1, 11);
    SCORE_MAP.put(ExternalSchemes.OG_SYNTHETIC_TICKER, 10);
    SCORE_MAP.put(ExternalSchemes.BLOOMBERG_BUID, 5);
    SCORE_MAP.put(ExternalSchemes.BLOOMBERG_BUID_WEAK, 4);
  }

  /**
   * The map of scores.
   */
  private Map<ExternalScheme, Integer> _scoreMap;

  /**
   * Uses hard-coded default information about scores.
   */
  public ExternalIdDisplayComparator() {
    _scoreMap = SCORE_MAP;
  }

  /**
   * Initialize comparator using configuration object stored in config database.
   *
   * @param orderConfig  sourced from a ConfigSource
   */
  public ExternalIdDisplayComparator(final ExternalIdOrderConfig orderConfig) {
    // TODO: code missing!
  }

  private int scoreExternalId(final ExternalId id) {
    if (_scoreMap.containsKey(id.getScheme())) {
      return _scoreMap.get(id.getScheme());
    }
    return 0;
  }

  @Override
  public int compare(final ExternalId id0, final ExternalId id1) {
    final int score0 = scoreExternalId(id0);
    final int score1 = scoreExternalId(id1);
    if (score1 - score0 != 0) {
      return score1 - score0;
    }
    return id0.compareTo(id1);
  }

}
