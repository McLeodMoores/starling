/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.riskfactors;

import com.opengamma.util.money.Currency;

/**
 * Default implementation of {@link RiskFactorsConfigurationProvider}.
 */
public class DefaultRiskFactorsConfigurationProvider implements RiskFactorsConfigurationProvider {

  private final Currency _currencyOverride;

  public DefaultRiskFactorsConfigurationProvider() {
    _currencyOverride = null;
  }

  public DefaultRiskFactorsConfigurationProvider(final Currency currencyOverride) {
    _currencyOverride = currencyOverride;
  }

  @Override
  public Currency getCurrencyOverride() {
    return _currencyOverride;
  }

  @Override
  public String getFundingCurve() {
    return "Discounting";
  }

  @Override
  public String getForwardCurve(final Currency currency) {
    String suffix;
    if (currency.equals(Currency.USD) || currency.equals(Currency.NZD) || currency.equals(Currency.SEK)) {
      suffix = "3M";
    } else {
      suffix = "6M";
    }
    return "Forward" + suffix;
  }

  @Override
  public String getFXCurve(final Currency ccy) {
    return "Discounting";
  }

  @Override
  public String getFXVanillaOptionSurfaceName(final Currency ccy1, final Currency ccy2) {
    return "TULLETT";  //_" + ccy1.getCode() + ccy2.getCode();
  }

  @Override
  public String getIRFutureOptionVolatilitySurfaceName(final String futureCode) {
    return "DEFAULT";  // + futureCode;;
  }

  @Override
  public String getCommodityFutureOptionVolatilitySurfaceName(final String futureCode) {
    return "DEFAULT";  // + futureCode;
  }

  @Override
  public String getEquityIndexOptionVolatilitySurfaceName(final String tickerPlusMarketSector) {
    return "BBG";  // + tickerPlusMarketSector;
  }

  @Override
  public String getSwaptionVolatilitySurfaceName(final Currency ccy) {
    return "DEFAULT";  //_ + ccy.getCode();
  }

  @Override
  public String getSwaptionVolatilityCubeName(final Currency ccy) {
    return "BLOOMBERG";  // + ccy.getCode();
  }

  @Override
  public String getFXCalculationMethod() {
    return "BlackMethod";
  }

  @Override
  public String getEquityFundingCurve() {
    return "FUNDING";
  }

  @Override
  public String getEquityCalculationMethod() {
    return "BlackMethod";
  }

  @Override
  public String getEquitySmileInterpolator() {
    return "Spline";
  }
}
