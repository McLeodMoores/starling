/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
public interface CurveTypeSetUpInterface {

  public enum CurveFunction {
    NELSON_SIEGEL
  }

  CurveTypeSetUpInterface forDiscounting(UniqueIdentifiable id);

  //TODO versions that only take a single index
  CurveTypeSetUpInterface forIndex(IborTypeIndex... indices);

  CurveTypeSetUpInterface forIndex(OvernightIndex... indices);

  CurveTypeSetUpInterface withInterpolator(Interpolator1D interpolator);

  //TODO asSpread under to indicate subtraction?
  CurveTypeSetUpInterface asSpreadOver(String baseCurveName);

  //TODO curve operations setup to allow A = B + C + D logic
  CurveTypeSetUpInterface functionalForm(CurveFunction function);

  CurveTypeSetUpInterface usingNodeDates(LocalDateTime... dates);

  CurveTypeSetUpInterface continuousInterpolationOnYield();

  //TODO not sure about this - having an argument is inconsistent
  CurveTypeSetUpInterface periodicInterpolationOnYield(int compoundingPeriodsPerYear);

  CurveTypeSetUpInterface continuousInterpolationOnDiscountFactors();

  //TODO rename next 2 methods
  CurveTypeSetUpInterface usingInstrumentMaturity();

  CurveTypeSetUpInterface usingLastFixingEndTime();

  GeneratorYDCurve buildCurveGenerator(ZonedDateTime valuationDate);

}
