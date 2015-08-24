/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * This class checks that all exogenous configurations referenced in a {@link CurveConstructionConfiguration} are available
 * from the config source.
 */
public final class ExogenousCurveConstructionConfigurationValidator extends
  ConfigurationValidator<CurveConstructionConfiguration, CurveConstructionConfiguration> {
  /** A static instance */
  private static final ConfigurationValidator<CurveConstructionConfiguration, CurveConstructionConfiguration> INSTANCE =
      new ExogenousCurveConstructionConfigurationValidator();

  /**
   * Returns a static instance.
   * @return  a static instance
   */
  public static ConfigurationValidator<CurveConstructionConfiguration, CurveConstructionConfiguration> getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private ExogenousCurveConstructionConfigurationValidator() {
  }

  @Override
  public ConfigurationValidationInfo<CurveConstructionConfiguration> validate(final CurveConstructionConfiguration curveConstructionConfiguration,
      final VersionCorrection versionCorrection, final ConfigSource configSource) {
    ArgumentChecker.notNull(curveConstructionConfiguration, "curveConstructionConfiguration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(configSource, "configSource");
    final Collection<CurveConstructionConfiguration> configurations = new HashSet<>();
    final Map<String, Class<?>> missingConfigurations = new HashMap<>();
    final Collection<Object> duplicatedConfigurations = new HashSet<>();
    final List<String> exogenousConfigurations = curveConstructionConfiguration.getExogenousConfigurations();
    for (final String exogenousConfiguration : exogenousConfigurations) {
      final Collection<ConfigItem<CurveConstructionConfiguration>> cccItems = configSource.get(CurveConstructionConfiguration.class,
          exogenousConfiguration, versionCorrection);
      if (cccItems.size() > 1) {
        duplicatedConfigurations.addAll(cccItems);
      } else if (cccItems.isEmpty()) {
        missingConfigurations.put(exogenousConfiguration, CurveConstructionConfiguration.class);
      } else {
        //TODO recursive search
        configurations.add(Iterables.getOnlyElement(cccItems).getValue());
      }
    }
    return new ConfigurationValidationInfo<>(CurveConstructionConfiguration.class, configurations, missingConfigurations, duplicatedConfigurations);
  }
}
