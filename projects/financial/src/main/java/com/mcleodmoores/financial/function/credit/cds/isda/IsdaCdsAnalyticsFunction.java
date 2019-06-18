/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda;

import static com.mcleodmoores.financial.function.credit.cds.isda.util.IsdaFunctionUtils.getPointsUpfront;
import static com.mcleodmoores.financial.function.credit.cds.isda.util.IsdaFunctionUtils.getQuotedSpread;
import static com.mcleodmoores.financial.function.credit.cds.isda.util.IsdaFunctionUtils.getUpfrontAmount;
import static com.mcleodmoores.financial.function.properties.CurveCalculationProperties.ISDA;
import static com.opengamma.core.value.MarketDataRequirementNames.MARKET_VALUE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.ACCRUED_DAYS;
import static com.opengamma.engine.value.ValueRequirementNames.ACCRUED_PREMIUM;
import static com.opengamma.engine.value.ValueRequirementNames.CLEAN_PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.CLEAN_PRICE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.DIRTY_PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.HAZARD_RATE_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.PARALLEL_CS01;
import static com.opengamma.engine.value.ValueRequirementNames.POINTS_UPFRONT;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.PRINCIPAL;
import static com.opengamma.engine.value.ValueRequirementNames.QUOTED_SPREAD;
import static com.opengamma.engine.value.ValueRequirementNames.UPFRONT_AMOUNT;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.ROOT_FINDING;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.data.IsdaCurveProvider;
import com.mcleodmoores.financial.function.credit.cds.isda.util.CreditSecurityConverter;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticSpreadSensitivityCalculator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.QuotedSpread;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.curve.exposure.ConfigDBInstrumentExposuresProvider;
import com.opengamma.financial.analytics.curve.exposure.InstrumentExposuresProvider;
import com.opengamma.financial.analytics.isda.credit.FlatSpreadQuote;
import com.opengamma.financial.analytics.isda.credit.PointsUpFrontQuote;
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToIdentifierVisitor;
import com.opengamma.financial.convention.IsdaCreditCurveConvention;
import com.opengamma.financial.credit.CdsRecoveryRateIdentifier;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Returns:
 * <ul>
 * <li>Accrued premium</li>
 * <li>Accrued days</li>
 * <li>Clean present value (also aliased to present value and principal)</li>
 * <li>Clean price</li>
 * <li>Dirty present value (also aliased to upfront amount)</li>
 * <li>Parallel CS01</li>
 * <li>Points up-front</li>
 * <li>Quoted spread</li>
 * </ul>
 * calculated using the ISDA model for standard vanilla CDS trades.
 */
public class IsdaCdsAnalyticsFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger LOGGER = LoggerFactory.getLogger(IsdaCdsAnalyticsFunction.class);
  private static final String[] RESULTS = new String[] { ACCRUED_DAYS, ACCRUED_PREMIUM, POINTS_UPFRONT, CLEAN_PRESENT_VALUE, DIRTY_PRESENT_VALUE, CLEAN_PRICE,
      QUOTED_SPREAD, UPFRONT_AMOUNT, PARALLEL_CS01, PRINCIPAL, PRESENT_VALUE };
  private CreditSecurityToIdentifierVisitor _identifierVisitor;
  private InstrumentExposuresProvider _instrumentExposuresProvider;
  private static final AnalyticSpreadSensitivityCalculator CS01_CALCULATOR = new AnalyticSpreadSensitivityCalculator();

  @Override
  public void init(final FunctionCompilationContext context) {
    _identifierVisitor = new CreditSecurityToIdentifierVisitor(OpenGammaCompilationContext.getSecuritySource(context));
    _instrumentExposuresProvider = ConfigDBInstrumentExposuresProvider.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final StandardCDSSecurity security = (StandardCDSSecurity) target.getTrade().getSecurity();
    final Currency currency = security.getNotional().getCurrency();
    final IsdaCurveProvider curves = (IsdaCurveProvider) inputs.getValue(CURVE_BUNDLE);
    if (curves == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve bundle");
    }
    final ISDACompliantYieldCurve yieldCurve = curves.getIsdaDiscountingCurve(currency);
    final ISDACompliantCreditCurve creditCurve = (ISDACompliantCreditCurve) inputs.getValue(HAZARD_RATE_CURVE);
    final Double recoveryRate = (Double) inputs.getValue(MARKET_VALUE);
    final ExternalIdBundle currencyId = ExternalIdBundle.of(currency.getObjectId().getScheme(), currency.getObjectId().getValue());
    final IsdaCreditCurveConvention convention = (IsdaCreditCurveConvention) conventionSource.getSingle(currencyId);
    final CDSAnalytic cds = CreditSecurityConverter.convertStandardCdsSecurity(holidaySource, security, recoveryRate, convention, now.toLocalDate());
    final CDSQuoteConvention quote = getQuote(security.getCoupon(), constraints, inputs);
    final double notional = security.getNotional().getAmount();
    final double coupon = security.getCoupon();
    final int buySellPremiumFactor = security.isBuyProtection() ? -1 : 1;
    final double accruedPremium = cds.getAccruedPremium(coupon) * notional * buySellPremiumFactor;
    final double accruedDays = cds.getAccruedDays();
    final QuotedSpread quotedSpread;
    final PointsUpFront pointsUpfront;
    final BuySellProtection buySellProtection = security.isBuyProtection() ? BuySellProtection.BUY : BuySellProtection.SELL;
    if (quote instanceof PointsUpFront) {
      pointsUpfront = (PointsUpFront) quote;
      quotedSpread = getQuotedSpread(pointsUpfront, buySellProtection, yieldCurve, cds);
    } else if (quote instanceof QuotedSpread) {
      quotedSpread = (QuotedSpread) quote;
      pointsUpfront = getPointsUpfront(quotedSpread, buySellProtection, yieldCurve, cds, creditCurve);
    } else {
      throw new OpenGammaRuntimeException("Unhandled quote type " + quote);
    }
    final double upfrontAmount = getUpfrontAmount(cds, pointsUpfront, notional, buySellProtection);
    final double cleanPv = pointsUpfront.getPointsUpFront() * notional;
    final double principal = cleanPv;
    final double cleanPrice = 100 * (1 - pointsUpfront.getPointsUpFront());
    final double parallelCs01 = CS01_CALCULATOR.parallelCS01(cds, quotedSpread, yieldCurve);
    final Set<ComputedValue> results = new HashSet<>();
    results.add(new ComputedValue(new ValueSpecification(ACCRUED_DAYS, target.toSpecification(), constraints), accruedDays));
    results.add(new ComputedValue(new ValueSpecification(ACCRUED_PREMIUM, target.toSpecification(), constraints), CurrencyAmount.of(currency, accruedPremium)));
    results.add(new ComputedValue(new ValueSpecification(CLEAN_PRESENT_VALUE, target.toSpecification(), constraints), CurrencyAmount.of(currency, cleanPv)));
    results.add(new ComputedValue(new ValueSpecification(CLEAN_PRICE, target.toSpecification(), constraints), cleanPrice));
    results
        .add(new ComputedValue(new ValueSpecification(DIRTY_PRESENT_VALUE, target.toSpecification(), constraints), CurrencyAmount.of(currency, upfrontAmount)));
    results.add(new ComputedValue(new ValueSpecification(PARALLEL_CS01, target.toSpecification(), constraints), CurrencyAmount.of(currency, parallelCs01)));
    results.add(new ComputedValue(new ValueSpecification(POINTS_UPFRONT, target.toSpecification(), constraints), pointsUpfront.getPointsUpFront()));
    results.add(new ComputedValue(new ValueSpecification(PRESENT_VALUE, target.toSpecification(), constraints), CurrencyAmount.of(currency, cleanPv)));
    results.add(new ComputedValue(new ValueSpecification(PRINCIPAL, target.toSpecification(), constraints), CurrencyAmount.of(currency, principal)));
    results.add(new ComputedValue(new ValueSpecification(QUOTED_SPREAD, target.toSpecification(), constraints), quotedSpread.getQuotedSpread()));
    results.add(new ComputedValue(new ValueSpecification(UPFRONT_AMOUNT, target.toSpecification(), constraints), CurrencyAmount.of(currency, upfrontAmount)));
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
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
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getTrade().getSecurity() instanceof StandardCDSSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(PROPERTY_CURVE_TYPE, ISDA).withAny(CURVE_EXPOSURES).with(CURVE_CALCULATION_METHOD, ISDA)
        .withAny(ISDAFunctionConstants.CDS_QUOTE_CONVENTION).get();
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String result : RESULTS) {
      results.add(new ValueSpecification(result, target.toSpecification(), properties));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String yieldCurveConfig = desiredValue.getConstraint(CURVE_EXPOSURES);
    if (yieldCurveConfig == null || yieldCurveConfig.isEmpty()) {
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
    final Set<ValueRequirement> requirements = new HashSet<>();
    final Set<String> curveConstructionConfigurationNames = _instrumentExposuresProvider.getCurveConstructionConfigurationsForConfig(yieldCurveConfig,
        target.getTrade());
    final CreditCurveIdentifier cdsId = security.accept(_identifierVisitor);
    for (final String curveConstructionConfigurationName : curveConstructionConfigurationNames) {
      final ValueProperties curveBundleConstraints = ValueProperties.with(CURVE_CALCULATION_METHOD, ROOT_FINDING).with(PROPERTY_CURVE_TYPE, ISDA)
          .with(CURVE_CONSTRUCTION_CONFIG, curveConstructionConfigurationName).get();
      final ValueProperties hazardRateCurveConstraints = ValueProperties.builder().with(CURVE_CALCULATION_METHOD, ISDA)
          .with(CURVE_CONSTRUCTION_CONFIG, curveConstructionConfigurationName).get();
      requirements.add(new ValueRequirement(CURVE_BUNDLE, ComputationTargetSpecification.NULL, curveBundleConstraints));
      requirements.add(new ValueRequirement(HAZARD_RATE_CURVE, ComputationTargetSpecification.of(cdsId), hazardRateCurveConstraints));
    }
    final String quoteType = desiredValue.getConstraint(ISDAFunctionConstants.CDS_QUOTE_CONVENTION);
    final CdsRecoveryRateIdentifier recoveryRateId = CdsRecoveryRateIdentifier.forSamedayCds(cdsId.getRedCode(), cdsId.getCurrency(), cdsId.getSeniority());
    final ValueRequirement recoveryRate = new ValueRequirement(MARKET_VALUE, ComputationTargetType.PRIMITIVE, recoveryRateId.getExternalId());
    requirements.add(recoveryRate);
    // ask for flat spread and PUF unless the properties request a specific one
    final ValueRequirement flatSpread = new ValueRequirement(FlatSpreadQuote.TYPE, ComputationTargetType.PRIMITIVE,
        security.getExternalIdBundle().iterator().next());
    final ValueRequirement puf = new ValueRequirement(PointsUpFrontQuote.TYPE, ComputationTargetType.PRIMITIVE,
        security.getExternalIdBundle().iterator().next());
    if (quoteType == null || quoteType.isEmpty()) {
      requirements.add(flatSpread);
      requirements.add(puf);
    } else {
      switch (quoteType) {
        case FlatSpreadQuote.TYPE:
          requirements.add(flatSpread);
          break;
        case PointsUpFrontQuote.TYPE:
          requirements.add(puf);
          break;
        default:
          LOGGER.warn("Unhandled quote type " + quoteType);
          requirements.add(flatSpread);
          requirements.add(puf);
      }
    }
    return requirements;
  }

  private static CDSQuoteConvention getQuote(final double coupon, final ValueProperties constraints, final FunctionInputs inputs) {
    String requestedQuoteType = constraints.getSingleValue(ISDAFunctionConstants.CDS_QUOTE_CONVENTION);
    final Double quotedValue;
    if (requestedQuoteType != null) {
      quotedValue = (Double) inputs.getValue(requestedQuoteType);
    } else {
      final Object quotedSpread = inputs.getValue(FlatSpreadQuote.TYPE);
      if (quotedSpread == null) {
        quotedValue = (Double) inputs.getValue(PointsUpFrontQuote.TYPE);
        requestedQuoteType = PointsUpFrontQuote.TYPE;
      } else {
        quotedValue = (Double) quotedSpread;
        requestedQuoteType = FlatSpreadQuote.TYPE;
      }
    }
    if (quotedValue == null) {
      throw new OpenGammaRuntimeException("Could not get flat spread or points upfront quote");
    }
    switch (requestedQuoteType) {
      case FlatSpreadQuote.TYPE:
        return new QuotedSpread(coupon, quotedValue);
      case PointsUpFrontQuote.TYPE:
        return new PointsUpFront(coupon, quotedValue);
      default:
        throw new OpenGammaRuntimeException("Unsupported quote type " + requestedQuoteType);
    }
  }
}
