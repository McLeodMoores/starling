/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.id.VersionCorrection;

/**
 *
 */
public final class CurveGroupValidator extends ConfigurationValidator<CurveGroupConfiguration, AbstractCurveDefinition> {
  private static final ConfigurationValidator<CurveGroupConfiguration, AbstractCurveDefinition> INSTANCE = new CurveGroupValidator();

  public static ConfigurationValidator<CurveGroupConfiguration, AbstractCurveDefinition> getInstance() {
    return INSTANCE;
  }

  private CurveGroupValidator() {
  }

  @Override
  protected ConfigurationValidationInfo<AbstractCurveDefinition> validate(final CurveGroupConfiguration curveGroupConfiguration, final VersionCorrection versionCorrection,
      final ConfigSource configSource) {
    final Collection<AbstractCurveDefinition> configurations = new HashSet<>();
    final Collection<String> missingCurveDefinitions = new HashSet<>();
    final Collection<String> duplicatedCurveDefinitions = new HashSet<>();
    final Map<String, List<? extends CurveTypeConfiguration>> typesForCurves = curveGroupConfiguration.getTypesForCurves();
    for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : typesForCurves.entrySet()) {
      final String definitionName = entry.getKey();
      final Collection<ConfigItem<AbstractCurveDefinition>> definitionItems = configSource.get(AbstractCurveDefinition.class,
          definitionName, versionCorrection);
      if (definitionItems.size() > 1) {
        duplicatedCurveDefinitions.add(definitionName);
      } else if (definitionItems.isEmpty()) {
        missingCurveDefinitions.add(definitionName);
      } else {
        configurations.add(Iterables.getOnlyElement(definitionItems).getValue());
      }
    }
    return new ConfigurationValidationInfo<>(AbstractCurveDefinition.class, configurations, missingCurveDefinitions, duplicatedCurveDefinitions);
  }
}
