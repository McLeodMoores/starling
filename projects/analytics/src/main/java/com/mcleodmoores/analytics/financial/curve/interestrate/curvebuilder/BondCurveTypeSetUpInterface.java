/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

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
 * An interface for builders that describe how a bond curve is to be constructed. In addition to the functionality from {@link CurveTypeSetUpInterface}, this
 * class defines which issuers can be priced using this curve.
 */
public interface BondCurveTypeSetUpInterface extends CurveTypeSetUpInterface {

  @Override
  BondCurveTypeSetUpInterface forDiscounting(UniqueIdentifiable id);

  @Override
  BondCurveTypeSetUpInterface forIndex(IborTypeIndex... indices);

  @Override
  BondCurveTypeSetUpInterface forIndex(OvernightIndex... indices);

  /**
   * Use this curve to price bonds with these issuers.
   *
   * @param issuer
   *          the issuers, not null
   * @return this builder
   */
  BondCurveTypeSetUpInterface forIssuer(Pair<Object, LegalEntityFilter<LegalEntity>>... issuer);

  @Override
  BondCurveTypeSetUpInterface withInterpolator(Interpolator1D interpolator);

  @Override
  BondCurveTypeSetUpInterface asSpreadOver(String otherCurveName);

  @Override
  BondCurveTypeSetUpInterface functionalForm(CurveFunction function);

  @Override
  BondCurveTypeSetUpInterface usingNodeDates(LocalDateTime... dates);

  @Override
  BondCurveTypeSetUpInterface continuousInterpolationOnYield();

  @Override
  BondCurveTypeSetUpInterface periodicInterpolationOnYield(int compoundingPeriodsPerYear);

  @Override
  BondCurveTypeSetUpInterface continuousInterpolationOnDiscountFactors();

  @Override
  BondCurveTypeSetUpInterface usingInstrumentMaturity();

  @Override
  BondCurveTypeSetUpInterface usingLastFixingEndTime();

  @Override
  GeneratorYDCurve buildCurveGenerator(ZonedDateTime valuationDate);

}
