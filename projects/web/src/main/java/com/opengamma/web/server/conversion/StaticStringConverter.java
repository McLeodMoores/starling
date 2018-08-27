/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Converter that always returns a fixed string.
 */
public class StaticStringConverter implements ResultConverter<Object> {

  private final String _result;

  public StaticStringConverter(final String result) {
    _result = result;
  }

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final Object value, final ConversionMode mode) {
    return _result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final Object value) {
    return _result;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final Object value) {
    return _result;
  }

  @Override
  public String getFormatterName() {
    return "PRIMITIVE";
  }

}
