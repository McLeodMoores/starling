/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import groovy.lang.Closure;

/**
 * Delegate class for closures that defines closure compatible builder methods
 * for {@link YieldCurveManipulatorBuilder} in the DSL.
 */
/* package */ final class DslYieldCurveManipulatorBuilder extends YieldCurveManipulatorBuilder {

  /* package */ DslYieldCurveManipulatorBuilder(final YieldCurveSelector selector, final Scenario scenario) {
    super(selector, scenario);
  }

  @SuppressWarnings("unused")
  public void bucketedShifts(final ScenarioShiftType shiftType, final Closure<?> body) {
    final BucketedShiftManipulatorBuilder builder =
        new BucketedShiftManipulatorBuilder(getSelector(), getScenario(), shiftType);
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    builder.build();
  }

  @SuppressWarnings("unused")
  public void pointShifts(final ScenarioShiftType shiftType, final Closure<?> body) {
    final YieldCurvePointShiftManipulatorBuilder builder = new YieldCurvePointShiftManipulatorBuilder(getSelector(), getScenario(), shiftType);
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    builder.build();
  }

}
