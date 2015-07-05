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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Formatter.
 */
@SuppressWarnings("rawtypes")
/* package */ class ListDoubleArrayFormatter extends AbstractFormatter<List> {

  private static final Logger s_logger = LoggerFactory.getLogger(ListDoubleArrayFormatter.class);

  /* package */ ListDoubleArrayFormatter() {
    super(List.class);
    addFormatter(new Formatter<List>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final List value, final ValueSpecification valueSpec, final Object inlineKey) {
        return value;
      }
    });
  }

  @Override
  public Object formatCell(final List value, final ValueSpecification valueSpec, final Object inlineKey) {
    final int rowCount = value.size();
    int colCount;
    if (rowCount == 0) {
      colCount = 0;
    } else {
      if (value.get(0).getClass().equals(double[].class)) {
        colCount = ((double[]) value.get(0)).length;
      } else if (value.get(0).getClass().equals(Double[].class)) {
        colCount = ((Double[]) value.get(0)).length;
      } else {
        s_logger.warn("Unexpected type in list: {}", value.get(0).getClass());
        return FORMATTING_ERROR;
      }
    }
    return "Matrix (" + rowCount + " x " + colCount + ")";
  }

  @Override
  public DataType getDataType() {
    return DataType.MATRIX_2D;
  }

}
