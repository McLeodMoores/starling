/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.properties;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.ArgumentChecker;

/**
 * Adds property values.
 */
public class With extends ValuePropertiesModifier {
  private final Stream<String> _propertyValues;

  /**
   * @param propertyName
   *          the property name, not null
   * @param propertyValues
   *          the property values
   */
  public With(final String propertyName, final String... propertyValues) {
    super(propertyName);
    _propertyValues = Stream.of(ArgumentChecker.notNull(propertyValues, "propertyValues"));
  }

  /**
   * Get the property values.
   *
   * @return the property values
   */
  public Stream<String> getPropertyValues() {
    return _propertyValues;
  }

  @Override
  public ValueProperties.Builder modify(final ValueProperties.Builder builder) {
    ArgumentChecker.notNull(builder, "builder");
    return builder.with(getPropertyName(), getPropertyValues().collect(Collectors.toList()));
  }

}
