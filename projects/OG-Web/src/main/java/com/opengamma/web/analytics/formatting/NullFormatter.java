/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Formats null values for display in the client.
 */
/* package */ class NullFormatter extends AbstractFormatter<Object> {

  /* package */ NullFormatter() {
    super(Object.class);
    addFormatter(new Formatter<Object>(Format.HISTORY) {
      @Override
      protected Object formatValue(final Object value, final ValueSpecification valueSpec, final Object inlineKey) {
        return null;
      }
    });
    addFormatter(new Formatter<Object>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final Object value, final ValueSpecification valueSpec, final Object inlineKey) {
        return "";
      }
    });
  }

  @Override
  public Object formatCell(final Object value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "";
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }
}
