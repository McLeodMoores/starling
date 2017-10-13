/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public interface BondCurveTypeSetUpInterface extends CurveTypeSetUpInterface {

  @Override
  BondCurveTypeSetUpInterface forDiscounting(UniqueIdentifiable id);

  @Override
  BondCurveTypeSetUpInterface forIndex(IborTypeIndex... indices);

  @Override
  BondCurveTypeSetUpInterface forIndex(OvernightIndex... indices);

  BondCurveTypeSetUpInterface forIssuer(Pair<Object, LegalEntityFilter<LegalEntity>>... issuer);

  @Override
  BondCurveTypeSetUpInterface withInterpolator(Interpolator1D interpolator);

  //TODO asSpread under to indicate subtraction?
  @Override
  BondCurveTypeSetUpInterface asSpreadOver(String otherCurveName);

  //TODO curve operations setup to allow A = B + C + D logic
  @Override
  BondCurveTypeSetUpInterface functionalForm(CurveFunction function);

  //TODO local dates would be better
  @Override

  BondCurveTypeSetUpInterface usingNodeDates(LocalDateTime... dates);
  @Override
  BondCurveTypeSetUpInterface continuousInterpolationOnYield();

  //TODO not sure about this - having an argument is inconsistent
  @Override
  BondCurveTypeSetUpInterface periodicInterpolationOnYield(int compoundingPeriodsPerYear);

  @Override
  BondCurveTypeSetUpInterface continuousInterpolationOnDiscountFactors();

  //TODO rename next 2 methods
  @Override
  BondCurveTypeSetUpInterface usingInstrumentMaturity();

  @Override
  BondCurveTypeSetUpInterface usingLastFixingEndTime();

  @Override
  GeneratorYDCurve buildCurveGenerator(ZonedDateTime valuationDate);

}
