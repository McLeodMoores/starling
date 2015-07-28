/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.convention;

import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class ConventionValidator<U, V extends Convention> {


  public ConventionValidationInfo<V> validate(final U configuration, final FunctionCompilationContext compilationContext) {
    return validate(configuration, VersionCorrection.LATEST, compilationContext);
  }

  public ConventionValidationInfo<V> validate(final U configuration, final ToolContext toolContext) {
    return validate(configuration, VersionCorrection.LATEST, toolContext);
  }

  public ConventionValidationInfo<V> validate(final U curveConstructionConfiguration, final VersionCorrection versionCorrection,
      final FunctionCompilationContext compilationContext) {
    ArgumentChecker.notNull(curveConstructionConfiguration, "curveConstructionConfiguration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(compilationContext, "compilationContext");
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(compilationContext);
    if (conventionSource == null) {
      throw new IllegalStateException("ConventionSource not set in function compilation context");
    }
    return validate(curveConstructionConfiguration, versionCorrection, conventionSource);
  }

  public ConventionValidationInfo<V> validate(final U curveConstructionConfiguration, final VersionCorrection versionCorrection,
      final ToolContext toolContext) {
    ArgumentChecker.notNull(curveConstructionConfiguration, "curveConstructionConfiguration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(toolContext, "toolContext");
    final ConventionSource conventionSource = toolContext.getConventionSource();
    if (conventionSource == null) {
      throw new IllegalStateException("ConventionSource not set in tool context");
    }
    return validate(curveConstructionConfiguration, versionCorrection, conventionSource);
  }

  protected abstract ConventionValidationInfo<V> validate(final U configuration, VersionCorrection versionCorrection, ConventionSource conventionSource);
}
