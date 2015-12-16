/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.config;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.SimpleCurrencyMatrix;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 *
 */
public final class QuandlCurrencyMatrixGenerator {

  /**
   * Restricted constructor.
   */
  private QuandlCurrencyMatrixGenerator() {
  }

  /**
   * Creates the configuration using the dominance order from the pairs configuration.
   * @param pairs The currency pairs, not null
   * @return A currency matrix
   */
  public static CurrencyMatrix createConfiguration(final CurrencyPairs pairs) {
    ArgumentChecker.notNull(pairs, "pairs");
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    for (final CurrencyPair pair : pairs.getPairs()) {
      final String code = "CURRFX/" + pair.getBase().getCode().toUpperCase() + pair.getCounter().getCode().toUpperCase();
      final ExternalId id = QuandlConstants.ofCode(code);
      matrix.setLiveData(pair.getCounter(), pair.getBase(),
          new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, id));
    }
    final StringBuilder sb = new StringBuilder();
    sb.append('\n');
    for (final Currency x : matrix.getTargetCurrencies()) {
      sb.append('\t').append(x.getCode());
    }
    for (final Currency y : matrix.getSourceCurrencies()) {
      sb.append('\n').append(y.getCode());
      for (final Currency x : matrix.getTargetCurrencies()) {
        sb.append('\t').append(matrix.getConversion(y, x));
      }
    }
    return matrix;
  }
}
