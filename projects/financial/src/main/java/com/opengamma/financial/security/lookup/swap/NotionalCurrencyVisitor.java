/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.security.swap.CommodityNotional;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.NotionalVisitor;
import com.opengamma.financial.security.swap.SecurityNotional;
import com.opengamma.financial.security.swap.VarianceSwapNotional;
import com.opengamma.util.money.Currency;

/**
 *
 */
/* package */ class NotionalCurrencyVisitor implements NotionalVisitor<Currency> {

  @Override
  public Currency visitCommodityNotional(final CommodityNotional notional) {
    return null;
  }

  @Override
  public Currency visitInterestRateNotional(final InterestRateNotional notional) {
    return notional.getCurrency();
  }

  @Override
  public Currency visitSecurityNotional(final SecurityNotional notional) {
    return null;
  }

  @Override
  public Currency visitVarianceSwapNotional(final VarianceSwapNotional notional) {
    return null;
  }

}
