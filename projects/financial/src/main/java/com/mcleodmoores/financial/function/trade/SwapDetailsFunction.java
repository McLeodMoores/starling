/**
 *
 */
package com.mcleodmoores.financial.function.trade;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.FIXED_CASH_FLOWS;
import static com.opengamma.engine.value.ValueRequirementNames.FLOATING_CASH_FLOWS;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ProviderUtils;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.DefaultTradeConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.exposure.ConfigDBInstrumentExposuresProvider;
import com.opengamma.financial.analytics.curve.exposure.InstrumentExposuresProvider;
import com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.fixedincome.SwapLegCashFlows;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class SwapDetailsFunction extends AbstractFunction.NonCompiledInvoker {
  public static final String LEG_TYPE_PROPERTY = "LegTypeProperty";
  public static final String PAY_LEG = "PayLeg";
  public static final String RECEIVE_LEG = "ReceiveLeg";
  private static final Logger LOGGER = LoggerFactory.getLogger(SwapDetailsFunction.class);
  private static final SwapDetailsCalculator CALCULATOR = new SwapDetailsCalculator();
  private CurveConstructionConfigurationSource _curveConstructionConfigurationSource;
  private InstrumentExposuresProvider _instrumentExposuresProvider;
  private DefaultTradeConverter _tradeConverter;
  private FixedIncomeConverterDataProvider _definitionConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveConstructionConfigurationSource = ConfigDBCurveConstructionConfigurationSource.init(context, this);
    _instrumentExposuresProvider = ConfigDBInstrumentExposuresProvider.init(context, this);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final SwapSecurityConverter securityConverter = new SwapSecurityConverter(securitySource, holidaySource, conventionSource, regionSource);
    _tradeConverter = new DefaultTradeConverter(securityConverter);
    _definitionConverter = new FixedIncomeConverterDataProvider(null, securitySource, timeSeriesResolver);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    // find out what type the graph is expecting
    ValueRequirement payLegReq = null;
    ValueRequirement receiveLegReq = null;
    for (final ValueRequirement requirement : desiredValues) {
      if (PAY_LEG.equals(requirement.getConstraint(LEG_TYPE_PROPERTY))) {
        payLegReq = requirement;
      } else if (RECEIVE_LEG.equals(requirement.getConstraint(LEG_TYPE_PROPERTY))) {
        receiveLegReq = requirement;
      }
    }
    if (payLegReq == null || receiveLegReq == null) {
      throw new OpenGammaRuntimeException("Could not get requirements");
    }
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final MulticurveProviderInterface curves = getMergedProviders(inputs, target, executionContext.getSecuritySource());
    final SwapSecurity security = (SwapSecurity) target.getTrade().getSecurity();
    final SwapDefinition definition = (SwapDefinition) _tradeConverter.convert(target.getTrade());
    final InstrumentDerivative derivative = _definitionConverter.convert(target.getTrade().getSecurity(), definition, now, timeSeries);
    final SwapLegCashFlows payResult = derivative.accept(CALCULATOR, new SwapDetailsProvider(curves, now, definition, security, PayReceiveType.PAY));
    final SwapLegCashFlows receiveResult = derivative.accept(CALCULATOR, new SwapDetailsProvider(curves, now, definition, security, PayReceiveType.RECEIVE));
    final ValueSpecification payLegSpec = new ValueSpecification(payLegReq.getValueName(), target.toSpecification(),
        payLegReq.getConstraints().copy().get());
    final ValueSpecification receiveLegSpec = new ValueSpecification(receiveLegReq.getValueName(), target.toSpecification(),
        receiveLegReq.getConstraints().copy().get());
    final Set<ComputedValue> results = new HashSet<>();
    results.add(new ComputedValue(payLegSpec, payResult));
    results.add(new ComputedValue(receiveLegSpec, receiveResult));
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getTrade().getSecurity() instanceof SwapSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> results = new HashSet<>();
    final ValueProperties.Builder properties = createValueProperties()
        .withAny(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE)
        .withAny(ValuePropertyNames.CURVE_EXPOSURES);
    final SwapSecurity security = (SwapSecurity) target.getTrade().getSecurity();
    if (security.getPayLeg() instanceof FixedInterestRateLeg) {
      results.add(new ValueSpecification(FIXED_CASH_FLOWS, target.toSpecification(), properties.copy().with(LEG_TYPE_PROPERTY, PAY_LEG).get()));
    } else {
      results.add(new ValueSpecification(FLOATING_CASH_FLOWS, target.toSpecification(), properties.copy().with(LEG_TYPE_PROPERTY, PAY_LEG).get()));
    }
    if (security.getReceiveLeg() instanceof FixedInterestRateLeg) {
      results.add(new ValueSpecification(FIXED_CASH_FLOWS, target.toSpecification(), properties.copy().with(LEG_TYPE_PROPERTY, RECEIVE_LEG).get()));
    } else {
      results.add(new ValueSpecification(FLOATING_CASH_FLOWS, target.toSpecification(), properties.copy().with(LEG_TYPE_PROPERTY, RECEIVE_LEG).get()));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target,
      final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final String curveType = constraints.getSingleValue(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE);
    final ValueProperties.Builder properties = ValueProperties.builder();
    if (curveType == null) {
      properties.withAny(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE);
    } else {
      properties.with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, curveType);
    }
    final Set<String> curveExposureConfigs = constraints.getValues(CURVE_EXPOSURES);
    if (curveExposureConfigs == null) {
      return null;
    }
    try {
      final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
      final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
      final Set<ValueRequirement> requirements = new HashSet<>();
      for (final String curveExposureConfig : curveExposureConfigs) {
        final Set<String> curveConstructionConfigurationNames =
            _instrumentExposuresProvider.getCurveConstructionConfigurationsForConfig(curveExposureConfig, target.getTrade());
        for (final String curveConstructionConfigurationName : curveConstructionConfigurationNames) {
          final ValueProperties curveBundleConstraints = properties.copy()
              .with(CURVE_CONSTRUCTION_CONFIG, curveConstructionConfigurationName)
              .with(CURVE_EXPOSURES, curveExposureConfig).withOptional(CURVE_EXPOSURES)
              .get();
          requirements.add(new ValueRequirement(CURVE_BUNDLE, ComputationTargetSpecification.NULL, curveBundleConstraints));
          final CurveConstructionConfiguration curveConstructionConfiguration =
              _curveConstructionConfigurationSource.getCurveConstructionConfiguration(curveConstructionConfigurationName);
          if (curveConstructionConfiguration == null) {
            LOGGER.error("Could not get curve construction configuration called {} from config master", curveConstructionConfigurationName);
            return null;
          }
        }
      }
      final Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(security, securitySource);
      if (currencies.size() > 1) {
        final Iterator<Currency> iter = currencies.iterator();
        final Currency initialCurrency = iter.next();
        while (iter.hasNext()) {
          requirements.add(new ValueRequirement(ValueRequirementNames.SPOT_RATE,
              CurrencyPair.TYPE.specification(CurrencyPair.of(iter.next(), initialCurrency))));
        }
      }
      final Trade trade = target.getTrade();
      final InstrumentDefinition<?> definition = _tradeConverter.convert(trade);
      final Set<ValueRequirement> timeSeriesRequirements = _definitionConverter.getConversionTimeSeriesRequirements(trade.getSecurity(), definition);
      if (timeSeriesRequirements == null) {
        LOGGER.error("Could not get fixing time series requirements for {}", security);
        return null;
      }
      requirements.addAll(timeSeriesRequirements);
      return requirements;
    } catch (final Exception e) {
      LOGGER.error(e.getMessage());
      return null;
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    String curveType = null;
    String curveExposuresName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueRequirement value = entry.getValue();
      if (value.getValueName().equals(CURVE_BUNDLE)) {
        final Set<String> curveTypes = value.getConstraints().getValues(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE);
        if (curveTypes != null && !curveTypes.isEmpty()) {
          curveType = value.getConstraint(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE);
        }
        curveExposuresName = value.getConstraint(CURVE_EXPOSURES);
        break;
      }
    }
    if (curveExposuresName == null) {
      // need the curve exposures name to be set - either in the view definition or a defaults function
      return null;
    }
    final Set<ValueSpecification> results = getResults(context, target);
    final Set<ValueSpecification> newResults = new HashSet<>();
    for (final ValueSpecification result : results) {
      // replace withAny if there's a specific value
      final ValueProperties.Builder properties = result.getProperties().copy()
          .withoutAny(CURVE_EXPOSURES).with(CURVE_EXPOSURES, curveExposuresName);
      if (curveType != null) {
        properties.withoutAny(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE)
                  .with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, curveType);
      }
      newResults.add(new ValueSpecification(result.getValueName(), result.getTargetSpecification(), properties.get()));
    }
    return newResults;
  }

  private static MulticurveProviderDiscount getMergedProviders(final FunctionInputs inputs, final ComputationTarget target,
      final SecuritySource securitySource) {
    final Collection<MulticurveProviderDiscount> providers = new HashSet<>();
    final FXMatrix matrix = getFXMatrix(inputs, target, securitySource);
    for (final ComputedValue input : inputs.getAllValues()) {
      final String valueName = input.getSpecification().getValueName();
      if (CURVE_BUNDLE.equals(valueName)) {
        final Object curves = input.getValue();
        if (curves instanceof MulticurveProviderDiscount) {
          providers.add((MulticurveProviderDiscount) curves);
        } else {
          throw new UnsupportedOperationException("Unhandled curve bundle type " + providers);
        }
      }
    }
    final MulticurveProviderDiscount result = ProviderUtils.mergeDiscountingProviders(providers);
    return ProviderUtils.mergeDiscountingProviders(result, matrix);
  }

  private static FXMatrix getFXMatrix(final FunctionInputs inputs, final ComputationTarget target, final SecuritySource securitySource) {
    final FXMatrix fxMatrix = new FXMatrix();
    final Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(target.getTrade().getSecurity(), securitySource);
    final Iterator<Currency> iter = currencies.iterator();
    final Currency initialCurrency = iter.next();
    while (iter.hasNext()) {
      final Currency otherCurrency = iter.next();
      final Double spotRate = (Double) inputs.getValue(new ValueRequirement(ValueRequirementNames.SPOT_RATE, CurrencyPair.TYPE.specification(CurrencyPair
          .of(otherCurrency, initialCurrency))));
      if (spotRate != null) {
        fxMatrix.addCurrency(otherCurrency, initialCurrency, spotRate);
      }
    }
    return fxMatrix;
  }

}
