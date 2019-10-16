/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.cds.isda;

import static com.opengamma.core.value.MarketDataRequirementNames.MARKET_VALUE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.CLEAN_PRICE;
import static com.opengamma.engine.value.ValueRequirementNames.CREDIT_SPREAD;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.HAZARD_RATE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.data.IsdaCurveProvider;
import com.mcleodmoores.date.CalendarAdapter;
import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AnalyticBondPricer;
import com.opengamma.analytics.financial.credit.isdastandardmodel.BondAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDAPremiumLegSchedule;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProvider;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.analytics.conversion.ConversionUtils;
import com.opengamma.financial.analytics.conversion.WorkingDayCalendarUtils;
import com.opengamma.financial.analytics.curve.exposure.ConfigDBInstrumentExposuresProvider;
import com.opengamma.financial.analytics.curve.exposure.InstrumentExposuresProvider;
import com.opengamma.financial.analytics.model.curve.IssuerProviderDiscountingFunction;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.credit.CdsRecoveryRateIdentifier;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Calculates the credit spread required for a bond to price to par, and the associated hazard rate.
 */
public class IsdaBondCreditSpreadHazardRateFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger LOGGER = LoggerFactory.getLogger(IsdaBondCreditSpreadHazardRateFunction.class);
  private static final AnalyticBondPricer CALCULATOR = new AnalyticBondPricer();
  private InstrumentExposuresProvider _instrumentExposuresProvider;

  @Override
  public void init(final FunctionCompilationContext context) {
    _instrumentExposuresProvider = ConfigDBInstrumentExposuresProvider.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final Trade trade = target.getTrade();
    final BondSecurity security = (BondSecurity) trade.getSecurity();
    final String domicile = security.getIssuerDomicile();
    ExternalIdBundle bundle = security.getExternalIdBundle();
    bundle = bundle.withExternalId(ExternalSchemes.financialRegionId(domicile));
    final BondConvention bondConvention = OpenGammaExecutionContext.getConventionSource(executionContext).getSingle(bundle, BondConvention.class);
    final IssuerProvider curves = (IssuerProvider) inputs.getValue(CURVE_BUNDLE);
    if (curves == null) {
      throw new OpenGammaRuntimeException("Could not get issuer curves for " + security);
    }
    final ISDACompliantYieldCurve yieldCurve = ((IsdaCurveProvider) curves.getMulticurveProvider()).getIsdaDiscountingCurve(security.getCurrency());
    final Double cleanPrice = (Double) inputs.getValue(CLEAN_PRICE) / 100.;
    final Double recoveryRate = (Double) inputs.getValue(MARKET_VALUE);
    final LocalDate startDate = security.getInterestAccrualDate().toLocalDate();
    final LocalDate endDate = security.getLastTradeDate().getExpiry().toLocalDate();
    final Period step = ConversionUtils.getTenor(security.getCouponFrequency());
    final StubType stubType = getStubType(security, step);
    final WorkingDayCalendar calendar = getCalendar(executionContext, security);
    final ISDAPremiumLegSchedule schedule = new ISDAPremiumLegSchedule(startDate, endDate, step, stubType, bondConvention.getBusinessDayConvention(),
        CalendarAdapter.of(calendar), true);
    final BondAnalytic bond = new BondAnalytic(now.toLocalDate(), security.getCouponRate() * 0.01, schedule, recoveryRate, security.getDayCount());
    final CDSAnalytic cds = new CDSAnalytic(now.toLocalDate(), now.toLocalDate().plusDays(1), now.toLocalDate(), startDate, endDate, true, step, stubType,
        true, recoveryRate, bondConvention.getBusinessDayConvention(), CalendarAdapter.of(calendar), security.getDayCount(), DayCounts.ACT_365);
    final double spread = CALCULATOR.getEquivalentCDSSpread(bond, yieldCurve, cleanPrice, PriceType.CLEAN, cds);
    final double hazardRate = CALCULATOR.getHazardRate(bond, yieldCurve, cleanPrice, PriceType.CLEAN);
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final ValueSpecification creditSpreadSpec = new ValueSpecification(CREDIT_SPREAD, target.toSpecification(), constraints);
    final ValueSpecification hazardRateSpec = new ValueSpecification(HAZARD_RATE, target.toSpecification(), constraints);
    final Set<ComputedValue> results = new HashSet<>();
    results.add(new ComputedValue(creditSpreadSpec, spread));
    results.add(new ComputedValue(hazardRateSpec, hazardRate));
    return results;
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    return security instanceof BondSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().withAny(PROPERTY_CURVE_TYPE)
        .withAny(IssuerProviderDiscountingFunction.UNDERLYING_CURVE_TYPE_PROPERTY).withAny(CURVE_EXPOSURES).get();
    final Set<ValueSpecification> results = new HashSet<>();
    results.add(new ValueSpecification(CREDIT_SPREAD, target.toSpecification(), properties));
    results.add(new ValueSpecification(HAZARD_RATE, target.toSpecification(), properties));
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveExposureConfigs = constraints.getValues(CURVE_EXPOSURES);
    if (curveExposureConfigs == null || curveExposureConfigs.size() != 1) {
      return null;
    }
    final Set<String> curveTypes = constraints.getValues(PROPERTY_CURVE_TYPE);
    final Set<String> underlyingCurveTypes = constraints.getValues(IssuerProviderDiscountingFunction.UNDERLYING_CURVE_TYPE_PROPERTY);
    final BondSecurity security = (BondSecurity) target.getTrade().getSecurity();
    final Set<ValueRequirement> requirements = new HashSet<>();
    try {
      for (final String curveExposureConfig : curveExposureConfigs) {
        final Set<String> curveConstructionConfigurationNames = _instrumentExposuresProvider.getCurveConstructionConfigurationsForConfig(curveExposureConfig,
            target.getTrade());
        if (curveConstructionConfigurationNames == null) {
          LOGGER.error("Could not get curve construction configuration names for curve exposure configuration called {}", curveExposureConfig);
          return null;
        }
        for (final String curveConstructionConfigurationName : curveConstructionConfigurationNames) {
          ValueProperties.Builder builder = ValueProperties.builder().with(CURVE_CONSTRUCTION_CONFIG, curveConstructionConfigurationName);
          ValueProperties.Builder priceBuilder = ValueProperties.builder().with(CURVE_EXPOSURES, curveExposureConfig);
          if (curveTypes != null && !curveTypes.isEmpty()) {
            builder = builder.with(PROPERTY_CURVE_TYPE, curveTypes);
          }
          if (underlyingCurveTypes != null && !underlyingCurveTypes.isEmpty()) {
            builder = builder.with(IssuerProviderDiscountingFunction.UNDERLYING_CURVE_TYPE_PROPERTY, underlyingCurveTypes);
            priceBuilder = priceBuilder.with(IssuerProviderDiscountingFunction.UNDERLYING_CURVE_TYPE_PROPERTY, underlyingCurveTypes);
          } else {
            builder = builder.with(IssuerProviderDiscountingFunction.UNDERLYING_CURVE_TYPE_PROPERTY, curveTypes);
            priceBuilder = priceBuilder.with(IssuerProviderDiscountingFunction.UNDERLYING_CURVE_TYPE_PROPERTY, curveTypes);
          }
          requirements.add(new ValueRequirement(CURVE_BUNDLE, ComputationTargetSpecification.NULL, builder.get()));
          requirements.add(new ValueRequirement(CLEAN_PRICE, target.toSpecification(), priceBuilder.get()));
        }
      }
      final ExternalId recoveryRateId = getRecoveryRateId(target, security.getCurrency());
      if (recoveryRateId != null) {
        requirements.add(new ValueRequirement(MARKET_VALUE, ComputationTargetType.PRIMITIVE, recoveryRateId));
      }
      return requirements;
    } catch (final Exception e) {
      LOGGER.error(e.getMessage());
      return null;
    }
  }

  private static ExternalId getRecoveryRateId(final ComputationTarget target, final Currency currency) {
    final Trade trade = target.getTrade();
    final Security security = trade.getSecurity();
    String redCode = trade.getAttributes().get("RED_CODE");
    if (redCode == null) {
      redCode = security.getAttributes().get("RED_CODE");
      if (redCode == null) {
        LOGGER.error("Could not get red code for " + security);
        return null;
      }
    }
    String seniority = trade.getAttributes().get("SENIORITY");
    if (seniority == null) {
      seniority = security.getAttributes().get("SENIORITY");
      if (seniority == null) {
        LOGGER.error("Could not get seniority for " + security);
        return null;
      }
    }
    return CdsRecoveryRateIdentifier.forSamedayCds(redCode, currency, seniority).getExternalId();
  }

  private static WorkingDayCalendar getCalendar(final FunctionExecutionContext executionContext, final BondSecurity security) {
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final ExternalId regionId = ExternalSchemes.financialRegionId(security.getIssuerDomicile());
    if (regionId == null) {
      throw new OpenGammaRuntimeException("Could not find region for " + security.getIssuerDomicile());
    }
    final WorkingDayCalendar calendar = WorkingDayCalendarUtils.getCalendarForRegionOrCurrency(regionSource, holidaySource, regionId, security.getCurrency());
    return calendar;
  }

  private static StubType getStubType(final BondSecurity bondSecurity, final Period step) {
    final LocalDate firstAccrual = bondSecurity.getInterestAccrualDate().toLocalDate();
    final LocalDate firstCoupon = bondSecurity.getFirstCouponDate().toLocalDate();
    final LocalDate calculatedFirstCoupon = firstAccrual.plus(step);
    if (calculatedFirstCoupon.isBefore(firstCoupon)) {
      return StubType.FRONTLONG;
    } else if (calculatedFirstCoupon.isAfter(firstCoupon)) {
      return StubType.FRONTSHORT;
    }
    final LocalDate maturity = bondSecurity.getLastTradeDate().getExpiry().toLocalDate();
    LocalDate calculatedMaturity = firstCoupon;
    while (calculatedMaturity.isBefore(maturity)) {
      calculatedMaturity = calculatedMaturity.plus(step);
    }
    if (calculatedMaturity.isBefore(maturity)) {
      return StubType.BACKLONG;
    } else if (calculatedMaturity.isAfter(maturity)) {
      return StubType.BACKSHORT;
    }
    // NONE isn't allowed
    return StubType.FRONTLONG;
  }
}
