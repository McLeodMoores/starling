/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.convention;

import com.opengamma.core.convention.ConventionSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Wrapper for a convention validator that gets a {@link ConventionSource} from a compilation context.
 * @param <U>  the type of the object to be validated
 * @param <V>  the type of the conventions that have been validated
 */
public final class CompilationContextConventionValidator<U, V> {
  /** The underlying validator */
  private final ConventionValidator<U, V> _validator;

  /**
   * Gets a validator.
   * @param validator  the underlying validator, not null
   * @return  the validator
   * @param <U>  the type of the object to be validated
   * @param <V>  the type of the conventions that have been validated
   */
  public static <U, V> CompilationContextConventionValidator<U, V> of(final ConventionValidator<U, V> validator) {
    ArgumentChecker.notNull(validator, "validator");
    return new CompilationContextConventionValidator<>(validator);
  }

  /**
   * Restricted constructor.
   * @param validator  the underlying validator
   */
  private CompilationContextConventionValidator(final ConventionValidator<U, V> validator) {
    _validator = validator;
  }

  /**
   * Validates the latest version of the conventions referenced in the object.
   * @param object  the object to be validated, not null
   * @param compilationContext  the compilation context, not null
   * @return  the validation information
   */
  public ConventionValidationInfo<V> validate(final U object, final FunctionCompilationContext compilationContext) {
    return validate(object, VersionCorrection.LATEST, compilationContext);
  }

  /**
   * Validates the conventions referenced in the object.
   * @param object  the object to be validated, not null
   * @param versionCorrection  the version, not null
   * @param compilationContext  the compilation context, not null
   * @return  the validation information
   */
  public ConventionValidationInfo<V> validate(final U object, final VersionCorrection versionCorrection,
      final FunctionCompilationContext compilationContext) {
    ArgumentChecker.notNull(object, "object");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(compilationContext, "compilationContext");
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(compilationContext);
    if (conventionSource == null) {
      throw new IllegalStateException("ConventionSource not set in tool context");
    }
    return _validator.validate(object, versionCorrection, conventionSource);
  }

}
