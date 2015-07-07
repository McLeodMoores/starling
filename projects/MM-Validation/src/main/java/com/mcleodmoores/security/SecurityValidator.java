/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.security;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class SecurityValidator<U, V extends Security> {

  public SecurityValidationInfo<V> validate(final U configuration, final FunctionCompilationContext compilationContext) {
    return validate(configuration, VersionCorrection.LATEST, compilationContext);
  }

  public SecurityValidationInfo<V> validate(final U configuration, final ToolContext toolContext) {
    return validate(configuration, VersionCorrection.LATEST, toolContext);
  }

  public SecurityValidationInfo<V> validate(final U curveConstructionConfiguration, final VersionCorrection versionCorrection,
      final FunctionCompilationContext compilationContext) {
    ArgumentChecker.notNull(curveConstructionConfiguration, "curveConstructionConfiguration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(compilationContext, "compilationContext");
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(compilationContext);
    if (securitySource == null) {
      throw new IllegalStateException("SecuritySource not set in function compilation context");
    }
    return validate(curveConstructionConfiguration, versionCorrection, securitySource);
  }

  public SecurityValidationInfo<V> validate(final U curveConstructionConfiguration, final VersionCorrection versionCorrection,
      final ToolContext toolContext) {
    ArgumentChecker.notNull(curveConstructionConfiguration, "curveConstructionConfiguration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(toolContext, "toolContext");
    final SecuritySource securitySource = toolContext.getSecuritySource();
    if (securitySource == null) {
      throw new IllegalStateException("SecuritySource not set in tool context");
    }
    return validate(curveConstructionConfiguration, versionCorrection, securitySource);
  }

  protected abstract SecurityValidationInfo<V> validate(final U configuration, VersionCorrection versionCorrection, SecuritySource securitySource);
}
