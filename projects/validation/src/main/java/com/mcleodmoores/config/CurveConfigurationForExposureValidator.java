/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * This class checks a {@link ExposureFunctions} and returns information about problems with the configuration that
 * will prevent instruments from being priced correctly. The problems that will be identified are:
 * <ul>
 *  <li>There is a reference to a {@link CurveConstructionConfiguration} that is not present in the config source.</li>
 *  <li>There is more than one {@link CurveConstructionConfiguration} with the same name in the config source.<li>
 * </ul>
 */
public final class CurveConfigurationForExposureValidator extends ConfigurationValidator<ExposureFunctions, CurveConstructionConfiguration> {
  /** A static instance */
  private static final ConfigurationValidator<ExposureFunctions, CurveConstructionConfiguration> INSTANCE = new CurveConfigurationForExposureValidator();

  /**
   * Returns a static instance.
   * @return  a static instance
   */
  public static ConfigurationValidator<ExposureFunctions, CurveConstructionConfiguration> getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private CurveConfigurationForExposureValidator() {
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
  public ConfigurationValidationInfo<CurveConstructionConfiguration> validate(final ExposureFunctions exposureFunctions,
      final VersionCorrection versionCorrection, final ConfigSource configSource) {
    ArgumentChecker.notNull(exposureFunctions, "exposureFunctions");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(configSource, "configSource");
    final Collection<CurveConstructionConfiguration> configurations = new HashSet<>();
    final Map<String, Class<?>> missingConfigurations = new HashMap<>();
    final Collection<Object> duplicatedConfigurations = new HashSet<>();
    final Map<ExternalId, String> idsToNames = exposureFunctions.getIdsToNames();
    for (final Map.Entry<ExternalId, String> entry : idsToNames.entrySet()) {
      final String cccName = entry.getValue();
      final Collection<ConfigItem<CurveConstructionConfiguration>> cccItems = configSource.get(CurveConstructionConfiguration.class, cccName,
          versionCorrection);
      if (cccItems.size() > 1) {
        duplicatedConfigurations.addAll(cccItems);
      } else if (cccItems.size() == 0) {
        missingConfigurations.put(cccName, CurveConstructionConfiguration.class);
      } else {
        configurations.add(Iterables.getOnlyElement(cccItems).getValue());
      }
    }
    return new ConfigurationValidationInfo<>(CurveConstructionConfiguration.class, configurations, missingConfigurations,
        duplicatedConfigurations);
  }
}
