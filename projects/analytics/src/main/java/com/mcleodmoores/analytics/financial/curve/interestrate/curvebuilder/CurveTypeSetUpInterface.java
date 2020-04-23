/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.id.UniqueIdentifiable;

/**
 * An interface for builders that describe how a curve is to be constructed. This class defines the uses for a curve (e.g. discounting curves of a particular
 * currency, calculating forward overnight rates), its form (interpolated, functional) and how the node times are to be calculated.
 *
 * The available uses for the curve are:
 * <ul>
 * <li>Discounting all payments with a particular identifier, e.g. a currency</li>
 * <li>Calculating forward *IBOR rates</li>
 * <li>Calculating forward overnight rates</li>
 * </ul>
 * Each curve can be used for any or all of these options and can be used for multiple indices (e.g. use one curve for 3M and 6M *IBOR).
 *
 * The available forms of the curve are:
 * <ul>
 * <li>Interpolate on continuous yields</li>
 * <li>Interpolate on periodic yields</li>
 * <li>Interpolate on continuous discount factors</li>
 * <li>Functional e.g. Nelson-Siegel</li>
 * </ul>
 *
 * It is also possible to set the node time calculation type:
 * <ul>
 * <li>Curve instrument maturity</li>
 * <li>End time of the last fixing period</li>
 * <li>Use a particular set of dates. This allows nodes to be on ECB meeting dates, for example.
 * </ul>
 *
 * Implementing classes should probably use covariant return types. See implementing classes' documentation for example configurations.
 */
public interface CurveTypeSetUpInterface {

  /**
   * Functional forms for curves.
   */
  enum CurveFunction {
    NELSON_SIEGEL
  }

  /**
   * Use this curve to discount payments with this identifier.
   *
   * @param id
   *          the identifier, not null
   * @return this builder
   */
  CurveTypeSetUpInterface forDiscounting(UniqueIdentifiable id);

  /**
   * Use this curve for calculate forward *IBOR rates for these indices.
   *
   * @param indices
   *          the indices, not null
   * @return this builder
   */
  CurveTypeSetUpInterface forIndex(IborTypeIndex... indices);

  /**
   * Use this curve for calculate forward overnight rates for these indices.
   *
   * @param indices
   *          the indices, not null
   * @return this builder
   */
  CurveTypeSetUpInterface forIndex(OvernightIndex... indices);

  /**
   * Use this interpolator.
   *
   * @param interpolator
   *          the interpolator, not null
   * @return this builder
   */
  CurveTypeSetUpInterface withInterpolator(Interpolator1D interpolator);

  /**
   * This curve is a spread over this base curve.
   *
   * @param baseCurveName
   *          the base curve name, not null
   * @return this builder
   */
  CurveTypeSetUpInterface asSpreadOver(String baseCurveName);

  /**
   * Use this functional form for this curve.
   *
   * @param function
   *          the functional form, not null
   * @return this builder
   */
  CurveTypeSetUpInterface functionalForm(CurveFunction function);

  /**
   * Use these node dates.
   *
   * @param dates
   *          the dates, not null
   * @return this builder
   */
  CurveTypeSetUpInterface usingNodeDates(LocalDateTime... dates);

  /**
   * Interpolate on continuous yields.
   *
   * @return this builder
   */
  CurveTypeSetUpInterface continuousInterpolationOnYield();

  /**
   * Interpolate on periodic yields.
   *
   * @param compoundingPeriodsPerYear
   *          the number of periods per year
   * @return this builder
   */
  CurveTypeSetUpInterface periodicInterpolationOnYield(int compoundingPeriodsPerYear);

  /**
   * Interpolate on continuous discount factors.
   *
   * @return this builder
   */
  CurveTypeSetUpInterface continuousInterpolationOnDiscountFactors();

  /**
   * Use the curve instrument maturities to calculate the node times.
   *
   * @return this builder
   */
  CurveTypeSetUpInterface usingInstrumentMaturity();

  /**
   * Use the end dates of the last fixing periods to calculate the node times.
   *
   * @return this builder
   */
  CurveTypeSetUpInterface usingLastFixingEndTime();

  /**
   * Builds the curve generator.
   * 
   * @param valuationDate
   *          the valuation date, not null
   * @return a curve generator
   */
  GeneratorYDCurve buildCurveGenerator(ZonedDateTime valuationDate);

  /**
   * Gets the node time calculator.
   * 
   * @return the node time calculator
   */
  InstrumentDerivativeVisitor<Object, Double> getNodeTimeCalculator();

}

// TODO asSpread under to indicate subtraction?
// TODO curve operations setup to allow A = B + C + D logic
