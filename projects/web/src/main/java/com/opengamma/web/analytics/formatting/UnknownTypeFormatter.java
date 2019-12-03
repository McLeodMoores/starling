/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Formatter for columns whose type is unknown.
 */
/* package */ class UnknownTypeFormatter extends DefaultFormatter {

  private static final Logger LOGGER = LoggerFactory.getLogger(UnknownTypeFormatter.class);

  @Override
  public String formatCell(final Object value, final ValueSpecification valueSpec, final Object inlineKey) {
    logType(value, valueSpec);
    return super.formatCell(value, valueSpec, inlineKey);
  }

  @Override
  public Object format(final Object value, final ValueSpecification valueSpec, final Format format, final Object inlineKey) {
    logType(value, valueSpec);
    return super.format(value, valueSpec, format, inlineKey);
  }

  private static void logType(final Object value, final ValueSpecification valueSpec) {
    final String typeName = value == null ? null : value.getClass().getName();
    LOGGER.info("Value received for unknown type, value name: {}, type: {}", valueSpec.getValueName(), typeName);
  }

  @Override
  public DataType getDataType() {
    return DataType.UNKNOWN;
  }
}
