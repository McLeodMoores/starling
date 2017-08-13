/**
 *
 */
package com.mcleodmoores.examples.simulated.loader.config;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class ExampleConfigUtils {
  private static final String FX_IMPLIED_CONSTRUCTION_SUFFIX = " FX Implied Curve";
  private static final String FX_IMPLIED_CURVE_SUFFIX = " FX";
  private static final String CURVE_CONSTRUCTION_CONFIG_PREFIX = "Default ";
  private static final String CURVE_CONSTRUCTION_CONFIG_SUFFIX = " Curves";
  private static final String DISCOUNTING_CURVE_NAME_SUFFIX = " Discounting";
  private static final Map<String, String> INDEX_NAMES = new HashMap<>();

  static {
    INDEX_NAMES.put("AUD", "BANK BILL");
    INDEX_NAMES.put("CHF", "LIBOR");
    INDEX_NAMES.put("EUR", "EURIBOR");
    INDEX_NAMES.put("GBP", "LIBOR");
    INDEX_NAMES.put("JPY", "TIBOR");
    INDEX_NAMES.put("USD", "LIBOR");
  }

  public static String generateFxImpliedConfigName(final String currency) {
    return currency + FX_IMPLIED_CONSTRUCTION_SUFFIX;
  }

  public static String generateVanillaFixedIncomeConfigName(final String currency) {
    return CURVE_CONSTRUCTION_CONFIG_PREFIX + currency + CURVE_CONSTRUCTION_CONFIG_SUFFIX;
  }

  public static String generateFxImpliedCurveName(final String currency) {
    return currency + FX_IMPLIED_CURVE_SUFFIX;
  }

  public static String generateVanillaFixedIncomeDiscountCurveName(final String currency) {
    return currency + DISCOUNTING_CURVE_NAME_SUFFIX;
  }

  public static String generateVanillaFixedIncomeIborCurveName(final String currency, final Tenor tenor) {
    return currency + " " + tenor.toFormattedString().substring(1) + " " + INDEX_NAMES.get(currency);
  }

  public static ConfigItem<CurveConstructionConfiguration> makeConfig(final CurveConstructionConfiguration curveConfig) {
    final ConfigItem<CurveConstructionConfiguration> config = ConfigItem.of(curveConfig);
    config.setName(curveConfig.getName());
    return config;
  }

  public static ConfigItem<CurveNodeIdMapper> makeConfig(final CurveNodeIdMapper curveNodeIdMapper) {
    final ConfigItem<CurveNodeIdMapper> config = ConfigItem.of(curveNodeIdMapper);
    config.setName(curveNodeIdMapper.getName());
    return config;
  }

  public static ConfigItem<CurveDefinition> makeConfig(final CurveDefinition curveDefinition) {
    final ConfigItem<CurveDefinition> config = ConfigItem.of(curveDefinition);
    config.setName(curveDefinition.getName());
    return config;
  }


}
