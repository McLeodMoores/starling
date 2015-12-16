/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.mcleodmoores.quandl.normalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.classification.QuandlCodeClassifier;
import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Implementation of an {@link HistoricalTimeSeriesAdjuster} for Quandl data.
 */
public class QuandlNormalizer implements HistoricalTimeSeriesAdjuster {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlNormalizer.class);
  /** The code classifier */
  private final QuandlCodeClassifier _classifier;

  /**
   * Creates an instance.
   * @param classifier The classifier, not null
   */
  public QuandlNormalizer(final QuandlCodeClassifier classifier) {
    ArgumentChecker.notNull(classifier, "classifier");
    _classifier = classifier;
  }

  @Override
  public HistoricalTimeSeries adjust(final ExternalIdBundle idBundle, final HistoricalTimeSeries timeSeries) {
    ArgumentChecker.notNull(idBundle, "idBundle");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    final Integer normalizationFactor = getNormalizationFactor(idBundle);
    if (normalizationFactor == null) {
      return timeSeries;
    }
    final LocalDateDoubleTimeSeries normalizedTimeSeries = timeSeries.getTimeSeries().divide(normalizationFactor);
    return new SimpleHistoricalTimeSeries(timeSeries.getUniqueId(), normalizedTimeSeries);
  }

  @Override
  public HistoricalTimeSeriesAdjustment getAdjustment(final ExternalIdBundle idBundle) {
    ArgumentChecker.notNull(idBundle, "idBundle");
    final Integer normalizationFactor = getNormalizationFactor(idBundle);
    if (normalizationFactor == null) {
      return HistoricalTimeSeriesAdjustment.NoOp.INSTANCE;
    }
    return new HistoricalTimeSeriesAdjustment.DivideBy(normalizationFactor);
  }

  /**
   * Gets the normalization factor.
   * @param idBundle The id bundle
   * @return The normalization factor, null if not found
   */
  private Integer getNormalizationFactor(final ExternalIdBundle idBundle) {
    final String quandlCode = idBundle.getValue(QuandlConstants.QUANDL_CODE);
    if (quandlCode == null) {
      LOGGER.warn("Unable to classify security for normalization as no Quandl code "
          + "found in bundle: {}. The time-series will be unnormalized.", idBundle);
      return null;
    }
    final Integer normalizationFactor = _classifier.getNormalizationFactor(quandlCode);
    if (normalizationFactor == null) {
      LOGGER.warn("Unable to classify security for normalization: {}. "
          + "The time-series will be unnormalized.", idBundle);
      return null;
    }
    if (normalizationFactor == 1) {
      return null;
    }
    return normalizationFactor;
  }

}
