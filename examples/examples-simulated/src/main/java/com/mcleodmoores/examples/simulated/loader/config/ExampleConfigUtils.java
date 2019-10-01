/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
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
 * Utilities for example configurations.
 */
public final class ExampleConfigUtils {
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

  /**
   * Creates a name: <code>[CCY] FX Implied Curve</code>.
   *
   * @param currency
   *          the currency
   * @return the name
   */
  public static String generateFxImpliedConfigName(final String currency) {
    return currency + FX_IMPLIED_CONSTRUCTION_SUFFIX;
  }

  /**
   * Creates a name: <code>Default [CCY] Curves</code>.
   *
   * @param currency
   *          the currency
   * @return the name
   */
  public static String generateVanillaFixedIncomeConfigName(final String currency) {
    return CURVE_CONSTRUCTION_CONFIG_PREFIX + currency + CURVE_CONSTRUCTION_CONFIG_SUFFIX;
  }

  /**
   * Creates a name: <code>[CCY] FX</code>.
   *
   * @param currency
   *          the currency
   * @return the name
   */
  public static String generateFxImpliedCurveName(final String currency) {
    return currency + FX_IMPLIED_CURVE_SUFFIX;
  }

  /**
   * Creates a name: <code>[CCY] Discounting</code>.
   *
   * @param currency
   *          the currency
   * @return the name
   */
  public static String generateVanillaFixedIncomeDiscountCurveName(final String currency) {
    return currency + DISCOUNTING_CURVE_NAME_SUFFIX;
  }

  /**
   * Creates a name: <code>[CCY] [TENOR] [IBOR INDEX NAME]</code>.
   *
   * @param currency
   *          the currency
   * @param tenor
   *          the tenor
   * @return the name
   */
  public static String generateVanillaFixedIncomeIborCurveName(final String currency, final Tenor tenor) {
    return currency + " " + tenor.toFormattedString().substring(1) + " " + INDEX_NAMES.get(currency);
  }

  /**
   * Creates a config item.
   *
   * @param curveConfig
   *          the curve configuration
   * @return the item
   */
  public static ConfigItem<CurveConstructionConfiguration> makeConfig(final CurveConstructionConfiguration curveConfig) {
    return makeConfig(curveConfig, curveConfig.getName());
  }

  /**
   * Creates a config item.
   *
   * @param curveNodeIdMapper
   *          the curve node id mapper
   * @return the item
   */
  public static ConfigItem<CurveNodeIdMapper> makeConfig(final CurveNodeIdMapper curveNodeIdMapper) {
    final ConfigItem<CurveNodeIdMapper> config = ConfigItem.of(curveNodeIdMapper);
    config.setName(curveNodeIdMapper.getName());
    return config;
  }

  /**
   * Creates a config item.
   *
   * @param curveDefinition
   *          the curve definition
   * @return the item
   */
  public static ConfigItem<CurveDefinition> makeConfig(final CurveDefinition curveDefinition) {
    return makeConfig(curveDefinition, curveDefinition.getName());
  }

  /**
   * Creates a config item.
   *
   * @param config
   *          the configuration
   * @param name
   *          the name
   * @return the item
   * @param <T>
   *          the type of the config item
   */
  public static <T> ConfigItem<T> makeConfig(final T config, final String name) {
    final ConfigItem<T> item = ConfigItem.of(config);
    item.setName(name);
    return item;
  }

  private ExampleConfigUtils() {
  }
}
