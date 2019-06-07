/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

/**
 * Function exclusion groups for use with the OG-Analytics package.
 */
public interface OpenGammaFunctionExclusions {

  // CSOFF
  String BLACK_VOLATILITY_SURFACE_DEFAULTS = "BLACK_VOLATILITY_SURFACE_DEFAULTS";
  String SURFACE_DEFAULTS = "SURFACE";
  String EQUITY_PURE_VOLATILITY_SURFACE_DEFAULTS = "EQUITY_PURE_VOLATILITY_SURFACE_DEFAULTS";
  String BLACK_VOLATILITY_SURFACE_INTERPOLATOR_DEFAULTS = "BLACK_VOLATILITY_SURFACE_INTERPOLATOR_DEFAULTS";
  String PDE_DEFAULTS = "PDE_DEFAULTS";
  String CURVE_DEFAULTS = "CURVE";
  String LOCAL_VOLATILITY_SURFACE_DEFAULTS = "LOCAL_VOLATILITY_SURFACE_DEFAULTS";
  String PNL_SERIES = "PNL_SERIES";
  String INTEREST_RATE_INSTRUMENT_DEFAULTS = "INTEREST_RATE_INSTRUMENT_DEFAULTS";
  String EXTERNALLY_PROVIDED_SENSITIVITIES_DEFAULTS = "EXTERNALLY PROVIDED SENSITIVITIES DEFAULTS";
  String EQUITY_VARIANCE_SWAP_DEFAULTS = "EQUITY VARIANCE SWAP DEFAULTS";
  String EQUITY_FUTURE_DEFAULTS = "EQUITY FUTURE DEFAULTS";
  String SURFACE_CALCULATION_METHOD_DEFAULTS = "30SURFACE_CALCULATION_METHOD_DEFAULTS";
  String CALCULATION_METHOD_DEFAULTS = "40CALCULATION_METHOD_DEFAULTS";
  String EQUITY_OPTION_DEFAULTS = "10EQUITY_OPTION_DEFAULTS";
  String INTERPOLATED_BLACK_LOGNORMAL_DEFAULTS = "20INTERPOLATED_BLACK_LOGNORMAL_DEFAULTS";
  String EQUITY_FORWARD_CURVE_DEFAULTS = "EQUITY_FORWARD_CURVE_DEFAULTS";
  String COMMODITY_FORWARD_CURVE_DEFAULTS = "COMMODITY_FORWARD_CURVE_DEFAULTS";
  String SABR_FITTING_DEFAULTS = "SABR_FITTING_DEFAULTS";
  String XCCY_SWAP_DEFAULTS = "XCCY_SWAP_DEFAULTS";
  String FX_FORWARD_DEFAULTS = "FX_FORWARD_DEFAULTS";
  String FX_FORWARD_THETA_DEFAULTS = "FX_FORWARD_THETA_DEFAULTS";
  String FX_OPTION_BLACK_DEFAULTS = "FX_OPTION_BLACK_DEFAULTS";
  String FX_DIGITAL_OPTION_CALL_SPREAD_BLACK_DEFAULTS = "FX_DIGITAL_OPTION_CALL_SPREAD_BLACK_DEFAULTS";
  String FX_OPTION_THETA_DEFAULTS = "FX_OPTION_THETA_DEFAULTS";
  String NORMAL_HISTORICAL_VAR = "NORMAL_HISTORICAL_VAR";
  String EMPIRICAL_HISTORICAL_VAR = "EMPIRICAL_HISTORICAL_VAR";
  String SWAPTION_BASIC_BLACK_DEFAULTS = "SWAPTION_BASIC_BLACK_DEFAULTS";
  String SWAPTION_BLACK_DEFAULTS = "SWAPTION_BLACK_DEFAULTS";
  String INTEREST_RATE_FUTURE = "INTEREST_RATE_FUTURE";
  String FUTURE_OPTION_BLACK = "FUTURE_OPTION_BLACK";
  String ISDA_LEGACY_CDS_CURVE = "ISDA_LEGACY_CDS_CURVE";
  String ISDA_LEGACY_CDS_PRICING = "ISDA_LEGACY_CDS";
  String INTEREST_RATE_FUTURE_OPTION_HESTON = "INTEREST_RATE_FUTURE_OPTION_HESTON";
  String ISDA_COMPLIANT_YIELD_CURVE_DEFAULTS = "ISDA_COMPLIANT_YIELD_CURVE";
  String ISDA_COMPLIANT_CREDIT_CURVE_DEFAULTS = "ISDA_COMPLIANT_CREDIT_CURVE";
  String ISDA_COMPLIANT_HAZARD_CURVE_DEFAULTS = "ISDA_COMPLIANT_HAZARD_CURVE";
  String ISDA_COMPLIANT_IR01 = "ISDA_COMPLIANT_IR01";
  String ISDA_COMPLIANT_CS01 = "ISDA_COMPLIANT_CS01";
  String ISDA_COMPLIANT_RR01 = "ISDA_COMPLIANT_RR01";
  String ISDA_COMPLIANT_PRICE = "ISDA_COMPLIANT_PRICE";

  // CSON

  String getMutualExclusionGroup();

}
