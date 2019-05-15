/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Populates the config master with example exposure functions.
 */
public class ExamplesExposureFunctionConfigsPopulator {

  /**
   * Populates a config master with exposure functions.
   *
   * @param configMaster
   *          The config master, not null
   */
  public static void populateConfigMaster(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    storeAudFixedIncomeExposures(configMaster);
    storeFixedIncomeExposures(configMaster);
    storeFxExposures(configMaster);
    storeGovernmentBondExposures(configMaster);
    storeCorporateBondExposures(configMaster);
  }

  private static void storeAudFixedIncomeExposures(final ConfigMaster configMaster) {
    String name;
    List<String> exposureFunctionNames;
    Map<ExternalId, String> idsToNames;
    ExposureFunctions exposureFunctions;
    name = "AUD Swaps (1)";
    exposureFunctionNames = Arrays.asList("Currency");
    idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "AUD"), "AUD Bank Bill Curves (1)");
    exposureFunctions = new ExposureFunctions(name, exposureFunctionNames, idsToNames);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(exposureFunctions));
    name = "AUD Swaps (2)";
    exposureFunctionNames = Arrays.asList("Currency");
    idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "AUD"), "AUD Bank Bill Curves (2)");
    exposureFunctions = new ExposureFunctions(name, exposureFunctionNames, idsToNames);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(exposureFunctions));
  }

  private static void storeFixedIncomeExposures(final ConfigMaster configMaster) {
    String name;
    List<String> exposureFunctionNames;
    Map<ExternalId, String> idsToNames;
    ExposureFunctions exposureFunctions;
    name = "Fixed Income Exposures";
    exposureFunctionNames = Arrays.asList("Currency");
    idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "USD"), ExampleConfigUtils.generateVanillaFixedIncomeConfigName("USD"));
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "AUD"), ExampleConfigUtils.generateVanillaFixedIncomeConfigName("AUD"));
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "CAD"), ExampleConfigUtils.generateVanillaFixedIncomeConfigName("CAD"));
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "EUR"), ExampleConfigUtils.generateVanillaFixedIncomeConfigName("EUR"));
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "GBP"), ExampleConfigUtils.generateVanillaFixedIncomeConfigName("GBP"));
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "CHF"), ExampleConfigUtils.generateVanillaFixedIncomeConfigName("CHF"));
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "JPY"), ExampleConfigUtils.generateVanillaFixedIncomeConfigName("JPY"));
    exposureFunctions = new ExposureFunctions(name, exposureFunctionNames, idsToNames);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(exposureFunctions));
  }

  private static void storeFxExposures(final ConfigMaster configMaster) {
    final String name = "FX Exposures";
    final List<String> exposureFunctionNames = Arrays.asList("Currency");
    final Map<ExternalId, String> idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "USD"), ExampleConfigUtils.generateFxImpliedConfigName("CHF"));
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "CHF"), ExampleConfigUtils.generateFxImpliedConfigName("CHF"));
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "EUR"), ExampleConfigUtils.generateFxImpliedConfigName("EUR"));
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "JPY"), ExampleConfigUtils.generateFxImpliedConfigName("JPY"));
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "GBP"), ExampleConfigUtils.generateFxImpliedConfigName("GBP"));
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "AUD"), ExampleConfigUtils.generateFxImpliedConfigName("AUD"));
    idsToNames.put(ExternalId.of(Currency.OBJECT_SCHEME, "NZD"), ExampleConfigUtils.generateFxImpliedConfigName("NZD"));
    final ExposureFunctions exposureFunctions = new ExposureFunctions(name, exposureFunctionNames, idsToNames);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(exposureFunctions));
  }

  private static void storeGovernmentBondExposures(final ConfigMaster configMaster) {
    final String name = "Govt Bond Exposures";
    final List<String> exposureFunctionNames = Arrays.asList("Region");
    final Map<ExternalId, String> idsToNames = Collections.singletonMap(ExternalSchemes.countryRegionId(Country.US), "US Treasury");
    final ExposureFunctions exposureFunctions = new ExposureFunctions(name, exposureFunctionNames, idsToNames);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(exposureFunctions));
  }

  private static void storeCorporateBondExposures(final ConfigMaster configMaster) {
    final String name = "Corp Bond Exposures";
    final List<String> exposureFunctionNames = Arrays.asList("Region");
    final Map<ExternalId, String> idsToNames = Collections.singletonMap(ExternalSchemes.countryRegionId(Country.US), "US Corp");
    final ExposureFunctions exposureFunctions = new ExposureFunctions(name, exposureFunctionNames, idsToNames);
    ConfigMasterUtils.storeByName(configMaster, makeConfig(exposureFunctions));
  }

  private static ConfigItem<ExposureFunctions> makeConfig(final ExposureFunctions exposureFunctions) {
    final ConfigItem<ExposureFunctions> config = ConfigItem.of(exposureFunctions);
    config.setName(exposureFunctions.getName());
    return config;
  }
}
