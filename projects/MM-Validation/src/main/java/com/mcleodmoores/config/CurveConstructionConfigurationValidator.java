/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.id.VersionCorrection;

/**
 *
 */
public final class CurveConstructionConfigurationValidator extends ConfigurationValidator<CurveConstructionConfiguration, CurveGroupConfiguration> {
  private static final ConfigurationValidator<CurveConstructionConfiguration, CurveGroupConfiguration> INSTANCE = new CurveConstructionConfigurationValidator();

  public static ConfigurationValidator<CurveConstructionConfiguration, CurveGroupConfiguration> getInstance() {
    return INSTANCE;
  }

  private CurveConstructionConfigurationValidator() {
  }

  @Override
  protected ConfigurationValidationInfo<CurveGroupConfiguration> validate(final CurveConstructionConfiguration curveConstructionConfiguration, final VersionCorrection versionCorrection,
      final ConfigSource configSource) {
    final Collection<CurveGroupConfiguration> configurations = new HashSet<>();
    for (final CurveGroupConfiguration curveGroup : curveConstructionConfiguration.getCurveGroups()) {
      configurations.add(curveGroup);
    }
    return new ConfigurationValidationInfo<>(CurveGroupConfiguration.class, configurations, Collections.<String>emptySet(), Collections.<String>emptySet());
  }
}
