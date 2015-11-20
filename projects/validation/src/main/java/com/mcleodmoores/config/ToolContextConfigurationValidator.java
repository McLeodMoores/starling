/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.config;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Wrapper for a configuration validator that gets a {@link ConfigSource} from a tool context.
 * @param <U>  the type of the configuration to be validated
 * @param <V>  the type of the configurations that have been validated
 */
public final class ToolContextConfigurationValidator<U, V> {
  /** The underlying validator */
  private final ConfigurationValidator<U, V> _validator;

  /**
   * Gets a validator.
   * @param validator  the underlying validator, not null
   * @return  the validator
   * @param <U>  the type of the configuration to be validated
   * @param <V>  the type of the configurations that have been validated
   */
  public static <U, V> ToolContextConfigurationValidator<U, V> of(final ConfigurationValidator<U, V> validator) {
    ArgumentChecker.notNull(validator, "validator");
    return new ToolContextConfigurationValidator<>(validator);
  }

  /**
   * Restricted constructor.
   * @param validator  the underlying validator
   */
  private ToolContextConfigurationValidator(final ConfigurationValidator<U, V> validator) {
    _validator = validator;
  }

  /**
   * Validates the latest version of a configuration.
   * @param configuration  the configuration, not null
   * @param toolContext  the tool context, not null
   * @return  the validation information
   */
  public ConfigurationValidationInfo<V> validate(final U configuration, final ToolContext toolContext) {
    return validate(configuration, VersionCorrection.LATEST, toolContext);
  }

  /**
   * Validates a configuration.
   * @param configuration  the configuration, not null
   * @param versionCorrection  the configuration version, not null
   * @param toolContext  the tool context, not null
   * @return  the validation information
   */
  public ConfigurationValidationInfo<V> validate(final U configuration, final VersionCorrection versionCorrection,
      final ToolContext toolContext) {
    ArgumentChecker.notNull(configuration, "configuration");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(toolContext, "toolContext");
    final ConfigSource configSource = toolContext.getConfigSource();
    if (configSource == null) {
      throw new IllegalStateException("ConfigSource not set in tool context");
    }
    return _validator.validate(configuration, versionCorrection, configSource);
  }
}
