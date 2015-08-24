/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.curve.exposure.factory.NamedExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.factory.NamedExposureFunctionFactory;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * This class checks a {@link ExposureFunctions} and returns information about problem with the configuration that
 * will prevent instruments from being priced correctly. This class identifies missing
 * {@link com.opengamma.financial.analytics.curve.exposure.factory.NamedExposureFunction}s that are referenced in
 * the configuration.
 */
public final class ExposureFunctionValidator extends ConfigurationValidator<ExposureFunctions, ExposureFunction> {
  /** A static instance */
  private static final ConfigurationValidator<ExposureFunctions, ExposureFunction> INSTANCE = new ExposureFunctionValidator();

  /**
   * Returns a static instance.
   * @return  a static instance
   */
  public static ConfigurationValidator<ExposureFunctions, ExposureFunction> getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private ExposureFunctionValidator() {
  }

  @Override
  public ConfigurationValidationInfo<ExposureFunction> validate(final ExposureFunctions exposureFunctions, final VersionCorrection versionCorrection,
      final ConfigSource configSource) {
    ArgumentChecker.notNull(exposureFunctions, "exposureFunctions");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(configSource, "configSource");
    final Collection<ExposureFunction> configurations = new HashSet<>();
    final Map<String, Class<?>> missingExposureFunctions = new HashMap<>();
    for (final String exposureFunctionName : exposureFunctions.getExposureFunctions()) {
      try {
        configurations.add(NamedExposureFunctionFactory.of(exposureFunctionName));
      } catch (final IllegalArgumentException e) {
        missingExposureFunctions.put(exposureFunctionName, NamedExposureFunction.class);
      }
    }
    return new ConfigurationValidationInfo<>(ExposureFunction.class, configurations, missingExposureFunctions, Collections.<Object>emptySet());
  }
}
