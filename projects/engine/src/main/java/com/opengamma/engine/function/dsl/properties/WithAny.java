/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.properties;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.ArgumentChecker;

/**
 * Adds a property name.
 */
public class WithAny extends ValuePropertiesModifier {

  /**
   * @param propertyName
   *          the property name, not null
   */
  public WithAny(final String propertyName) {
    super(propertyName);
  }

  @Override
  public ValueProperties.Builder modify(final ValueProperties.Builder builder) {
    ArgumentChecker.notNull(builder, "builder");
    return builder.withAny(getPropertyName());
  }

}
