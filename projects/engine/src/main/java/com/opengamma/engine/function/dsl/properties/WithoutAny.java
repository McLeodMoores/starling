/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.properties;

import com.opengamma.engine.value.ValueProperties;

public class WithoutAny extends ValuePropertiesModifier {

  public WithoutAny(final String propertyName) {
    super(propertyName);
  }

  @Override
  public ValueProperties.Builder modify(final ValueProperties.Builder builder) {
    return builder.withoutAny(getPropertyName());
  }

}
