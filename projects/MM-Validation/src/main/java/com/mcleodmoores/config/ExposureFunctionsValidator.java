/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.curve.exposure.factory.NamedExposureFunctionFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;

/**
 * This class checks {@link ExposureFunctions} and returns information about missing or otherwise incorrect configurations
 * in the config source that will prevent curves from being constructed correctly. These configurations can be incorrect
 * because:
 * <ul>
 *  <li>The list of exposure function names contains a value that is not recognised by the {@link NamedExposureFunctionFactory}.</li>
 *  <li>There is a reference to a {@link CurveConstructionConfiguration} that is not present in the config source.</li>
 *  <li>There is more than one {@link CurveConstructionConfiguration} with the same name in the config source.<li>
 * </ul>
 */
public final class ExposureFunctionsValidator extends ConfigurationValidator<ExposureFunctions, CurveConstructionConfiguration> {
  /** A static instance */
  private static final ConfigurationValidator<ExposureFunctions, CurveConstructionConfiguration> INSTANCE = new ExposureFunctionsValidator();

  /**
   * Gets a static instance.
   * @return  a static instance
   */
  public static ConfigurationValidator<ExposureFunctions, CurveConstructionConfiguration> getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private ExposureFunctionsValidator() {
  }

  /**
   * Returns an object that contains information about missing or duplicated configurations referenced by the exposure
   * function.
   * @param exposureFunctions  the exposure functions, not null
   * @param versionCorrection  the version correction, not null
   * @param configSource  the config source, not null
   * @return  an object containing information about missing or duplicated configurations
   */
  @Override
  protected ConfigurationValidationInfo<CurveConstructionConfiguration> validate(final ExposureFunctions exposureFunctions, final VersionCorrection versionCorrection,
      final ConfigSource configSource) {
    final Collection<CurveConstructionConfiguration> configurations = new HashSet<>();
    final Collection<String> missingExposureFunctions = new HashSet<>();
    final Collection<String> missingCurveConstructionConfigurations = new HashSet<>();
    final Collection<String> duplicatedCurveConstructionConfigurations = new HashSet<>();
    final List<String> exposureFunctionNames = exposureFunctions.getExposureFunctions();
    for (final String exposureFunctionName : exposureFunctionNames) {
      try {
        NamedExposureFunctionFactory.of(exposureFunctionName);
      } catch (final IllegalArgumentException e) {
        missingExposureFunctions.add(exposureFunctionName);
      }
    }
    final Map<ExternalId, String> idsToNames = exposureFunctions.getIdsToNames();
    for (final Map.Entry<ExternalId, String> entry : idsToNames.entrySet()) {
      final String cccName = entry.getValue();
      final Collection<ConfigItem<CurveConstructionConfiguration>> cccItems = configSource.get(CurveConstructionConfiguration.class, cccName,
          versionCorrection);
      if (cccItems.size() > 1) {
        duplicatedCurveConstructionConfigurations.add(cccName);
      } else if (cccItems.size() == 0) {
        missingCurveConstructionConfigurations.add(cccName);
      } else {
        configurations.add(Iterables.getOnlyElement(cccItems).getValue());
      }
    }
    return new ConfigurationValidationInfo<>(CurveConstructionConfiguration.class, configurations, missingCurveConstructionConfigurations, duplicatedCurveConstructionConfigurations);
  }
}
