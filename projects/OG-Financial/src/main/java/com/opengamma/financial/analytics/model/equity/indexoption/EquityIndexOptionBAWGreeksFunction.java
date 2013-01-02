/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.analytics.financial.equity.EqyOptBaroneAdesiGreekCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public class EquityIndexOptionBAWGreeksFunction extends EquityIndexOptionBAWFunction {
  /** Value requirement names */
  private static final String[] GREEK_NAMES = new String[] {
    ValueRequirementNames.VALUE_DELTA,
    ValueRequirementNames.VALUE_DUAL_DELTA,
    ValueRequirementNames.VALUE_RHO,
    ValueRequirementNames.VALUE_CARRY_RHO,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.VALUE_THETA
  };
  /** Equivalent greeks */
  private static final Greek[] GREEKS = new Greek[] {
    Greek.DELTA,
    Greek.DUAL_DELTA,
    Greek.RHO,
    Greek.CARRY_RHO,
    Greek.VEGA,
    Greek.THETA
  };


  /**
   * Default constructor
   */
  public EquityIndexOptionBAWGreeksFunction() {
    super(GREEK_NAMES);
  }


  @Override
  protected Set<ComputedValue> computeValues(final EquityIndexOption derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final GreekResultCollection greeks = derivative.accept(EqyOptBaroneAdesiGreekCalculator.getInstance(), market);
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    for (int i = 0; i < GREEKS.length; i++) {
      final ValueSpecification spec = new ValueSpecification(GREEK_NAMES[i], targetSpec, resultProperties);
      final double greek = greeks.get(GREEKS[i]);
      result.add(new ComputedValue(spec, greek));
    }
    return result;
  }

}