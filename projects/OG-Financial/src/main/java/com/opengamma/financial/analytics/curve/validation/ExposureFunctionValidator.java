/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.curve.exposure.factory.NamedExposureFunctionFactory;
import com.opengamma.id.VersionCorrection;

public final class ExposureFunctionValidator extends ConfigurationValidator<ExposureFunctions, ExposureFunction> {
  /** A static instance */
  private static final ConfigurationValidator<ExposureFunctions, ExposureFunction> INSTANCE = new ExposureFunctionValidator();

  /**
   * Gets a static instance.
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
  protected ConfigurationValidationInfo<ExposureFunction> validate(final ExposureFunctions exposureFunctions, final VersionCorrection versionCorrection,
      final ConfigSource configSource) {
    final Collection<ExposureFunction> configurations = new HashSet<>();
    final Collection<String> missingExposureFunctions = new HashSet<>();
    final List<String> exposureFunctionNames = exposureFunctions.getExposureFunctions();
    for (final String exposureFunctionName : exposureFunctionNames) {
      try {
        configurations.add(NamedExposureFunctionFactory.of(exposureFunctionName));
      } catch (final IllegalArgumentException e) {
        missingExposureFunctions.add(exposureFunctionName);
      }
    }
    return new ConfigurationValidationInfo<>(ExposureFunction.class, configurations, missingExposureFunctions, Collections.<String>emptySet());
  }
}
