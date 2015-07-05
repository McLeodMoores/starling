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

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Formatter for yield curves.
 */
class ISDACompliantYieldCurveFormatter extends AbstractFormatter<ISDACompliantYieldCurve> {

  /**
   * Creates an instance.
   */
  ISDACompliantYieldCurveFormatter() {
    super(ISDACompliantYieldCurve.class);
    addFormatter(new Formatter<ISDACompliantYieldCurve>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final ISDACompliantYieldCurve value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  public List<Double[]> formatCell(final ISDACompliantYieldCurve value, final ValueSpecification valueSpec, final Object inlineKey) {
    final List<Double[]> data = new ArrayList<>();
    final double[] xData = value.getT();
    final double[] yData = value.getKnotZeroRates();
    for (int i = 0; i < xData.length; i++) {
      data.add(new Double[] {xData[i], yData[i] });
    }
    return data;
  }

  // This should really interpolate the curve
  private List<Double[]> formatExpanded(final ISDACompliantYieldCurve value) {
    return formatCell(value, null, null);
  }

  @Override
  public DataType getDataType() {
    return DataType.CURVE;
  }

}
