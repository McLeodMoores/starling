/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.equity.EqyOptPDEPresentValueCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.equity.ScenarioPnLPropertyNamesAndValues;

/**
 *
 * @author casey
 *
 */
public class EquityOptionPDEScenarioPnLFunction extends EquityOptionPDEFunction {

  private static final EqyOptPDEPresentValueCalculator PV_CALCULATOR = EqyOptPDEPresentValueCalculator.getInstance();

  /** Default constructor */
  public EquityOptionPDEScenarioPnLFunction() {
    super(ValueRequirementNames.PNL);
  }

  private static final String PRICE_SHIFT = ScenarioPnLPropertyNamesAndValues.PROPERTY_PRICE_SHIFT;
  private static final String VOL_SHIFT = ScenarioPnLPropertyNamesAndValues.PROPERTY_VOL_SHIFT;
  private static final String PRICE_SHIFT_TYPE = ScenarioPnLPropertyNamesAndValues.PROPERTY_PRICE_SHIFT_TYPE;
  private static final String VOL_SHIFT_TYPE = ScenarioPnLPropertyNamesAndValues.PROPERTY_VOL_SHIFT_TYPE;

  private static final Logger LOGGER = LoggerFactory.getLogger(EquityOptionBAWScenarioPnLFunction.class);

  private String getValueRequirementName() {
    return ValueRequirementNames.PNL;
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs, final Set<ValueRequirement> desiredValues,
      final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {

    // Compute present value under current market
    final double pvBase = derivative.accept(PV_CALCULATOR, market);


    // Form market scenario
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();

    // Apply shift to forward price curve
    final ForwardCurve fwdCurveScen;
    final String priceShiftTypeConstraint = constraints.getValues(PRICE_SHIFT_TYPE).iterator().next();
    final String stockConstraint = constraints.getValues(PRICE_SHIFT).iterator().next();

    if (stockConstraint.equals("")) {
      fwdCurveScen = market.getForwardCurve(); // use base market prices
    } else {
      final Double fractionalShift;
      if (priceShiftTypeConstraint.equalsIgnoreCase("Additive")) {
        final Double absShift = Double.valueOf(stockConstraint);
        final double spotPrice = market.getForwardCurve().getSpot();
        fractionalShift = absShift / spotPrice;
      } else if (priceShiftTypeConstraint.equalsIgnoreCase("Multiplicative")) {
        fractionalShift = Double.valueOf(stockConstraint);
      } else {
        fractionalShift = Double.valueOf(stockConstraint);
        LOGGER.debug("Valid PriceShiftType's: Additive and Multiplicative. Found: " + priceShiftTypeConstraint + " Defaulting to Multiplicative.");
      }
      fwdCurveScen = market.getForwardCurve().withFractionalShift(fractionalShift);
    }

    // Apply shift to vol surface curve
    final BlackVolatilitySurface<?> volSurfScen;
    final String volConstraint = constraints.getValues(VOL_SHIFT).iterator().next();
    if (volConstraint.equals("")) { // use base market vols
      volSurfScen = market.getVolatilitySurface();
    } else { // bump vol surface
      final Double shiftVol = Double.valueOf(volConstraint);
      final String volShiftTypeConstraint = constraints.getValues(VOL_SHIFT_TYPE).iterator().next();
      final boolean additiveShift;
      if (volShiftTypeConstraint.equalsIgnoreCase("Additive")) {
        additiveShift = true;
      } else if (volShiftTypeConstraint.equalsIgnoreCase("Multiplicative")) {
        additiveShift = false;
      } else {
        LOGGER.debug("In ScenarioPnLFunctions, VolShiftType's are Additive and Multiplicative. Found: " + priceShiftTypeConstraint + " Defaulting to Multiplicative.");
        additiveShift = false;
      }
      volSurfScen = market.getVolatilitySurface().withShift(shiftVol, additiveShift);
    }

    final StaticReplicationDataBundle marketScen = new StaticReplicationDataBundle(volSurfScen, market.getDiscountCurve(), fwdCurveScen);

    // Compute present value under scenario
    final double pvScen = derivative.accept(PV_CALCULATOR, marketScen);

    // Return with spec
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    return Collections.singleton(new ComputedValue(resultSpec, pvScen - pvBase));
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
      return Collections.singleton(new ValueRequirement(getValueRequirementName(), target.toSpecification(), scenarioDefaults.get()));
    } else {  // Scenarios are defined, so we're satisfied
      return superReqs;
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.size() == 1) {
      final ValueSpecification input = inputs.keySet().iterator().next();
      if (getValueRequirementName().equals(input.getValueName())) {
        return inputs.keySet();
      }
    }
    final ValueSpecification superSpec = super.getResults(context, target, inputs).iterator().next();
    final Builder properties = superSpec.getProperties().copy()
        .withAny(PRICE_SHIFT)
        .withAny(VOL_SHIFT)
        .withAny(PRICE_SHIFT_TYPE)
        .withAny(VOL_SHIFT_TYPE);

    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties.get()));
  }
}
