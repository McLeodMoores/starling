/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.web.analytics.formatting;

import java.util.ArrayList;
import java.util.List;

import net.finmath.marketdata.model.curves.DiscountCurve;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.analytics.formatting.AbstractFormatter;
import com.opengamma.web.analytics.formatting.DataType;

/**
 *
 */
/* package */ class DiscountCurveFormatter extends AbstractFormatter<DiscountCurve> {

  /* package */ DiscountCurveFormatter() {
    super(DiscountCurve.class);
    addFormatter(new Formatter<DiscountCurve>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final DiscountCurve value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatCell(value, valueSpec, inlineKey);
      }
    });
  }

  @Override
  public List<Double[]> formatCell(final DiscountCurve value, final ValueSpecification valueSpec, final Object inlineKey) {
    final int n = 34;
    final List<Double[]> data = new ArrayList<>();
    final double[] xData = new double[n];
    final double[] yData = new double[n];
    for (int i = 0; i < n; i++) {
      if (i == 0) {
        xData[0] = 1. / 12;
      } else if (i == 1) {
        xData[1] = 0.25;
      } else if (i == 2) {
        xData[2] = 0.5;
      } else if (i == 3) {
        xData[3] = 0.75;
      } else {
        xData[i] = i - 3;
      }
      yData[i] = value.getZeroRate(xData[i]);
      data.add(new Double[]{xData[i], yData[i]});
    }
    return data;
  }

  @Override
  public DataType getDataType() {
    return DataType.CURVE;
  }

}
