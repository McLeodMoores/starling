/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyCDSSecurity;
import com.opengamma.financial.security.cds.StandardCDSSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CreditInstrumentCS01PnLDefaults extends DefaultPropertyFunction {
  private final String _samplingPeriod;
  private final String _scheduleCalculator;
  private final String _samplingFunction;

  public CreditInstrumentCS01PnLDefaults(final String samplingPeriod, final String scheduleCalculator, final String samplingFunction) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(samplingPeriod, "sampling period");
    ArgumentChecker.notNull(scheduleCalculator, "schedule calculator");
    ArgumentChecker.notNull(samplingFunction, "sampling function");
    _samplingPeriod = samplingPeriod;
    _scheduleCalculator = scheduleCalculator;
    _samplingFunction = samplingFunction;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    return security instanceof StandardCDSSecurity
        || security instanceof LegacyCDSSecurity
        || security instanceof CreditDefaultSwapOptionSecurity
        || security instanceof CreditDefaultSwapIndexSecurity;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_PERIOD);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SCHEDULE_CALCULATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_FUNCTION);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    if (ValuePropertyNames.SAMPLING_PERIOD.equals(propertyName)) {
      return Collections.singleton(_samplingPeriod);
    }
    if (ValuePropertyNames.SCHEDULE_CALCULATOR.equals(propertyName)) {
      return Collections.singleton(_scheduleCalculator);
    }
    if (ValuePropertyNames.SAMPLING_FUNCTION.equals(propertyName)) {
      return Collections.singleton(_samplingFunction);
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.PNL_SERIES;
  }
}
