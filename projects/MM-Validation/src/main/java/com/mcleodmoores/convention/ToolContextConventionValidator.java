/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.convention;

import com.opengamma.core.convention.ConventionSource;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Wrapper for a convention validator that gets a {@link ConventionSource} from a tool context.
 * @param <U>  the type of the object to be validated
 * @param <V>  the type of the conventions that have been validated
 */
public final class ToolContextConventionValidator<U, V> {
  /** The underlying validator */
  private final ConventionValidator<U, V> _validator;

  /**
   * Gets a validator.
   * @param validator  the underlying validator, not null
   * @return  the validator
   * @param <U>  the type of the object to be validated
   * @param <V>  the type of the conventions that have been validated
   */
  public static <U, V> ToolContextConventionValidator<U, V> of(final ConventionValidator<U, V> validator) {
    ArgumentChecker.notNull(validator, "validator");
    return new ToolContextConventionValidator<>(validator);
  }

  /**
   * Restricted constructor.
   * @param validator  the underlying validator
   */
  private ToolContextConventionValidator(final ConventionValidator<U, V> validator) {
    _validator = validator;
  }

  /**
   * Validates the latest version of the conventions referenced in the object.
   * @param object  the object to be validated, not null
   * @param toolContext  the tool context, not null
   * @return  the validation information
   */
  public ConventionValidationInfo<V> validate(final U object, final ToolContext toolContext) {
    return validate(object, VersionCorrection.LATEST, toolContext);
  }

  /**
   * Validates the conventions referenced in the object.
   * @param object  the object to be validated, not null
   * @param versionCorrection  the version, not null
   * @param toolContext  the tool context, not null
   * @return  the validation information
   */
  public ConventionValidationInfo<V> validate(final U object, final VersionCorrection versionCorrection,
      final ToolContext toolContext) {
    ArgumentChecker.notNull(object, "object");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(toolContext, "toolContext");
    final ConventionSource conventionSource = toolContext.getConventionSource();
    if (conventionSource == null) {
      throw new IllegalStateException("ConventionSource not set in tool context");
    }
    return _validator.validate(object, versionCorrection, conventionSource);
  }

}
