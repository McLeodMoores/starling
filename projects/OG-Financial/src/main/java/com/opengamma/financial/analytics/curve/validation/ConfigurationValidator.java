/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.validation;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class ConfigurationValidator<U, V> {

  public ConfigurationValidationInfo<V> validate(final U configuration, final FunctionCompilationContext compilationContext) {
    return validate(configuration, VersionCorrection.LATEST, compilationContext);
  }

  public ConfigurationValidationInfo<V> validate(final U configuration, final ToolContext toolContext) {
    return validate(configuration, VersionCorrection.LATEST, toolContext);
  }

  public ConfigurationValidationInfo<V> validate(final U curveConstructionConfiguration, final VersionCorrection versionCorrection,
      final FunctionCompilationContext compilationContext) {
    ArgumentChecker.notNull(curveConstructionConfiguration, "curveConstructionConfiguration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(compilationContext, "compilationContext");
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(compilationContext);
    if (configSource == null) {
      throw new IllegalStateException("ConfigSource not set in function compilation context");
    }
    return validate(curveConstructionConfiguration, versionCorrection, configSource);
  }

  public ConfigurationValidationInfo<V> validate(final U curveConstructionConfiguration, final VersionCorrection versionCorrection,
      final ToolContext toolContext) {
    ArgumentChecker.notNull(curveConstructionConfiguration, "curveConstructionConfiguration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(toolContext, "toolContext");
    final ConfigSource configSource = toolContext.getConfigSource();
    if (configSource == null) {
      throw new IllegalStateException("ConfigSource not set in tool context");
    }
    return validate(curveConstructionConfiguration, versionCorrection, configSource);
  }

  protected abstract ConfigurationValidationInfo<V> validate(U configuration, VersionCorrection versionCorrection, ConfigSource configSource);
}
