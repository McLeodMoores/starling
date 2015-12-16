/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.financial.curve;

import java.util.Collections;
import java.util.Set;

import com.mcleodmoores.quandl.convention.QuandlFedFundsFutureConvention;
import com.mcleodmoores.quandl.convention.QuandlFinancialConventionVisitor;
import com.mcleodmoores.quandl.convention.QuandlStirFutureConvention;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitor;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.util.money.Currency;

/**
 * Adds support for {@link com.mcleodmoores.quandl.convention.QuandlFinancialConvention} types to {@link CurveNodeCurrencyVisitor}.
 */
public class QuandlCurveNodeCurrencyVisitor extends CurveNodeCurrencyVisitor implements QuandlFinancialConventionVisitor<Set<Currency>> {

  /**
   * Creates an instance, setting the config source to null.
   * @param conventionSource The convention source, not null
   * @param securitySource The security source, not null
   */
  public QuandlCurveNodeCurrencyVisitor(final ConventionSource conventionSource, final SecuritySource securitySource) {
    super(conventionSource, securitySource);
  }

  /**
   * Creates an instance.
   * @param conventionSource The convention source, not null
   * @param securitySource The security source, not null
   * @param configSource The config source, not null
   */
  public QuandlCurveNodeCurrencyVisitor(final ConventionSource conventionSource, final SecuritySource securitySource, final ConfigSource configSource) {
    super(conventionSource, securitySource, configSource);
  }

  /**
   * If the convention is a {@link QuandlStirFutureConvention} or {@link QuandlFedFundsFutureConvention}, returns the currency. Otherwise,
   * delegates to the superclass.
   * {@inheritDoc}.
   */
  @Override
  public Set<Currency> visitRateFutureNode(final RateFutureNode node) {
    final FinancialConvention convention = getConventionSource().getSingle(node.getFutureConvention(), FinancialConvention.class);
    if (convention == null) {
      throw new Quandl4OpenGammaRuntimeException("Could not get convention with id " + node.getFutureConvention());
    }
    if (convention instanceof QuandlStirFutureConvention) {
      final QuandlStirFutureConvention quandlConvention = (QuandlStirFutureConvention) convention;
      return Collections.singleton(quandlConvention.getCurrency());
    }
    if (convention instanceof QuandlFedFundsFutureConvention) {
      final QuandlFedFundsFutureConvention quandlConvention = (QuandlFedFundsFutureConvention) convention;
      return Collections.singleton(quandlConvention.getCurrency());
    }
    return super.visitRateFutureNode(node);
  }

  @Override
  public Set<Currency> visitQuandlStirFutureConvention(final QuandlStirFutureConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitQuandlFedFundsFutureConvention(final QuandlFedFundsFutureConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }
}
