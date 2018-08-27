/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import groovy.lang.Closure;

/**
 *
 */
/* package */ final class DslYieldCurveDataManipulatorBuilder extends YieldCurveDataManipulatorBuilder {

  /* package */ DslYieldCurveDataManipulatorBuilder(final YieldCurveDataSelector selector, final Scenario scenario) {
    super(selector, scenario);
  }

  public void bucketedShifts(final ScenarioShiftType shiftType, final Closure<?> body) {
    final YieldCurveDataBucketedShiftsManipulatorBuilder builder =
        new YieldCurveDataBucketedShiftsManipulatorBuilder(getSelector(), getScenario(), shiftType);
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    builder.build();
  }

  public void pointShifts(final ScenarioShiftType shiftType, final Closure<?> body) {
    final YieldCurveDataPointShiftsManipulatorBuilder builder =
        new YieldCurveDataPointShiftsManipulatorBuilder(getSelector(), getScenario(), shiftType);
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    builder.build();
  }

}
