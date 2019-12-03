/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.PortfolioStructure;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Bean for constructing an {@link OpenGammaCompilationContext}.
 */
public class OpenGammaCompilationContextFactoryBean extends SingletonFactoryBean<FunctionCompilationContext> {

  private ConfigSource _configSource;
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private RegionSource _regionSource;
  private LegalEntitySource _legalEntitySource;
  private VolatilityCubeDefinitionSource _volatilityCubeDefinitionSource;
  private HolidaySource _holidaySource;
  private ExchangeSource _exchangeSource;
  private ComputationTargetResolver _targetResolver;
  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  private HistoricalTimeSeriesResolver _historicalTimeSeriesResolver;
  private ConventionSource _conventionSource;

  public void setSecuritySource(final SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public void setPositionSource(final PositionSource positionSource) {
    _positionSource = positionSource;
  }

  public PositionSource getPositionSource() {
    return _positionSource;
  }

  public void setLegalEntitySource(final LegalEntitySource legalEntitySource) {
    _legalEntitySource = legalEntitySource;
  }

  public LegalEntitySource getLegalEntitySource() {
    return _legalEntitySource;
  }

  public void setComputationTargetResolver(final ComputationTargetResolver targetResolver) {
    _targetResolver = targetResolver;
  }

  public ComputationTargetResolver getComputationTargetResolver() {
    return _targetResolver;
  }

  public void setRegionSource(final RegionSource regionSource) {
    _regionSource = regionSource;
  }

  public RegionSource getRegionSource() {
    return _regionSource;
  }

  public void setConventionSource(final ConventionSource conventionSource) {
    _conventionSource = conventionSource;
  }

  public ConventionSource getConventionSource() {
    return _conventionSource;
  }

  public void setConfigSource(final ConfigSource configSource) {
    _configSource = configSource;
  }

  public ConfigSource getConfigSource() {
    return _configSource;
  }

  public VolatilityCubeDefinitionSource getVolatilityCubeDefinitionSource() {
    return _volatilityCubeDefinitionSource;
  }

  public void setVolatilityCubeDefinitionSource(final VolatilityCubeDefinitionSource volatilityCubeDefinitionSource) {
    _volatilityCubeDefinitionSource = volatilityCubeDefinitionSource;
  }

  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  public void setHolidaySource(final HolidaySource holidaySource) {
    _holidaySource = holidaySource;
  }

  public ExchangeSource getExchangeSource() {
    return _exchangeSource;
  }

  public void setExchangeSource(final ExchangeSource exchangeSource) {
    _exchangeSource = exchangeSource;
  }

  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  public void setHistoricalTimeSeriesSource(final HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    _historicalTimeSeriesSource = historicalTimeSeriesSource;
  }

  public HistoricalTimeSeriesResolver getHistoricalTimeSeriesResolver() {
    return _historicalTimeSeriesResolver;
  }

  public void setHistoricalTimeSeriesResolver(final HistoricalTimeSeriesResolver historicalTimeSeriesResolver) {
    _historicalTimeSeriesResolver = historicalTimeSeriesResolver;
  }

  private void configureCompilationContext(final FunctionCompilationContext context) {
    if (getConfigSource() != null) {
      OpenGammaCompilationContext.setConfigSource(context, getConfigSource());
    }
    if (getRegionSource() != null) {
      OpenGammaCompilationContext.setRegionSource(context, getRegionSource());
    }
    if (getConventionSource() != null) {
      OpenGammaCompilationContext.setConventionSource(context, getConventionSource());
    }
    if (getVolatilityCubeDefinitionSource() != null) {
      OpenGammaCompilationContext.setVolatilityCubeDefinitionSource(context, getVolatilityCubeDefinitionSource());
    }
    if (getHolidaySource() != null) {
      OpenGammaCompilationContext.setHolidaySource(context, getHolidaySource());
    }
    if (getExchangeSource() != null) {
      OpenGammaCompilationContext.setExchangeSource(context, getExchangeSource());
    }
    if (getHistoricalTimeSeriesSource() != null) {
      OpenGammaCompilationContext.setHistoricalTimeSeriesSource(context, getHistoricalTimeSeriesSource());
    }
    if (getHistoricalTimeSeriesResolver() != null) {
      OpenGammaCompilationContext.setHistoricalTimeSeriesResolver(context, getHistoricalTimeSeriesResolver());
    }
    if (getLegalEntitySource() != null) {
      context.setLegalEntitySource(getLegalEntitySource());
    }
    context.setSecuritySource(getSecuritySource());
    context.setRawComputationTargetResolver(getComputationTargetResolver());
    context.setPortfolioStructure(new PortfolioStructure(getPositionSource()));
  }

  @Override
  protected FunctionCompilationContext createObject() {
    final FunctionCompilationContext context = new FunctionCompilationContext();
    configureCompilationContext(context);
    return context;
  }

}
