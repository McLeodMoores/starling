/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cds;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FiniteDifferenceSpreadSensitivityCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.QuotedSpread;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class StandardVanillaParallelCS01CDSFunction extends StandardVanillaCS01CDSFunction {
  private static final FiniteDifferenceSpreadSensitivityCalculator CALCULATOR = new FiniteDifferenceSpreadSensitivityCalculator();

  public StandardVanillaParallelCS01CDSFunction() {
    super(ValueRequirementNames.CS01);
  }

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapDefinition definition,
                                                final ISDACompliantYieldCurve yieldCurve,
                                                final ZonedDateTime[] times,
                                                final double[] marketSpreads,
                                                final ZonedDateTime valuationDate,
                                                final ComputationTarget target,
                                                final ValueProperties properties,
                                                final FunctionInputs inputs,
                                                final ISDACompliantCreditCurve hazardCurve, final CDSAnalytic analytic, final Tenor[] tenors) {
    //TODO: bump type
    final Double bump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_BUMP)));
    final double cs01 = parallelCS01(definition, yieldCurve, times, marketSpreads, analytic, bump * 1e-4, definition.getBuySellProtection(), tenors);

    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.CS01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, cs01));
  }

  public static double parallelCS01(final CreditDefaultSwapDefinition definition,
                             final ISDACompliantYieldCurve yieldCurve,
                             final ZonedDateTime[] times, final double[] marketSpreads, final CDSAnalytic analytic, final double fracBump, final
                             BuySellProtection buySellProtection, final Tenor[] tenors) {
    double cs01;
    if (definition instanceof StandardCreditDefaultSwapDefinition) {
      final StandardCreditDefaultSwapDefinition cds = (StandardCreditDefaultSwapDefinition) definition;
      cs01 = CALCULATOR.parallelCS01(analytic, new QuotedSpread(cds.getQuotedSpread() * 1e-4, cds.getPremiumLegCoupon()), yieldCurve, fracBump);
    } else if (definition instanceof LegacyCreditDefaultSwapDefinition) {
      final CDSAnalyticFactory analyticFactory = new CDSAnalyticFactory(definition.getRecoveryRate(), definition.getCouponFrequency().getPeriod())
          .with(definition.getBusinessDayAdjustmentConvention())
          .with(definition.getCalendar()).with(definition.getStubType())
          .withAccrualDCC(definition.getDayCountFractionConvention());
      final Period[] periods = new Period[times.length];
      for (int i = 0; i < times.length; i++) {
        periods[i] = tenors[i].getPeriod();
      }
      final CDSAnalytic[] pillars = analyticFactory.makeIMMCDS(definition.getStartDate().toLocalDate(), periods);
      cs01 = CALCULATOR.parallelCS01FromParSpreads(analytic,
                                                   ((LegacyCreditDefaultSwapDefinition) definition).getParSpread() * 1e-4,
                                                   yieldCurve,
                                                   pillars,
                                                   marketSpreads,
                                                   fracBump,
                                                   BumpType.ADDITIVE);
    } else {
      throw new OpenGammaRuntimeException("Unexpected cds type: " + definition.getClass().getSimpleName());
    }
    cs01 = cs01 * definition.getNotional() * 1e-4;
    return buySellProtection == BuySellProtection.BUY ? cs01 : -cs01;
  }

}
