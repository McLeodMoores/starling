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
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * This class checks a {@link CurveGroupConfiguration} and returns information about problems with the configuration
 * that will prevent curves from being constructed correctly. These configurations can be incorrect because:
 * <ul>
 *  <li>There is a reference to a {@link AbstractCurveDefinition} that is not present in the config source.</li>
 *  <li>There is more than one {@link AbstractCurveDefinition} with the same name in the config source.</li>
 * </ul>
 */
public final class CurveGroupValidator extends ConfigurationValidator<CurveGroupConfiguration, AbstractCurveDefinition> {
  /** A static instance */
  private static final ConfigurationValidator<CurveGroupConfiguration, AbstractCurveDefinition> INSTANCE = new CurveGroupValidator();

  /**
   * Returns a static instance.
   * @return  an instance
   */
  public static ConfigurationValidator<CurveGroupConfiguration, AbstractCurveDefinition> getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private CurveGroupValidator() {
  }

  @Override
  public ConfigurationValidationInfo<AbstractCurveDefinition> validate(final CurveGroupConfiguration curveGroupConfiguration,
      final VersionCorrection versionCorrection, final ConfigSource configSource) {
    ArgumentChecker.notNull(curveGroupConfiguration, "curveGroupConfiguration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(configSource, "configSource");
    final Collection<AbstractCurveDefinition> configurations = new HashSet<>();
    final Map<String, Class<?>> missingCurveDefinitions = new HashMap<>();
    final Collection<Object> duplicatedCurveDefinitions = new HashSet<>();
    final Map<String, List<? extends CurveTypeConfiguration>> typesForCurves = curveGroupConfiguration.getTypesForCurves();
    for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : typesForCurves.entrySet()) {
      final String definitionName = entry.getKey();
      final Collection<ConfigItem<AbstractCurveDefinition>> definitionItems = configSource.get(AbstractCurveDefinition.class,
          definitionName, versionCorrection);
      if (definitionItems.size() > 1) {
        duplicatedCurveDefinitions.addAll(definitionItems);
      } else if (definitionItems.isEmpty()) {
        missingCurveDefinitions.put(definitionName, AbstractCurveDefinition.class);
      } else {
        configurations.add(Iterables.getOnlyElement(definitionItems).getValue());
      }
    }
    return new ConfigurationValidationInfo<>(AbstractCurveDefinition.class, configurations, missingCurveDefinitions, duplicatedCurveDefinitions);
  }
}
