/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import com.opengamma.util.money.Currency;

/**
 *
 */
public interface FxDataProvider extends DataProvider {

  @Override
  FxDataProvider copy();

  double getFxRate(Currency ccy1, Currency ccy2);

}
