/**
 *
 */
package com.mcleodmoores.examples.simulated.loader.config;

import com.opengamma.util.time.Tenor;

/**
 *
 */
public class ExampleConfigUtils {
  /** The curve construction configuration prefix */
  private static final String CURVE_CONSTRUCTION_CONFIG_PREFIX = "Default ";
  /** The curve construction configuration suffix */
  private static final String CURVE_CONSTRUCTION_CONFIG_SUFFIX = " Curves";
  /** The discounting curve name suffix */
  private static final String DISCOUNTING_CURVE_NAME_SUFFIX = " Discounting";
  /** The ibor curve name suffix */
  private static final String IBOR_CURVE_NAME_SUFFIX = " XIBOR";

  public static String generateVanillaFixedIncomeConfigName(final String currency) {
    return CURVE_CONSTRUCTION_CONFIG_PREFIX + currency + CURVE_CONSTRUCTION_CONFIG_SUFFIX;
  }

  public static String generateVanillaFixedIncomeDiscountCurveName(final String currency) {
    return currency + DISCOUNTING_CURVE_NAME_SUFFIX;
  }

  public static String generateVanillaFixedIncomeIborCurveName(final String currency, final Tenor tenor) {
    return currency + " " + tenor.toFormattedString().substring(1) + IBOR_CURVE_NAME_SUFFIX;
  }
}
