/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceConstantMultiplicativeShift;
import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceIndexShifts;
import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceMultipleAdditiveShifts;
import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceMultipleMultiplicativeShifts;
import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceParallelShift;
import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceSingleAdditiveShift;
import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceSingleMultiplicativeShift;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class VolatilitySurfaceManipulatorBuilder {

  /** Selector whose selected items will be modified by the manipulators from this builder. */
  private final VolatilitySurfaceSelector _selector;

  /** The scenario to which manipulations are added. */
  private final Scenario _scenario;

  /* package */ VolatilitySurfaceManipulatorBuilder(final Scenario scenario, final VolatilitySurfaceSelector selector) {
    ArgumentChecker.notNull(scenario, "scenario");
    ArgumentChecker.notNull(selector, "selector");
    _scenario = scenario;
    _selector = selector;
  }

  public VolatilitySurfaceManipulatorBuilder shifts(final ScenarioShiftType shiftType, final VolatilitySurfaceShift... shifts) {
    _scenario.add(_selector, VolatilitySurfaceShiftManipulator.create(shiftType, Arrays.asList(shifts)));
    return this;
  }

  public VolatilitySurfaceManipulatorBuilder parallelShift(final ScenarioShiftType shiftType, final Number shift) {
    if (shiftType == ScenarioShiftType.ABSOLUTE) {
      _scenario.add(_selector, new VolatilitySurfaceParallelShift(shift.doubleValue()));
    } else {
      _scenario.add(_selector, new VolatilitySurfaceConstantMultiplicativeShift(shift.doubleValue() + 1));
    }
    return this;
  }

  /**
   * Creates {@link VolatilitySurfaceIndexShifts} which specifies surface shifts by expiry index.
   *
   * @param shiftType absolute or relative
   * @param shifts the shift amounts to apply at each point on the surface
   * @return this builder
   */
  public VolatilitySurfaceManipulatorBuilder indexShifts(final ScenarioShiftType shiftType, final Number... shifts) {
    final List<Double> shiftList = new ArrayList<>(shifts.length);
    for (final Number shift : shifts) {
      shiftList.add(shift.doubleValue());
    }
    _scenario.add(_selector, new VolatilitySurfaceIndexShifts(shiftType, shiftList));
    return this;
  }

  /**
   * @param shift
   *          the shift
   * @return the builder
   * @deprecated Use {@link #parallelShift(ScenarioShiftType, Number)} with {@link ScenarioShiftType#ABSOLUTE}
   */
  @Deprecated
  public VolatilitySurfaceManipulatorBuilder parallelShift(final Number shift) {
    _scenario.add(_selector, new VolatilitySurfaceParallelShift(shift.doubleValue()));
    return this;
  }

  /**
   * @param shift
   *          the shift
   * @return the builder
   * @deprecated Use {@link #parallelShift(ScenarioShiftType, Number)} with {@link ScenarioShiftType#RELATIVE}
   */
  @Deprecated
  public VolatilitySurfaceManipulatorBuilder constantMultiplicativeShift(final Number shift) {
    _scenario.add(_selector, new VolatilitySurfaceConstantMultiplicativeShift(shift.doubleValue()));
    return this;
  }

  /**
   * @param x
   *          the x co-ordinate of the value to shift
   * @param y
   *          the y co-ordinate of the value to shift
   * @param shift
   *          the shift
   * @return the builder
   * @deprecated Use {@link #shifts(ScenarioShiftType, VolatilitySurfaceShift...)} with {@link ScenarioShiftType#ABSOLUTE} and one shift
   */
  @Deprecated
  public VolatilitySurfaceManipulatorBuilder singleAdditiveShift(final Number x, final Number y, final Number shift) {
    _scenario.add(_selector, new VolatilitySurfaceSingleAdditiveShift(x.doubleValue(), y.doubleValue(), shift.doubleValue()));
    return this;
  }

  /**
   * @param x
   *          the x co-ordinates of the values to shift
   * @param y
   *          the y co-ordinates of the values to shift
   * @param shifts
   *          the shifts
   * @return the builder
   * @deprecated Use {@link #shifts(ScenarioShiftType, VolatilitySurfaceShift...)} with {@link ScenarioShiftType#ABSOLUTE} and multiple shifts
   */
  @Deprecated
  public VolatilitySurfaceManipulatorBuilder multipleAdditiveShifts(final List<Number> x, final List<Number> y, final List<Number> shifts) {
    _scenario.add(_selector, new VolatilitySurfaceMultipleAdditiveShifts(array(x), array(y), array(shifts)));
    return this;
  }

  /**
   * @param x
   *          the x co-ordinate of the value to shift
   * @param y
   *          the y co-ordinate of the value to shift
   * @param shift
   *          the shift
   * @return the builder
   * @deprecated Use {@link #shifts(ScenarioShiftType, VolatilitySurfaceShift...)} with {@link ScenarioShiftType#RELATIVE} and one shift
   */
  @Deprecated
  public VolatilitySurfaceManipulatorBuilder singleMultiplicativeShift(final Number x, final Number y, final Number shift) {
    _scenario.add(_selector, new VolatilitySurfaceSingleMultiplicativeShift(x.doubleValue(), y.doubleValue(), shift.doubleValue()));
    return this;
  }

  /**
   * @param x
   *          the x co-ordinates of the value to shift
   * @param y
   *          the y co-ordinates of the value to shift
   * @param shifts
   *          the shifts
   * @return the builder
   * @deprecated Use {@link #shifts(ScenarioShiftType, VolatilitySurfaceShift...)} with {@link ScenarioShiftType#RELATIVE} and multiple shifts
   */
  @Deprecated
  public VolatilitySurfaceManipulatorBuilder multipleMultiplicativeShifts(final List<Number> x, final List<Number> y, final List<Number> shifts) {
    _scenario.add(_selector, new VolatilitySurfaceMultipleMultiplicativeShifts(array(x), array(y), array(shifts)));
    return this;
  }

  private static double[] array(final List<Number> list) {
    final double[] array = new double[list.size()];
    int index = 0;
    for (final Number value : list) {
      array[index++] = value.doubleValue();
    }
    return array;
  }

  /* package */ VolatilitySurfaceSelector getSelector() {
    return _selector;
  }

  /* package */ Scenario getScenario() {
    return _scenario;
  }
}
