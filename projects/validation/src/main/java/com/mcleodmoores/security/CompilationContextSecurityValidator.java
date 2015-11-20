/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.security;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Wrapper for a security validator that gets a {@link SecuritySource} from a compilation context.
 * @param <U>  the type of the object to be validated
 * @param <V>  the type of the securities that have been validated
 */
public final class CompilationContextSecurityValidator<U, V> {
  /** The underlying validator */
  private final SecurityValidator<U, V> _validator;

  /**
   * Gets a validator.
   * @param validator  the underlying validator, not null
   * @return  the validator
   * @param <U>  the type of the object to be validated
   * @param <V>  the type of the securities that have been validated
   */
  public static <U, V> CompilationContextSecurityValidator<U, V> of(final SecurityValidator<U, V> validator) {
    ArgumentChecker.notNull(validator, "validator");
    return new CompilationContextSecurityValidator<>(validator);
  }

  /**
   * Restricted constructor.
   * @param validator  the underlying validator
   */
  private CompilationContextSecurityValidator(final SecurityValidator<U, V> validator) {
    _validator = validator;
  }

  /**
   * Validates the latest version of the securities referenced in the object.
   * @param object  the object to be validated, not null
   * @param compilationContext  the compilation context, not null
   * @return  the validation information
   */
  public SecurityValidationInfo<V> validate(final U object, final FunctionCompilationContext compilationContext) {
    return validate(object, VersionCorrection.LATEST, compilationContext);
  }

  /**
   * Validates the securities referenced in the object.
   * @param object  the object to be validated, not null
   * @param versionCorrection  the version, not null
   * @param compilationContext  the compilation context, not null
   * @return  the validation information
   */
  public SecurityValidationInfo<V> validate(final U object, final VersionCorrection versionCorrection,
      final FunctionCompilationContext compilationContext) {
    ArgumentChecker.notNull(object, "object");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(compilationContext, "compilationContext");
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(compilationContext);
    if (securitySource == null) {
      throw new IllegalStateException("SecuritySource not set in tool context");
    }
    return _validator.validate(object, versionCorrection, securitySource);
  }

}
