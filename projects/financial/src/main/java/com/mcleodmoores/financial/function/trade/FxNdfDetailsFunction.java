/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.trade;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.FX_NDF_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.PAY_AMOUNT;
import static com.opengamma.engine.value.ValueRequirementNames.PAY_DISCOUNT_FACTOR;
import static com.opengamma.engine.value.ValueRequirementNames.PAY_ZERO_RATE;
import static com.opengamma.engine.value.ValueRequirementNames.RECEIVE_AMOUNT;
import static com.opengamma.engine.value.ValueRequirementNames.RECEIVE_DISCOUNT_FACTOR;
import static com.opengamma.engine.value.ValueRequirementNames.RECEIVE_ZERO_RATE;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ProviderUtils;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.analytics.conversion.NonDeliverableFXForwardSecurityConverter;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.exposure.ConfigDBInstrumentExposuresProvider;
import com.opengamma.financial.analytics.curve.exposure.InstrumentExposuresProvider;
import com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Returns pricing information for FX forwards.
 */
public class FxNdfDetailsFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger LOGGER = LoggerFactory.getLogger(FxNdfDetailsFunction.class);
  private static final NonDeliverableFXForwardSecurityConverter SECURITY_CONVERTER = new NonDeliverableFXForwardSecurityConverter();
  private CurveConstructionConfigurationSource _curveConstructionConfigurationSource;
  private InstrumentExposuresProvider _instrumentExposuresProvider;

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveConstructionConfigurationSource = ConfigDBCurveConstructionConfigurationSource.init(context, this);
    _instrumentExposuresProvider = ConfigDBInstrumentExposuresProvider.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueProperties.Builder curvePropertiesBuilder = createValueProperties();
    for (final ValueRequirement requirement : desiredValues) {
      if (requirement.getConstraint(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE) != null) {
        curvePropertiesBuilder.with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE,
            requirement.getConstraint(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE));
        curvePropertiesBuilder.with(ValuePropertyNames.CURVE_EXPOSURES,
            requirement.getConstraint(ValuePropertyNames.CURVE_EXPOSURES));
        break;
      }
    }
    final ValueProperties curveProperties = curvePropertiesBuilder.get();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final MulticurveProviderInterface curves = getMergedProviders(inputs, target, executionContext.getSecuritySource());
    final NonDeliverableFXForwardSecurity security = (NonDeliverableFXForwardSecurity) target.getTrade().getSecurity();
    final InstrumentDerivative instrument = security.accept(SECURITY_CONVERTER).toDerivative(now);
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final CurrencyAmount pay = CurrencyAmount.of(payCurrency, security.accept(ForexVisitors.getPayAmountVisitor()));
    final CurrencyAmount receive = CurrencyAmount.of(receiveCurrency, security.accept(ForexVisitors.getReceiveAmountVisitor()));
    final double paymentTime = instrument.accept(LastTimeCalculator.getInstance());
    final double payDiscountFactor = curves.getDiscountFactor(payCurrency, paymentTime);
    final double receiveDiscountFactor = curves.getDiscountFactor(receiveCurrency, paymentTime);
    final double payZeroRate = -Math.log(payDiscountFactor) / paymentTime;
    final double receiveZeroRate = -Math.log(receiveDiscountFactor) / paymentTime;
    final FxNdfDetails.Builder details = FxNdfDetails.builder()
        .withPayAmount(pay)
        .withReceiveAmount(receive)
        .withPayDiscountFactor(payDiscountFactor)
        .withReceiveDiscountFactor(receiveDiscountFactor)
        .withPayZeroRate(payZeroRate)
        .withReceiveZeroRate(receiveZeroRate)
        .withPaymentTime(paymentTime);
    if (security.isDeliverInReceiveCurrency()) {
      details.deliverInReceiveCurrency();
    }
    final ValueProperties emptyProperties = createValueProperties().get();
    final Set<ComputedValue> results = new HashSet<>();
    results.add(new ComputedValue(new ValueSpecification(PAY_AMOUNT, target.toSpecification(), emptyProperties), pay));
    results.add(new ComputedValue(new ValueSpecification(RECEIVE_AMOUNT, target.toSpecification(), emptyProperties), receive));
    results.add(new ComputedValue(new ValueSpecification(PAY_DISCOUNT_FACTOR, target.toSpecification(), curveProperties), payDiscountFactor));
    results.add(new ComputedValue(new ValueSpecification(RECEIVE_DISCOUNT_FACTOR, target.toSpecification(), curveProperties), receiveDiscountFactor));
    results.add(new ComputedValue(new ValueSpecification(PAY_ZERO_RATE, target.toSpecification(), curveProperties), payZeroRate));
    results.add(new ComputedValue(new ValueSpecification(RECEIVE_ZERO_RATE, target.toSpecification(), curveProperties), receiveZeroRate));
    results.add(new ComputedValue(new ValueSpecification(FX_NDF_DETAILS, target.toSpecification(), curveProperties), details));
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    return security instanceof FXForwardSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final Set<ValueSpecification> results = new HashSet<>();
    final ValueProperties emptyProperties = createValueProperties().get();
    final ValueProperties curveProperties = createValueProperties()
        .withAny(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE)
        .withAny(ValuePropertyNames.CURVE_EXPOSURES)
        .get();
    results.add(new ValueSpecification(PAY_AMOUNT, target.toSpecification(), emptyProperties));
    results.add(new ValueSpecification(RECEIVE_AMOUNT, target.toSpecification(), emptyProperties));
    results.add(new ValueSpecification(PAY_DISCOUNT_FACTOR, target.toSpecification(), curveProperties));
    results.add(new ValueSpecification(RECEIVE_DISCOUNT_FACTOR, target.toSpecification(), curveProperties));
    results.add(new ValueSpecification(PAY_ZERO_RATE, target.toSpecification(), curveProperties));
    results.add(new ValueSpecification(RECEIVE_ZERO_RATE, target.toSpecification(), curveProperties));
    results.add(new ValueSpecification(FX_NDF_DETAILS, target.toSpecification(), curveProperties));
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target,
      final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final String curveType = constraints.getSingleValue(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE);
    final ValueProperties.Builder curveProperties = ValueProperties.builder();
    if (curveType == null) {
      curveProperties.withAny(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE);
    } else {
      curveProperties.with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, curveType);
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
          final ValueProperties curveBundleConstraints = curveProperties.copy()
              .with(CURVE_CONSTRUCTION_CONFIG, curveConstructionConfigurationName)
              .with(CURVE_EXPOSURES, curveExposureConfig).withOptional(CURVE_EXPOSURES)
              .get();
          requirements.add(new ValueRequirement(CURVE_BUNDLE, ComputationTargetSpecification.NULL, curveBundleConstraints));
          requirements.add(new ValueRequirement(JACOBIAN_BUNDLE, ComputationTargetSpecification.NULL, curveBundleConstraints));
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
        curveType = value.getConstraint(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE);
        curveExposuresName = value.getConstraint(CURVE_EXPOSURES);
        break;
      }
    }
    if (curveType == null) {
      return null;
    }
    if (curveExposuresName == null) {
      return null;
    }
    final ValueProperties emptyProperties = createValueProperties().get();
    final ValueProperties curveProperties = createValueProperties()
        .with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, curveType)
        .with(ValuePropertyNames.CURVE_EXPOSURES, curveExposuresName)
        .get();
    final Set<ValueSpecification> results = new HashSet<>();
    results.add(new ValueSpecification(PAY_AMOUNT, target.toSpecification(), emptyProperties));
    results.add(new ValueSpecification(RECEIVE_AMOUNT, target.toSpecification(), emptyProperties));
    results.add(new ValueSpecification(PAY_DISCOUNT_FACTOR, target.toSpecification(), curveProperties));
    results.add(new ValueSpecification(RECEIVE_DISCOUNT_FACTOR, target.toSpecification(), curveProperties));
    results.add(new ValueSpecification(PAY_ZERO_RATE, target.toSpecification(), curveProperties));
    results.add(new ValueSpecification(RECEIVE_ZERO_RATE, target.toSpecification(), curveProperties));
    results.add(new ValueSpecification(FX_NDF_DETAILS, target.toSpecification(), curveProperties));
    return results;
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
