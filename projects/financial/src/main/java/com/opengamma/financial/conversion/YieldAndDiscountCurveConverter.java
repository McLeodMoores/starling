/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;

/**
 *
 */
public class YieldAndDiscountCurveConverter implements ResultConverter<YieldAndDiscountCurve> {

  @Override
  public Map<String, Double> convert(final String valueName, final YieldAndDiscountCurve value) {
    if (!(value instanceof YieldCurve)) { //TODO: make it more generic
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }
    final Map<String, Double> returnValue = new HashMap<>();

    for (final Double x : ((YieldCurve) value).getCurve().getXData()) {
      final Double y = ((YieldCurve) value).getCurve().getYValue(x);
      returnValue.put(valueName + "[" + x + "]", y);
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return YieldAndDiscountCurve.class;
  }

}
