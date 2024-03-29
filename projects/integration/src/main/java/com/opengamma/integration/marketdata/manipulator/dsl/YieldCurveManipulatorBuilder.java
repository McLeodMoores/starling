/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 * Collects actions to manipulate a curve and adds them to a scenario.
 */
public class YieldCurveManipulatorBuilder {

  /** Selector whose selected items will be modified by the manipulators from this builder. */
  private final YieldCurveSelector _selector;
  /** The scenario to which manipulations are added. */
  private final Scenario _scenario;

  /* package */ YieldCurveManipulatorBuilder(final YieldCurveSelector selector, final Scenario scenario) {
    ArgumentChecker.notNull(selector, "selector");
    ArgumentChecker.notNull(scenario, "scenario");
    _selector = selector;
    _scenario = scenario;
  }

  /**
   * @return the configured selector
   */
  public YieldCurveSelector getSelector() {
    return _selector;
  }

  /**
   * @return the configured scenario
   */
  public Scenario getScenario() {
    return _scenario;
  }

  /**
   * Adds an action to perform a parallel shift to the scenario.
   * 
   * @param shiftType
   *          Specifies how to apply the shift. A relative shift is expressed as an amount to add or subtract, e.g. 10% shift = rate * 1.1, -20% shift = rate *
   *          0.8
   * @param shift
   *          The size of the shift
   * @return This builder
   */
  public YieldCurveManipulatorBuilder parallelShift(final ScenarioShiftType shiftType, final Number shift) {
    _scenario.add(_selector, new YieldCurveParallelShift(shiftType, shift.doubleValue()));
    return this;
  }

  /**
   * Adds an action to perform a parallel shift to the scenario.
   * 
   * @param shift
   *          The size of the shift
   * @return This builder
   * @deprecated Use {@link #parallelShift(ScenarioShiftType, Number)}
   */
  @Deprecated
  public YieldCurveManipulatorBuilder parallelShift(final Number shift) {
    _scenario.add(_selector, new YieldCurveParallelShift(ScenarioShiftType.ABSOLUTE, shift.doubleValue()));
    return this;
  }

  /**
   * Shifts the curve using {@link com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve#withSingleShift}.
   *
   * @param t
   *          The time.
   * @param shift
   *          The shift amount.
   * @return This builder
   */
  public YieldCurveManipulatorBuilder singleShift(final Number t, final Number shift) {
    _scenario.add(_selector, new YieldCurveSingleShift(t.doubleValue(), shift.doubleValue()));
    return this;
  }

  /**
   * Creates a bucketed shift builder with the given type. This is only for the benefit of the Java API, not the DSL
   *
   * @param shiftType
   *          The type of shift
   * @param shifts
   *          the shifts
   * @return the bucketed shift builder
   */
  public final YieldCurveManipulatorBuilder bucketedShifts(final ScenarioShiftType shiftType, final YieldCurveBucketedShift... shifts) {
    ArgumentChecker.notNull(shiftType, "shiftType");
    ArgumentChecker.notEmpty(shifts, "shifts");
    final YieldCurveBucketedShiftManipulator manipulator = new YieldCurveBucketedShiftManipulator(shiftType, Arrays.asList(shifts));
    _scenario.add(_selector, manipulator);
    return this;
  }

  /**
   * Creates a point shift builder. This is only for the benefit of the Java API, not the DSL.
   *
   * @param shiftType
   *          The type of shift
   * @param shifts
   *          the shifts
   * @return the point shifts builder
   */
  public final YieldCurveManipulatorBuilder pointShifts(final ScenarioShiftType shiftType, final YieldCurvePointShift... shifts) {
    ArgumentChecker.notNull(shiftType, "shiftType");
    ArgumentChecker.notEmpty(shifts, "shifts");
    final YieldCurvePointShiftManipulator manipulator = new YieldCurvePointShiftManipulator(shiftType, Arrays.asList(shifts));
    _scenario.add(_selector, manipulator);
    return this;
  }

}
