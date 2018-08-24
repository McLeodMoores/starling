/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.derivative;

import com.opengamma.util.money.Currency;

/**
 * A cash-settled futures contract value of a volatility index published on the expiry date.
 */
public class VolatilityIndexFuture extends IndexFuture {

  public VolatilityIndexFuture(final double timeToExpiry, final double timeToSettlement, final double strike,
      final Currency currency, final double unitAmount) {
    super(timeToExpiry, timeToSettlement, strike, currency, unitAmount);
  }

}
