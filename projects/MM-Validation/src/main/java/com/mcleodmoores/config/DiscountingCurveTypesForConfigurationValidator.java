/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * This class checks that there are no duplicated references in any of the {@link DiscountingCurveTypeConfiguration}s in the
 * configuration, including all exogenous configurations.
 */
public final class DiscountingCurveTypesForConfigurationValidator extends ConfigurationValidator<CurveConstructionConfiguration, String> {
  /** A static instance */
  private static final ConfigurationValidator<CurveConstructionConfiguration, String> INSTANCE =
      new DiscountingCurveTypesForConfigurationValidator();

  /**
   * Returns a static instance.
   * @return  an instance
   */
  public static ConfigurationValidator<CurveConstructionConfiguration, String> getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private DiscountingCurveTypesForConfigurationValidator() {
  }

  @Override
  public ConfigurationValidationInfo<String> validate(final CurveConstructionConfiguration configuration, final VersionCorrection versionCorrection,
      final ConfigSource configSource) {
    ArgumentChecker.notNull(configuration, "configuration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(configSource, "configSource");
    final Set<String> references = new HashSet<>();
    final Set<Object> duplicatedReferences = new HashSet<>();
    for (final CurveGroupConfiguration group : configuration.getCurveGroups()) {
      for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
        for (final CurveTypeConfiguration type : entry.getValue()) {
          if (type instanceof DiscountingCurveTypeConfiguration) {
            final String reference = ((DiscountingCurveTypeConfiguration) type).getReference();
            if (references.remove(reference)) {
              duplicatedReferences.add(reference);
            } else {
              if (!duplicatedReferences.contains(reference)) {
                references.add(reference);
              }
            }
          }
        }
      }
    }
    if (configuration.getExogenousConfigurations().isEmpty()) {
      return new ConfigurationValidationInfo<>(String.class, references, Collections.<String, Class<?>>emptyMap(), duplicatedReferences);
    }
    for (final String name : configuration.getExogenousConfigurations()) {
      final Collection<ConfigItem<CurveConstructionConfiguration>> exogenousConfiguration =
          configSource.get(CurveConstructionConfiguration.class, name, versionCorrection);
      if (exogenousConfiguration.size() == 1) {
        final ConfigurationValidationInfo<String> underlyingInfo =
            validate(Iterables.getOnlyElement(exogenousConfiguration).getValue(), versionCorrection, configSource);
        duplicatedReferences.addAll(underlyingInfo.getDuplicatedConfigurations());
        for (final String validatedReference : underlyingInfo.getValidatedConfigurations()) {
          if (references.remove(validatedReference)) {
            duplicatedReferences.add(validatedReference);
          } else {
            references.add(validatedReference);
          }
        }
        for (final Object duplicatedReference : duplicatedReferences) {
          references.remove(duplicatedReference);
        }
      }
    }
    return new ConfigurationValidationInfo<>(String.class, references, Collections.<String, Class<?>>emptyMap(), duplicatedReferences);
  }
}
