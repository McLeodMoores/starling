/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Converter for primitives that map directly to JSON and require no transformation.
 */
public class PrimitiveConverter implements ResultConverter<Object> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final Object value, final ConversionMode mode) {
    return value;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final Object value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final Object value) {
    return value.toString();
  }

  @Override
  public String getFormatterName() {
    return "PRIMITIVE";
  }

}
