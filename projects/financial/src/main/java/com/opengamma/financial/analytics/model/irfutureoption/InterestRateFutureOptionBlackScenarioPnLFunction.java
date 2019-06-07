/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.math.surface.SurfaceShiftFunctionFactory;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.equity.ScenarioPnLPropertyNamesAndValues;

// CSOFF
/**
 * Simple scenario Function returns the difference in PresentValue between defined Scenario and current market conditions.
 * 
 * @deprecated The parent class of this function is deprecated
 */
@Deprecated
public class InterestRateFutureOptionBlackScenarioPnLFunction extends InterestRateFutureOptionBlackFunction {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#PNL}.
   */
  public InterestRateFutureOptionBlackScenarioPnLFunction() {
    super(ValueRequirementNames.PNL, true);
  }

  /** The Black present value calculator */
  private static final PresentValueBlackCalculator PV_CALCULATOR = PresentValueBlackCalculator.getInstance();

  /** Property to define the price shift */
  private static final String PRICE_SHIFT = ScenarioPnLPropertyNamesAndValues.PROPERTY_PRICE_SHIFT;
  /** Property to define the volatility shift */
  private static final String VOL_SHIFT = ScenarioPnLPropertyNamesAndValues.PROPERTY_VOL_SHIFT;
  /** Property to define the price shift type */
  private static final String PRICE_SHIFT_TYPE = ScenarioPnLPropertyNamesAndValues.PROPERTY_PRICE_SHIFT_TYPE;
  /** Property to define the volatility shift type */
  private static final String VOL_SHIFT_TYPE = ScenarioPnLPropertyNamesAndValues.PROPERTY_VOL_SHIFT_TYPE;
  /** Logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(InterestRateFutureOptionBlackScenarioPnLFunction.class);

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle market,
      final ValueSpecification spec, final Set<ValueRequirement> desiredValues) {

    // Compute present value under current market
    final double pvBase = irFutureOption.accept(PV_CALCULATOR, market);

    // Form market scenario
    final YieldCurveWithBlackCubeBundle marketScen;
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();

    // Apply shift to yield curve(s)
    final YieldCurveBundle curvesScen = new YieldCurveBundle();
    final String priceShiftTypeConstraint = constraints.getValues(PRICE_SHIFT_TYPE).iterator().next();
    final String priceConstraint = constraints.getValues(PRICE_SHIFT).iterator().next();

    if (priceConstraint.equals("")) {
      // use base market prices
      curvesScen.addAll(market);
    } else {
      final Double shift = Double.valueOf(priceConstraint);
      // As curve may be functional, we can only apply a parallel shift.
      Double parallelShift;
      for (final String crvName : market.getAllNames()) {
        final YieldAndDiscountCurve curve = market.getCurve(crvName);
        if (priceShiftTypeConstraint.equalsIgnoreCase("Additive")) {
          parallelShift = shift;
        } else {
          if (!priceShiftTypeConstraint.equalsIgnoreCase("Multiplicative")) {
            LOGGER.debug("Valid PriceShiftType's: Additive and Multiplicative. Found: " + priceShiftTypeConstraint + " Defaulting to Multiplicative.");
          }
          // We (arbitrarily) choose to scale by the rate at the short end
          final double shortRate = curve.getInterestRate(0.0);
          parallelShift = shift * shortRate;
        }
        final YieldAndDiscountCurve curveShifted = curve.withParallelShift(parallelShift);
        curvesScen.setCurve(crvName, curveShifted);
      }
    }

    // Apply shift to vol surface
    final String volConstraint = constraints.getValues(VOL_SHIFT).iterator().next();
    if (volConstraint.equals("")) {
      // use base market vols
      marketScen = market;
    } else {
      // bump vol surface
      final Double shiftVol = Double.valueOf(volConstraint);
      final String volShiftTypeConstraint = constraints.getValues(VOL_SHIFT_TYPE).iterator().next();
      final boolean additiveShift;
      if (volShiftTypeConstraint.equalsIgnoreCase("Additive")) {
        additiveShift = true;
      } else if (volShiftTypeConstraint.equalsIgnoreCase("Multiplicative")) {
        additiveShift = false;
      } else {
        LOGGER.debug(
            "In ScenarioPnLFunctions, VolShiftType's are Additive and Multiplicative. Found: " + priceShiftTypeConstraint + " Defaulting to Multiplicative.");
        additiveShift = false;
      }
      final Surface<Double, Double, Double> volSurfaceScen = SurfaceShiftFunctionFactory.getShiftedSurface(market.getBlackParameters(), shiftVol,
          additiveShift);
      marketScen = new YieldCurveWithBlackCubeBundle(volSurfaceScen, curvesScen);
    }

    // Compute present value under scenario
    final double pvScen = irFutureOption.accept(PV_CALCULATOR, marketScen);

    // Return with spec
    return Collections.singleton(new ComputedValue(spec, pvScen - pvBase));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> superReqs = super.getRequirements(context, target, desiredValue);
    if (superReqs == null) {
      return null;
    }

    // Test constraints are provided, else set to ""
    final ValueProperties constraints = desiredValue.getConstraints();
    ValueProperties.Builder scenarioDefaults = null;

    final Set<String> priceShiftSet = constraints.getValues(PRICE_SHIFT);
    if (priceShiftSet == null || priceShiftSet.isEmpty()) {
      scenarioDefaults = constraints.copy().withoutAny(PRICE_SHIFT).with(PRICE_SHIFT, "");
    }
    final Set<String> priceShiftTypeSet = constraints.getValues(PRICE_SHIFT_TYPE);
    if (priceShiftTypeSet == null || priceShiftTypeSet.isEmpty()) {
      if (scenarioDefaults == null) {
        scenarioDefaults = constraints.copy().withoutAny(PRICE_SHIFT_TYPE).with(PRICE_SHIFT_TYPE, "Multiplicative");
      } else {
        scenarioDefaults = scenarioDefaults.withoutAny(PRICE_SHIFT_TYPE).with(PRICE_SHIFT_TYPE, "Multiplicative");
      }
    }
    final Set<String> volShiftSet = constraints.getValues(VOL_SHIFT);
    if (volShiftSet == null || volShiftSet.isEmpty()) {
      if (scenarioDefaults == null) {
        scenarioDefaults = constraints.copy().withoutAny(VOL_SHIFT).with(VOL_SHIFT, "");
      } else {
        scenarioDefaults = scenarioDefaults.withoutAny(VOL_SHIFT).with(VOL_SHIFT, "");
      }
    }
    final Set<String> volShiftSetType = constraints.getValues(VOL_SHIFT_TYPE);
    if (volShiftSetType == null || volShiftSetType.isEmpty()) {
      if (scenarioDefaults == null) {
        scenarioDefaults = constraints.copy().withoutAny(VOL_SHIFT_TYPE).with(VOL_SHIFT_TYPE, "Multiplicative");
      } else {
        scenarioDefaults = scenarioDefaults.withoutAny(VOL_SHIFT_TYPE).with(VOL_SHIFT_TYPE, "Multiplicative");
      }
    }

    // If defaults have been added, this adds additional copy of the Function into dep graph with the adjusted constraints
    if (scenarioDefaults != null) {
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL, target.toSpecification(), scenarioDefaults.get()));
    } // Scenarios are defined, so we're satisfied
    return superReqs;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String currency) {
    return super.getResultProperties(currency)
        .withAny(PRICE_SHIFT)
        .withAny(VOL_SHIFT)
        .withAny(PRICE_SHIFT_TYPE)
        .withAny(VOL_SHIFT_TYPE);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.size() == 1) {
      final ValueSpecification input = inputs.keySet().iterator().next();
      if (ValueRequirementNames.PNL.equals(input.getValueName())) {
        return inputs.keySet();
      }
    }
    return super.getResults(context, target, inputs);
  }
}
