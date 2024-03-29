/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda;

import static com.mcleodmoores.financial.function.properties.CurveCalculationProperties.ISDA;
import static com.opengamma.core.value.MarketDataRequirementNames.MARKET_VALUE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;
import static com.opengamma.engine.value.ValueRequirementNames.HAZARD_RATE_CURVE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.data.IsdaCurveProvider;
import com.mcleodmoores.financial.function.credit.cds.isda.util.CreditSecurityConverter;
import com.mcleodmoores.financial.function.credit.cds.isda.util.IsdaFunctionUtils;
import com.mcleodmoores.financial.function.credit.configs.CreditCurveSpecification;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FastCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.convention.IsdaCreditCurveConvention;
import com.opengamma.financial.credit.CdsRecoveryRateIdentifier;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.money.Currency;

/**
 * A function that creates a credit curve using the ISDA methodology ({@link FastCreditCurveBuilder}).
 */
public class IsdaCreditCurveFunction extends AbstractFunction.NonCompiledInvoker {
  private static final FastCreditCurveBuilder CREDIT_CURVE_BUILDER = new FastCreditCurveBuilder();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate valuationDate = ZonedDateTime.now(snapshotClock).toLocalDate();
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
    final ValueRequirement requirement = desiredValues.iterator().next();
    final CreditCurveIdentifier cdsId = (CreditCurveIdentifier) target.getValue();
    final Double recoveryRate = (Double) inputs.getValue(MARKET_VALUE);
    if (recoveryRate == null) {
      final CdsRecoveryRateIdentifier recoveryRateId = CdsRecoveryRateIdentifier.forSamedayCds(cdsId.getRedCode(), cdsId.getCurrency(), cdsId.getSeniority());
      throw new OpenGammaRuntimeException("Could not get recovery rate for " + target.getValue() + " with identifier " + recoveryRateId);
    }
    final CreditCurveSpecification specification = (CreditCurveSpecification) inputs.getValue(CURVE_SPECIFICATION);
    final SnapshotDataBundle marketData = (SnapshotDataBundle) inputs.getValue(CURVE_MARKET_DATA);
    final IsdaCurveProvider curves = (IsdaCurveProvider) inputs.getValue(CURVE_BUNDLE);
    if (curves == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve");
    }
    final ISDACompliantYieldCurve yieldCurve = curves.getIsdaDiscountingCurve(cdsId.getCurrency());
    final int n = specification.getNodes().size();
    final CDSAnalytic[] creditAnalytics = new CDSAnalytic[n];
    final CDSQuoteConvention[] quotes = new CDSQuoteConvention[n];
    int i = 0;
    for (final CurveNodeWithIdentifier node : specification.getNodes()) {
      final CreditSpreadNode creditNode = (CreditSpreadNode) node.getCurveNode();
      final Currency currency = cdsId.getCurrency();
      final ExternalIdBundle currencyId = ExternalIdBundle.of(currency.getObjectId().getScheme(), currency.getObjectId().getValue());
      final IsdaCreditCurveConvention convention = (IsdaCreditCurveConvention) conventionSource.getSingle(currencyId);
      final Double value = marketData.getDataPoint(node.getIdentifier());
      if (value == null) {
        throw new OpenGammaRuntimeException("Could not get quote for " + node.getIdentifier());
      }
      quotes[i] = IsdaFunctionUtils.getQuote(creditNode.getCoupon(), value, creditNode.getQuoteType());
      creditAnalytics[i++] = CreditSecurityConverter.convertStandardCDSSecurity(holidaySource, cdsId.getCurrency(), node, recoveryRate, convention,
          valuationDate);
    }
    final ISDACompliantCreditCurve creditCurve = CREDIT_CURVE_BUILDER.calibrateCreditCurve(creditAnalytics, quotes, yieldCurve);
    final ValueSpecification spec = new ValueSpecification(HAZARD_RATE_CURVE, target.toSpecification(), requirement.getConstraints());
    return Collections.singleton(new ComputedValue(spec, creditCurve));
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CREDIT_CURVE_IDENTIFIER;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> results = new HashSet<>();
    final ValueProperties properties = createValueProperties().with(CURVE_CALCULATION_METHOD, ISDA).withAny(CURVE_CONSTRUCTION_CONFIG).get();
    results.add(new ValueSpecification(HAZARD_RATE_CURVE, target.toSpecification(), properties));
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String config = desiredValue.getConstraint(CURVE_CONSTRUCTION_CONFIG);
    if (config == null) {
      return null;
    }
    final CreditCurveIdentifier cdsId = (CreditCurveIdentifier) target.getValue();
    final CdsRecoveryRateIdentifier recoveryRateId = CdsRecoveryRateIdentifier.forSamedayCds(cdsId.getRedCode(), cdsId.getCurrency(), cdsId.getSeniority());
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ValueProperties curveProperties = ValueProperties.builder().with(CURVE_CONSTRUCTION_CONFIG, config).with(PROPERTY_CURVE_TYPE, ISDA).get();
    requirements.add(new ValueRequirement(CURVE_BUNDLE, ComputationTargetSpecification.NULL, curveProperties));
    requirements.add(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.of(cdsId), ValueProperties.none()));
    requirements.add(new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.of(cdsId), ValueProperties.none()));
    requirements.add(new ValueRequirement(MARKET_VALUE, ComputationTargetType.PRIMITIVE, recoveryRateId.getExternalId()));
    return requirements;
  }

}
