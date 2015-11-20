/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Wrapper for a configuration validator that gets a {@link ConfigSource} from a compilation context.
 * @param <U>  the type of the configuration to be validated
 * @param <V>  the type of the configurations that have been validated
 */
public final class CompilationContextConfigurationValidator<U, V> {
  /** The underlying validator */
  private final ConfigurationValidator<U, V> _validator;

  /**
   * Gets a validator.
   * @param validator  the underlying validator, not null
   * @return  the validator
   * @param <U>  the type of the configuration to be validated
   * @param <V>  the type of the configurations that have been validated
   */
  public static <U, V> CompilationContextConfigurationValidator<U, V> of(final ConfigurationValidator<U, V> validator) {
    ArgumentChecker.notNull(validator, "validator");
    return new CompilationContextConfigurationValidator<>(validator);
  }

  /**
   * Restricted constructor.
   * @param validator  the underlying validator
   */
  private CompilationContextConfigurationValidator(final ConfigurationValidator<U, V> validator) {
    _validator = validator;
  }

  /**
   * Validates the latest version of a configuration.
   * @param configuration  the configuration, not null
   * @param compilationContext  the compilation context, not null
   * @return  the validation information
   */
  public ConfigurationValidationInfo<V> validate(final U configuration, final FunctionCompilationContext compilationContext) {
    return validate(configuration, VersionCorrection.LATEST, compilationContext);
  }

  /**
   * Validates a configuration.
   * @param configuration  the configuration, not null
   * @param versionCorrection  the configuration version, not null
   * @param compilationContext  the compilation context, not null
   * @return  the validation information
   */
  public ConfigurationValidationInfo<V> validate(final U configuration, final VersionCorrection versionCorrection,
      final FunctionCompilationContext compilationContext) {
    ArgumentChecker.notNull(configuration, "configuration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(compilationContext, "compilationContext");
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(compilationContext);
    if (configSource == null) {
      throw new IllegalStateException("ConfigSource not set in function compilation context");
    }
    return _validator.validate(configuration, versionCorrection, configSource);
  }

}
