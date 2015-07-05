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
 *
 */
/* package */ class DoubleObjectArrayFormatter extends AbstractFormatter<Double[][]> {

  /* package */ DoubleObjectArrayFormatter() {
    super(Double[][].class);
    addFormatter(new Formatter<Double[][]>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final Double[][] value, final ValueSpecification valueSpec, final Object inlineKey) {
        return value;
      }
    });
  }

  @Override
  public Object formatCell(final Double[][] value, final ValueSpecification valueSpec, final Object inlineKey) {
    int rowCount;
    int colCount;
    rowCount = value.length;
    if (rowCount == 0) {
      colCount = 0;
    } else {
      colCount = value[0].length;
    }
    return "Matrix (" + rowCount + " x " + colCount + ")";
  }

  @Override
  public DataType getDataType() {
    return DataType.MATRIX_2D;
  }
}
