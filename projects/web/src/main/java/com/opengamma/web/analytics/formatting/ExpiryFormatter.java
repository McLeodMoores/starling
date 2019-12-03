/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Expiry;

/**
 *
 */
/* package */ class ExpiryFormatter extends AbstractFormatter<Expiry> {

  private final ZonedDateTimeFormatter _zonedDateTimeFormatter;

  /* package */ ExpiryFormatter(final ZonedDateTimeFormatter zonedDateTimeFormatter) {
    super(Expiry.class);
    _zonedDateTimeFormatter = zonedDateTimeFormatter;
    ArgumentChecker.notNull(zonedDateTimeFormatter, "zonedDateTimeFormatter");
  }

  @Override
  public Object formatCell(final Expiry value, final ValueSpecification valueSpec, final Object inlineKey) {
    return _zonedDateTimeFormatter.formatCell(value.getExpiry(), valueSpec, inlineKey);
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }
}
