/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;

/**
 * PnL requirements gatherer.
 * @deprecated Deprecated
 */
@Deprecated
public class DefaultPnLRequirementsGatherer implements PnLRequirementsGatherer {

  @Override
  public Set<ValueRequirement> getFirstOrderRequirements(final FinancialSecurity security, final String samplingPeriod, final String scheduleCalculator,
      final String samplingFunction,
      final ComputationTargetSpecification targetSpec, final String currency) {
    return security.accept(getFirstOrderRequirements(samplingPeriod, scheduleCalculator, samplingFunction, targetSpec, currency));
  }

  //TODO another visitor that takes desiredValue and uses those properties instead of the static defaults
  protected FinancialSecurityVisitor<Set<ValueRequirement>> getFirstOrderRequirements(final String samplingPeriod, final String scheduleCalculator,
      final String samplingFunction,
      final ComputationTargetSpecification targetSpec, final String currency) {
    final ValueProperties commonProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriod)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculator)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunction).get();
    return new FinancialSecurityVisitorAdapter<Set<ValueRequirement>>() {

      @Override
      public Set<ValueRequirement> visitEquitySecurity(final EquitySecurity security) {
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, "Delta").get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @Override
      public Set<ValueRequirement> visitEquityOptionSecurity(final EquityOptionSecurity security) {
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, "Delta").get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @Override
      public Set<ValueRequirement> visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.BUCKETED_CS01)
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @Override
      public Set<ValueRequirement> visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.BUCKETED_CS01)
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @Override
      public Set<ValueRequirement> visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.BUCKETED_CS01)
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

      @Override
      public Set<ValueRequirement> visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
        final ValueProperties properties = commonProperties.copy()
            .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.BUCKETED_CS01)
            .get();
        return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
      }

    };
  }

}
