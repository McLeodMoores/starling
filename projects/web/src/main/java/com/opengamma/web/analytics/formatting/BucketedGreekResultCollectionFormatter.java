/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.greeks.BucketedGreekResultCollection;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class BucketedGreekResultCollectionFormatter extends AbstractFormatter<BucketedGreekResultCollection> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BucketedGreekResultCollectionFormatter.class);

  /* package */ BucketedGreekResultCollectionFormatter() {
    super(BucketedGreekResultCollection.class);
  }

  @Override
  public Object formatCell(final BucketedGreekResultCollection value, final ValueSpecification valueSpec, final Object inlineKey) {
    if (value.getBucketedGreeks(BucketedGreekResultCollection.BUCKETED_VEGA) != null) {
      final double[] expiries = value.getExpiries();
      final double[][] strikes = value.getStrikes();
      final double[] uniqueStrikes = strikes[0];
      for (int i = 1; i < strikes.length; i++) {
        if (strikes[i].length != uniqueStrikes.length) {
          LOGGER.warn("Did not have a rectangular bucketed vega surface");
          return FORMATTING_ERROR;
        }
      }
      return "Volatility Surface (" + expiries.length + " x " + uniqueStrikes.length + ")";
    }
    return FORMATTING_ERROR;
  }

  @Override
  public DataType getDataType() {
    return DataType.SURFACE_DATA;
  }
}
