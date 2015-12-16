/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.financial.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.model.curve.FXMatrixFunction;
import com.opengamma.util.money.Currency;

/**
 * A function that constructs the {@link com.opengamma.analytics.financial.forex.method.FXMatrix} required to construct a bundle of
 * curves that can handle curves defined using Quandl data.
 */
public class QuandlFxMatrixFunction extends FXMatrixFunction {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlFxMatrixFunction.class);
  /** The configuration name */
  private final String _configurationName;
  /** A curve construction configuration source */
  private CurveConstructionConfigurationSource _curveConstructionConfigurationSource;
  /** A curve definition source */
  private CurveDefinitionSource _curveDefinitionSource;

  /**
   * @param configurationName The configuration name, not null
   */
  public QuandlFxMatrixFunction(final String configurationName) {
    super(configurationName);
    ArgumentChecker.notNull(configurationName, "configuration name");
    _configurationName = configurationName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveConstructionConfigurationSource = ConfigDBCurveConstructionConfigurationSource.init(context, this);
    _curveDefinitionSource = ConfigDBCurveDefinitionSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    //TODO work out a way to use dependency graph to get curve information for this config
    final CurveConstructionConfiguration curveConstructionConfiguration =
        _curveConstructionConfigurationSource.getCurveConstructionConfiguration(_configurationName);
    if (curveConstructionConfiguration == null) {
      throw new OpenGammaRuntimeException("Could not get curve construction configuration called " + _configurationName);
    }
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    try {
      final CurveNodeVisitor<Set<Currency>> visitor = new QuandlCurveNodeCurrencyVisitor(conventionSource, securitySource, configSource);
      final Set<Currency> currencies = CurveUtils.getCurrencies(curveConstructionConfiguration, _curveDefinitionSource, _curveConstructionConfigurationSource,
          visitor);
      final ValueProperties properties = createValueProperties().with(CURVE_CONSTRUCTION_CONFIG, _configurationName).get();
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FX_MATRIX, ComputationTargetSpecification.NULL, properties);
      return new MyCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000), spec, currencies);
    } catch (final Throwable e) {
      LOGGER.error("{}: problem in CurveConstructionConfiguration called {}", e.getMessage(), _configurationName);
      LOGGER.error("Full stack trace", e);
      throw new OpenGammaRuntimeException(e.getMessage() + ": problem in CurveConstructionConfiguration called " + _configurationName);
    }
  }
}
