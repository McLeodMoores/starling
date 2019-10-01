/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.properties;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.ArgumentChecker;

/**
 * Adds an optional property name.
 */
public class WithOptional extends ValuePropertiesModifier {

  /**
   * @param propertyName
   *          the property name, not null
   */
  public WithOptional(final String propertyName) {
    super(propertyName);
  }

  @Override
  public ValueProperties.Builder modify(final ValueProperties.Builder builder) {
    ArgumentChecker.notNull(builder, "builder");
    return builder.withOptional(getPropertyName());
  }
}
