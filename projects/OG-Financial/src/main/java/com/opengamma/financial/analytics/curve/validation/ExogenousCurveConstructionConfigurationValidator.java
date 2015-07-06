/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Iterables;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.id.VersionCorrection;

/**
 *
 */
public final class ExogenousCurveConstructionConfigurationValidator extends ConfigurationValidator<CurveConstructionConfiguration, CurveConstructionConfiguration> {
  private static final ConfigurationValidator<CurveConstructionConfiguration, CurveConstructionConfiguration> INSTANCE = new ExogenousCurveConstructionConfigurationValidator();

  public static ConfigurationValidator<CurveConstructionConfiguration, CurveConstructionConfiguration> getInstance() {
    return INSTANCE;
  }

  private ExogenousCurveConstructionConfigurationValidator() {
  }

  @Override
  protected ConfigurationValidationInfo<CurveConstructionConfiguration> validate(final CurveConstructionConfiguration curveConstructionConfiguration, final VersionCorrection versionCorrection,
      final ConfigSource configSource) {
    final Collection<CurveConstructionConfiguration> configurations = new HashSet<>();
    final Collection<String> missingExogenousConfigurations = new HashSet<>();
    final Collection<String> duplicatedExogenousConfigurations = new HashSet<>();
    final List<String> exogenousConfigurations = curveConstructionConfiguration.getExogenousConfigurations();
    for (final String exogenousConfiguration : exogenousConfigurations) {
      final Collection<ConfigItem<CurveConstructionConfiguration>> cccItems = configSource.get(CurveConstructionConfiguration.class,
          exogenousConfiguration, versionCorrection);
      if (cccItems.size() > 1) {
        duplicatedExogenousConfigurations.add(exogenousConfiguration);
      } else if (cccItems.isEmpty()) {
        missingExogenousConfigurations.add(exogenousConfiguration);
      } else {
        configurations.add(Iterables.getOnlyElement(cccItems).getValue());
      }
    }
    return new ConfigurationValidationInfo<>(CurveConstructionConfiguration.class, configurations, missingExogenousConfigurations, duplicatedExogenousConfigurations);
  }
}
