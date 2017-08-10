/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.multicurve.MultiCurveUtils;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Calculates the yield curve node sensitivities of instruments using curves constructed using the discounting method.
 */
public class DiscountingYCNSFunction extends DiscountingFunction {
  static final OGMatrixAlgebra ALG = new OGMatrixAlgebra();

  /**
   * The constraint name to select the currency for which the sensitivity is returned.
   */
  public static final String SENSITIVITY_CURRENCY_PROPERTY = "SensitivityCurrency";

  /**
   * Sets the value requirements to {@link ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES}.
   */
  public DiscountingYCNSFunction() {
    super(YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new DiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final MultipleCurrencyParameterSensitivity sensitivities = (MultipleCurrencyParameterSensitivity) inputs.getValue(BLOCK_CURVE_SENSITIVITIES);
        final Map<Pair<String, Currency>, DoubleMatrix1D> sensitivityEntries = sensitivities.getSensitivities();
        final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(desiredValues.size());
        final Collection<Currency> securityCurrencies = FinancialSecurityUtils.getCurrencies(target.getTrade().getSecurity(),
            OpenGammaExecutionContext.getSecuritySource(executionContext));
        for (final ValueRequirement desiredValue : desiredValues) {
          final String curveName = desiredValue.getConstraint(CURVE);
          final CurveSpecification curveSpecification =
              (CurveSpecification) inputs.getValue(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL,
                  ValueProperties.builder().with(CURVE, curveName).get()));
          final String currency = desiredValue.getConstraints().getSingleValue(SENSITIVITY_CURRENCY_PROPERTY);
          DoubleMatrix1D sensitivityMatrix = null;
          if (currency != null) {
            // might be in the other currency - get matrix and convert
            final Currency ccy = Currency.of(currency);
            for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : sensitivityEntries.entrySet()) {
              if (curveName.equals(entry.getKey().getFirst())) {
                // found a match, now use matrix to convert elements or get from inputs if currency is not put or call currency
                final Double conversion;
                if (securityCurrencies.contains(ccy)) {
                  conversion = fxMatrix.getFxRate(entry.getKey().getSecond(), ccy);
                } else {
                  conversion = (Double) inputs.getComputedValue(new ValueRequirement(ValueRequirementNames.SPOT_RATE,
                          CurrencyPair.TYPE.specification(CurrencyPair.of(entry.getKey().getSecond(), ccy)))).getValue();
                  if (conversion == null) {
                    throw new OpenGammaRuntimeException("Could not get FX conversion for " + entry.getKey().getSecond() + "/" + ccy);
                  }
                }
                sensitivityMatrix = (DoubleMatrix1D) ALG.scale(entry.getValue(), conversion);
                break;
              }
            }
          } else {
            // No currency constraint so make an arbitrary choice.
            for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> sensitivityEntry : sensitivityEntries.entrySet()) {
              if (curveName.equals(sensitivityEntry.getKey().getFirst())) {
                sensitivityMatrix = sensitivityEntry.getValue();
                break;
              }
            }
          }
          if (sensitivityMatrix == null) {
            final double[] zeroes = new double[curveSpecification.getNodes().size()];
            sensitivityMatrix = new DoubleMatrix1D(zeroes);
          }
          final ValueSpecification valueSpec = new ValueSpecification(YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), desiredValue.getConstraints());
          final DoubleLabelledMatrix1D ycns = MultiCurveUtils.getLabelledMatrix(sensitivityMatrix, curveSpecification);
          results.add(new ComputedValue(valueSpec, ycns));
        }
        return results;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target,
          final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          return null;
        }
        final Set<String> curveExposureConfigs = constraints.getValues(CURVE_EXPOSURES);
        if (curveExposureConfigs == null) {
          return null;
        }
        final ValueProperties.Builder builder = ValueProperties
            .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
            .with(CURVE_EXPOSURES, curveExposureConfigs);
        final String sensitivityCurrency = constraints.getSingleValue(SENSITIVITY_CURRENCY_PROPERTY);
        final Set<ValueRequirement> requirements = new HashSet<>();
        final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
        final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
        final Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(security, securitySource);
        if (sensitivityCurrency != null) {
          builder.with(SENSITIVITY_CURRENCY_PROPERTY, sensitivityCurrency).withOptional(SENSITIVITY_CURRENCY_PROPERTY);
          final Currency ccy = Currency.of(sensitivityCurrency);
          for (final Currency currency : currencies) {
            if (!currency.equals(ccy)) {
              requirements.add(new ValueRequirement(ValueRequirementNames.SPOT_RATE,
                  CurrencyPair.TYPE.specification(CurrencyPair.of(currency, ccy))));
            }
          }
        }
        final ValueProperties properties = builder.get();
        final ValueProperties curveProperties = ValueProperties.with(CURVE, curveNames).get();

        requirements.add(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, curveProperties));
        requirements.add(new ValueRequirement(BLOCK_CURVE_SENSITIVITIES, target.toSpecification(), properties));
        requirements.addAll(getFXRequirements(security, securitySource));
        final Set<ValueRequirement> tsRequirements = getTimeSeriesRequirements(context, target);
        if (tsRequirements == null) {
          return null;
        }
        requirements.addAll(tsRequirements);
        return requirements;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target,
          final Map<ValueSpecification, ValueRequirement> inputs) {
        final Set<ValueSpecification> results = getResults(compilationContext, target);
        String sensitivityCurrency = null;
        for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
          final ValueRequirement resolved = entry.getValue();
          if (resolved.getValueName().equals(BLOCK_CURVE_SENSITIVITIES)) {
            sensitivityCurrency = resolved.getConstraint(SENSITIVITY_CURRENCY_PROPERTY);
            break;
          }
        }
        final Set<ValueSpecification> specificResults = new HashSet<>();
        final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
        final String payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor()).getCode();
        final String receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor()).getCode();
        for (final ValueSpecification result : results) {
          final ValueProperties.Builder properties = result.getProperties().copy()
              .withoutAny(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY)
              .withoutAny(CURRENCY);
          if (sensitivityCurrency != null) {
            // want sensitivities for a third currency - will return zeroes, but avoids empty results
            specificResults.add(new ValueSpecification(result.getValueName(), result.getTargetSpecification(),
                properties.copy().with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, sensitivityCurrency).with(CURRENCY, payCurrency).get()));
            specificResults.add(new ValueSpecification(result.getValueName(), result.getTargetSpecification(),
                properties.copy().with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, sensitivityCurrency).with(CURRENCY, receiveCurrency).get()));
          } else {
            specificResults.add(new ValueSpecification(result.getValueName(), result.getTargetSpecification(),
                properties.copy().with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, payCurrency).with(CURRENCY, payCurrency).get()));
            specificResults.add(new ValueSpecification(result.getValueName(), result.getTargetSpecification(),
                properties.copy().with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, payCurrency).with(CURRENCY, receiveCurrency).get()));
            specificResults.add(new ValueSpecification(result.getValueName(), result.getTargetSpecification(),
                properties.copy().with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, receiveCurrency).with(CURRENCY, payCurrency).get()));
            specificResults.add(new ValueSpecification(result.getValueName(), result.getTargetSpecification(),
                properties.copy().with(DiscountingYCNSFunction.SENSITIVITY_CURRENCY_PROPERTY, receiveCurrency).with(CURRENCY, receiveCurrency).get()));
          }
        }
        return specificResults;
      }

      private Currency getLegCurrency(final SwapLeg leg) {
        return leg.getNotional() instanceof InterestRateNotional ? ((InterestRateNotional) leg.getNotional()).getCurrency() : null;
      }

      @Override
      protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        return getResultProperties(target, true);
      }

      @SuppressWarnings("synthetic-access")
      private Collection<ValueProperties.Builder> getResultProperties(final ComputationTarget target, final boolean withAny) {
        final ValueProperties.Builder properties = createValueProperties().with(PROPERTY_CURVE_TYPE, DISCOUNTING).withAny(CURVE_EXPOSURES).withAny(CURVE);
        final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
        if (security instanceof SwapSecurity && InterestRateInstrumentType.isFixedIncomeInstrumentType(security)) {
          final SwapSecurity swapSecurity = (SwapSecurity) security;
          final Currency pay = getLegCurrency(swapSecurity.getPayLeg());
          final Currency receive = getLegCurrency(swapSecurity.getReceiveLeg());
          return addCurrencies(properties, pay, receive, withAny);
        } else if (security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity) {
          final Currency pay = security.accept(ForexVisitors.getPayCurrencyVisitor());
          final Currency receive = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
          return addCurrencies(properties, pay, receive, withAny);
        } else {
          final String ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
          return Collections.singleton(properties.with(SENSITIVITY_CURRENCY_PROPERTY, ccy).with(CURRENCY, ccy));
        }
      }

      private Collection<ValueProperties.Builder> addCurrencies(final ValueProperties.Builder properties, final Currency c1, final Currency c2,
          final boolean withAny) {
        if (c1 != null) {
          final String c1Code = c1.getCode();
          if (c2 != null) {
            final String c2Code = c2.getCode();
            final List<ValueProperties.Builder> result = new ArrayList<>();
            if (withAny) {
              result.add(properties.copy().withAny(SENSITIVITY_CURRENCY_PROPERTY).withoutAny(CURRENCY).with(CURRENCY, c1Code));
              result.add(properties.withAny(SENSITIVITY_CURRENCY_PROPERTY).withoutAny(CURRENCY).with(CURRENCY, c2Code));
            } else {
              result.add(properties.copy().with(SENSITIVITY_CURRENCY_PROPERTY, c1Code).withoutAny(CURRENCY).with(CURRENCY, c1Code));
              result.add(properties.with(SENSITIVITY_CURRENCY_PROPERTY, c2Code).withoutAny(CURRENCY).with(CURRENCY, c2Code));
            }
            return result;
          }
          if (withAny) {
            return Collections.singleton(properties.withAny(SENSITIVITY_CURRENCY_PROPERTY).with(CURRENCY, c1Code));
          }
          return Collections.singleton(properties.with(SENSITIVITY_CURRENCY_PROPERTY, c1Code).with(CURRENCY, c1Code));
        }
        if (c2 != null) {
          if (withAny) {
            return Collections.singleton(properties.withAny(SENSITIVITY_CURRENCY_PROPERTY).with(CURRENCY, c2.getCode()));
          }
          return Collections.singleton(properties.with(SENSITIVITY_CURRENCY_PROPERTY, c2.getCode()).with(CURRENCY, c2.getCode()));
        }
        return Collections.singleton(properties.withAny(SENSITIVITY_CURRENCY_PROPERTY).withAny(CURRENCY));
      }

    };
  }
}
