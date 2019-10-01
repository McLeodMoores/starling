/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.properties;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.ArgumentChecker;

/**
 * Base class for value properties modifiers.
 */
public abstract class ValuePropertiesModifier {
  private final String _propertyName;

  /**
   * @param propertyName  the property name, not null
   */
  protected ValuePropertiesModifier(final String propertyName) {
    _propertyName = ArgumentChecker.notNull(propertyName, "propertyName");
  }

  /**
   * Gets the property name.
   * @return  the property name
   */
  public final String getPropertyName() {
    return _propertyName;
  }

  /**
   * Modifies the builder according to the type of modifier.
   * @param builder  the builder, not null
   * @return  the modified builder
   */
  public abstract ValueProperties.Builder modify(ValueProperties.Builder builder);

}
