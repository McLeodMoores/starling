/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.examples.simulated.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Sets;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.examples.simulated.historical.SimulatedHistoricalDataGenerator;
import com.opengamma.examples.simulated.volatility.surface.ExampleCallPutVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Populates the example data with equity option volatility surface definitions and specifications.
 * The ATM strike used for the call / put crossover is taken from the historical-data.csv file. If
 * there is no entry for the requested ticker, nothing is stored.
 */
public final class ExampleCallPutVolatilitySurfaceConfigPopulator {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleCallPutVolatilitySurfaceConfigPopulator.class);

  /**
   * Restricted constructor.
   */
  private ExampleCallPutVolatilitySurfaceConfigPopulator() {
  }

  /**
   * Populates the config master with equity option volatility surface definitions and specifications.
   * The approximate ATM strike is found from the historical market data file.
   * @param configMaster The config master, not null
   * @param configs A map from ticker to surface name, not null
   * @return The populated config master
   * @throws IOException If there is an error opening the historical data file.
   */
  public static ConfigMaster populateSurfaceConfigMaster(final ConfigMaster configMaster, final Map<String, String> configs) throws IOException {
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(configs, "configs");
    try (InputStream resource = SimulatedHistoricalDataGenerator.class.getResourceAsStream("historical-data.csv")) {
      if (resource == null) {
        LOGGER.error("Could not get file called historical-data.csv");
        return configMaster;
      }
      try (final CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(resource)))) {
        String[] line = reader.readNext(); //ignore headers
        final Set<String> matchedTickers = new HashSet<>();
        while ((line = reader.readNext()) != null) {
          if (!line[0].equals(ExternalSchemes.OG_SYNTHETIC_TICKER.getName()) || line.length != 4) {
            continue;
          }
          final String ticker = line[1];
          final String field = line[2];
          if (configs.containsKey(ticker) && field.equals("CLOSE")) {
            matchedTickers.add(ticker);
            final String surfaceName = configs.get(ticker);
            final Double spot = Double.valueOf(line[3]);
            final String name = surfaceName + "_" + ticker + "_" + InstrumentTypeProperties.EQUITY_OPTION;
            final UniqueIdentifiable target = UniqueId.of(ExternalSchemes.OG_SYNTHETIC_TICKER.getName(), ticker);
            populateSurfaceSpecification(configMaster, name, target, ticker, spot);
            populateSurfaceDefinition(configMaster, name, target, spot);
          }
        }
        if (!matchedTickers.containsAll(configs.keySet())) {
          LOGGER.error("Could not get all configurations; missing {}", Sets.difference(configs.keySet(), matchedTickers));
        }
        return configMaster;
      }
    }
  }

  /**
   * Adds an equity option volatility surface specification. The surface quote type is put / call, the quote units
   * are volatility and the market data requirement is {@link MarketDataRequirementNames#IMPLIED_VOLATILITY}.
   * @param configMaster The config master
   * @param name The surface name
   * @param target The target
   * @param ticker The ticker
   * @param spot The spot price
   */
  private static void populateSurfaceSpecification(final ConfigMaster configMaster, final String name, final UniqueIdentifiable target,
      final String ticker, final Double spot) {
    final SurfaceInstrumentProvider<Number, Double> provider = new ExampleCallPutVolatilitySurfaceInstrumentProvider(ticker,
        MarketDataRequirementNames.IMPLIED_VOLATILITY, spot);
    final VolatilitySurfaceSpecification specification = new VolatilitySurfaceSpecification(name, target, SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE,
        SurfaceAndCubePropertyNames.VOLATILITY_QUOTE, provider);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(specification, name));
  }

  /**
   * Adds an equity option volatility surface definition. The x axis points are the first eight expiries
   * and the difference between strikes requested is relative to the spot price.
   * @param configMaster The config master
   * @param name The surface name
   * @param target The target
   * @param spot The spot price
   */
  private static void populateSurfaceDefinition(final ConfigMaster configMaster, final String name, final UniqueIdentifiable target,
      final Double spot) {
    final Number[] xs = new Number[] {1, 2, 3, 4, 5, 6, 7, 8 };
    final Double[] ys = new Double[20];
    final double delta = Math.round(spot.intValue() * 2500 / 250000.);
    final double intSpot = Math.round(spot);
    for (int i = 0; i < 20; i++) {
      ys[i] = intSpot + (i - 10) * delta;
    }
    final VolatilitySurfaceDefinition<Number, Double> definition = new VolatilitySurfaceDefinition<>(name, target, xs, ys);
    ConfigMasterUtils.storeByName(configMaster, makeConfigDocument(definition, name));
  }

  /**
   * Creates a config item.
   * @param config The config object
   * @param name The config name
   * @return The config item
   */
  private static <T> ConfigItem<T> makeConfigDocument(final T config, final String name) {
    return ConfigItem.of(config, name, config.getClass());
  }

}
