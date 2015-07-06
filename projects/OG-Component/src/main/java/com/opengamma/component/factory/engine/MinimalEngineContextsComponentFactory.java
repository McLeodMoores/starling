/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.component.factory.engine;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
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
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.PortfolioStructure;
import com.opengamma.engine.function.blacklist.DefaultFunctionBlacklistQuery;
import com.opengamma.engine.function.blacklist.FunctionBlacklist;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.marketdata.MarketDataELCompiler;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;

/**
 * A version of {@link com.opengamma.component.factory.engine.EngineContextsComponentFactory} that does not
 * reference deprecated components or components that are only used by deprecated functionality (e.g.
 * {@link com.opengamma.financial.convention.ConventionBundleSource}. The only sources that are required
 * to be populated are the {@link SecuritySource} and {@link PositionSource}. The {@link ComputationTargetResolver}
 * must also have a value.
 * <p>
 * This factory only populates {@link com.opengamma.core.Source}s, although it could be extended to use masters
 * if required.
 */
@BeanDefinition
public class MinimalEngineContextsComponentFactory extends AbstractComponentFactory {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(MinimalEngineContextsComponentFactory.class);

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;

  /**
   * The security source.
   */
  @PropertyDefinition(validate = "notNull")
  private SecuritySource _securitySource;

  /**
   * The position source.
   */
  @PropertyDefinition(validate = "notNull")
  private PositionSource _positionSource;

  /**
   * The target resolver.
   */
  @PropertyDefinition(validate = "notNull")
  private ComputationTargetResolver _targetResolver;

  /**
   * The config source.
   */
  @PropertyDefinition
  private ConfigSource _configSource;

  /**
   * The convention source.
   */
  @PropertyDefinition
  private ConventionSource _conventionSource;

  /**
   * The exchange source.
   */
  @PropertyDefinition
  private ExchangeSource _exchangeSource;

  /**
   * The historical time series source.
   */
  @PropertyDefinition
  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;

  /**
   * The historical time series resolver.
   */
  @PropertyDefinition
  private HistoricalTimeSeriesResolver _historicalTimeSeriesResolver;

  /**
   * The holiday source.
   */
  @PropertyDefinition
  private HolidaySource _holidaySource;

  /**
   * The legal entity source.
   */
  @PropertyDefinition
  private LegalEntitySource _legalEntitySource;

  /**
   * The region source.
   */
  @PropertyDefinition
  private RegionSource _regionSource;

  /**
   * The execution blacklist. View processors will not submit nodes matched by this blacklist for execution.
   */
  @PropertyDefinition
  private FunctionBlacklist _executionBlacklist;

  /**
   * The compilation blacklist. Dependency graph builders will not produce graphs which contain nodes matched by this blacklist.
   */
  @PropertyDefinition
  private FunctionBlacklist _compilationBlacklist;

  /**
   * The temporary target repository.
   */
  @PropertyDefinition
  private TempTargetRepository _tempTargetRepository;

  /**
   * The slave view processor that executing functions can make requests to. This might be the view processor that owns the context,
   * but might be a different but compatible one.
   */
  @PropertyDefinition
  private ViewProcessor _viewProcessor;

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    initThreadLocalServiceContext();
    initFunctionCompilationContext(repo, configuration);
    final OverrideOperationCompiler ooc = initOverrideOperationCompiler(repo, configuration);
    initFunctionExecutionContext(repo, configuration, ooc);
  }

  /**
   * Initializes the service context.
   */
  private void initThreadLocalServiceContext() {
    final VersionCorrectionProvider vcProvider = new VersionCorrectionProvider() {
      @Override
      public VersionCorrection getPortfolioVersionCorrection() {
        return VersionCorrection.LATEST;
      }
      @Override
      public VersionCorrection getConfigVersionCorrection() {
        return VersionCorrection.LATEST;
      }
    };
    final ImmutableMap.Builder<Class<?>, Object> services = ImmutableMap.<Class<?>, Object>builder()
        .put(PositionSource.class, getPositionSource())
        .put(SecuritySource.class, getSecuritySource());
    if (getConfigSource() != null) {
      services.put(ConfigSource.class, getConfigSource());
    }
    if (getConventionSource() != null) {
      services.put(ConventionSource.class, getConventionSource());
    }
    if (getExchangeSource() != null) {
      services.put(ExchangeSource.class, getExchangeSource());
    }
    if (getHistoricalTimeSeriesResolver() != null) {
      services.put(HistoricalTimeSeriesResolver.class, getHistoricalTimeSeriesResolver());
    }
    if (getHistoricalTimeSeriesSource() != null) {
      services.put(HistoricalTimeSeriesSource.class, getHistoricalTimeSeriesSource());
    }
    if (getHolidaySource() != null) {
      services.put(HolidaySource.class, getHolidaySource());
    }
    if (getLegalEntitySource() != null) {
      services.put(LegalEntitySource.class, getLegalEntitySource());
    }
    if (getRegionSource() != null) {
      services.put(RegionSource.class, getRegionSource());
    }
    services.put(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(ServiceContext.of(services.build()));
  }

  /**
   * Initializes the compilation context.
   * @param repo  the component repository
   * @param configuration  the configuration
   */
  protected void initFunctionCompilationContext(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final FunctionCompilationContext context = new FunctionCompilationContext();
    context.setSecuritySource(getSecuritySource());
    context.setPortfolioStructure(new PortfolioStructure(getPositionSource()));
    context.setRawComputationTargetResolver(getTargetResolver());
    if (getConfigSource() != null) {
      OpenGammaCompilationContext.setConfigSource(context, getConfigSource());
    } else {
      LOGGER.warn("ConfigSource not set for OpenGammaCompilationContext");
    }
    if (getConventionSource() != null) {
      OpenGammaCompilationContext.setConventionSource(context, getConventionSource());
    } else {
      LOGGER.warn("ConventionSource not set for OpenGammaCompilationContext");
    }
    if (getExchangeSource() != null) {
      OpenGammaCompilationContext.setExchangeSource(context, getExchangeSource());
    } else {
      LOGGER.warn("ExchangeSource not set for OpenGammaCompilationContext");
    }
    if (getHistoricalTimeSeriesResolver() != null) {
      OpenGammaCompilationContext.setHistoricalTimeSeriesResolver(context, getHistoricalTimeSeriesResolver());
    } else {
      LOGGER.warn("HistoricalTimeSeriesResolver not set for OpenGammaCompilationContext");
    }
    if (getHistoricalTimeSeriesSource() != null) {
      OpenGammaCompilationContext.setHistoricalTimeSeriesSource(context, getHistoricalTimeSeriesSource());
    } else {
      LOGGER.warn("HistoricalTimeSeriesSource not set for OpenGammaCompilationContext");
    }
    if (getHolidaySource() != null) {
      OpenGammaCompilationContext.setHolidaySource(context, getHolidaySource());
    } else {
      LOGGER.warn("HolidaySource not set for OpenGammaCompilationContext");
    }
    if (getLegalEntitySource() != null) {
      OpenGammaCompilationContext.setLegalEntitySource(context, getLegalEntitySource());
    } else {
      LOGGER.warn("LegalEntitySource not set for OpenGammaCompilationContext");
    }
    if (getRegionSource() != null) {
      OpenGammaCompilationContext.setRegionSource(context, getRegionSource());
    } else {
      LOGGER.warn("RegionSource not set for OpenGammaCompilationContext");
    }
    if (getCompilationBlacklist() != null) {
      context.setGraphBuildingBlacklist(new DefaultFunctionBlacklistQuery(getCompilationBlacklist()));
    }
    if (getExecutionBlacklist() != null) {
      context.setGraphExecutionBlacklist(new DefaultFunctionBlacklistQuery(getExecutionBlacklist()));
    }
    if (getTempTargetRepository() != null) {
      OpenGammaCompilationContext.setTempTargets(context, getTempTargetRepository());
    }
    final ComponentInfo info = new ComponentInfo(FunctionCompilationContext.class, getClassifier());
    repo.registerComponent(info, context);
  }

  /**
   * Initializes the market data override compiler.
   * @param repo  the component repository
   * @param configuration  the configuration
   * @return  the override compiler
   */
  protected OverrideOperationCompiler initOverrideOperationCompiler(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final OverrideOperationCompiler ooc = new MarketDataELCompiler();
    final ComponentInfo info = new ComponentInfo(OverrideOperationCompiler.class, getClassifier());
    repo.registerComponent(info, ooc);
    return ooc;
  }

  /**
   * Initializes the execution context.
   * @param repo  the component repository
   * @param configuration  the configuration
   * @param ooc  the override compiler
   */
  protected void initFunctionExecutionContext(final ComponentRepository repo, final LinkedHashMap<String, String> configuration,
      final OverrideOperationCompiler ooc) {
    final FunctionExecutionContext context = new FunctionExecutionContext();
    context.setSecuritySource(getSecuritySource());
    context.setPortfolioStructure(new PortfolioStructure(getPositionSource()));
    if (getConfigSource() != null) {
      OpenGammaExecutionContext.setConfigSource(context, getConfigSource());
    } else {
      LOGGER.warn("ConfigSource not set for OpenGammaExecutionContext");
    }
    if (getConventionSource() != null) {
      OpenGammaExecutionContext.setConventionSource(context, getConventionSource());
    } else {
      LOGGER.warn("ConventionSource not set for OpenGammaExecutionContext");
    }
    if (getExchangeSource() != null) {
      OpenGammaExecutionContext.setExchangeSource(context, getExchangeSource());
    } else {
      LOGGER.warn("ExchangeSource not set for OpenGammaExecutionContext");
    }
    if (getHistoricalTimeSeriesSource() != null) {
      OpenGammaExecutionContext.setHistoricalTimeSeriesSource(context, getHistoricalTimeSeriesSource());
    } else {
      LOGGER.warn("HistoricalTimeSeriesSource not set for OpenGammaExecutionContext");
    }
    if (getHolidaySource() != null) {
      OpenGammaExecutionContext.setHolidaySource(context, getHolidaySource());
    } else {
      LOGGER.warn("HolidaySource not set for OpenGammaExecutionContext");
    }
    if (getLegalEntitySource() != null) {
      OpenGammaExecutionContext.setLegalEntitySource(context, getLegalEntitySource());
    } else {
      LOGGER.warn("LegalEntitySource not set for OpenGammaExecutionContext");
    }
    if (getRegionSource() != null) {
      OpenGammaExecutionContext.setRegionSource(context, getRegionSource());
    } else {
      LOGGER.warn("RegionSource not set for OpenGammaExecutionContext");
    }
    OpenGammaExecutionContext.setOverrideOperationCompiler(context, ooc);
    if (getViewProcessor() != null) {
      OpenGammaExecutionContext.setViewProcessor(context, getViewProcessor());
    }
    final ComponentInfo info = new ComponentInfo(FunctionExecutionContext.class, getClassifier());
    repo.registerComponent(info, context);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MinimalEngineContextsComponentFactory}.
   * @return the meta-bean, not null
   */
  public static MinimalEngineContextsComponentFactory.Meta meta() {
    return MinimalEngineContextsComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MinimalEngineContextsComponentFactory.Meta.INSTANCE);
  }

  @Override
  public MinimalEngineContextsComponentFactory.Meta metaBean() {
    return MinimalEngineContextsComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security source.
   * @return the value of the property, not null
   */
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Sets the security source.
   * @param securitySource  the new value of the property, not null
   */
  public void setSecuritySource(SecuritySource securitySource) {
    JodaBeanUtils.notNull(securitySource, "securitySource");
    this._securitySource = securitySource;
  }

  /**
   * Gets the the {@code securitySource} property.
   * @return the property, not null
   */
  public final Property<SecuritySource> securitySource() {
    return metaBean().securitySource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the position source.
   * @return the value of the property, not null
   */
  public PositionSource getPositionSource() {
    return _positionSource;
  }

  /**
   * Sets the position source.
   * @param positionSource  the new value of the property, not null
   */
  public void setPositionSource(PositionSource positionSource) {
    JodaBeanUtils.notNull(positionSource, "positionSource");
    this._positionSource = positionSource;
  }

  /**
   * Gets the the {@code positionSource} property.
   * @return the property, not null
   */
  public final Property<PositionSource> positionSource() {
    return metaBean().positionSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the target resolver.
   * @return the value of the property, not null
   */
  public ComputationTargetResolver getTargetResolver() {
    return _targetResolver;
  }

  /**
   * Sets the target resolver.
   * @param targetResolver  the new value of the property, not null
   */
  public void setTargetResolver(ComputationTargetResolver targetResolver) {
    JodaBeanUtils.notNull(targetResolver, "targetResolver");
    this._targetResolver = targetResolver;
  }

  /**
   * Gets the the {@code targetResolver} property.
   * @return the property, not null
   */
  public final Property<ComputationTargetResolver> targetResolver() {
    return metaBean().targetResolver().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config source.
   * @return the value of the property
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Sets the config source.
   * @param configSource  the new value of the property
   */
  public void setConfigSource(ConfigSource configSource) {
    this._configSource = configSource;
  }

  /**
   * Gets the the {@code configSource} property.
   * @return the property, not null
   */
  public final Property<ConfigSource> configSource() {
    return metaBean().configSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the convention source.
   * @return the value of the property
   */
  public ConventionSource getConventionSource() {
    return _conventionSource;
  }

  /**
   * Sets the convention source.
   * @param conventionSource  the new value of the property
   */
  public void setConventionSource(ConventionSource conventionSource) {
    this._conventionSource = conventionSource;
  }

  /**
   * Gets the the {@code conventionSource} property.
   * @return the property, not null
   */
  public final Property<ConventionSource> conventionSource() {
    return metaBean().conventionSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the exchange source.
   * @return the value of the property
   */
  public ExchangeSource getExchangeSource() {
    return _exchangeSource;
  }

  /**
   * Sets the exchange source.
   * @param exchangeSource  the new value of the property
   */
  public void setExchangeSource(ExchangeSource exchangeSource) {
    this._exchangeSource = exchangeSource;
  }

  /**
   * Gets the the {@code exchangeSource} property.
   * @return the property, not null
   */
  public final Property<ExchangeSource> exchangeSource() {
    return metaBean().exchangeSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the historical time series source.
   * @return the value of the property
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  /**
   * Sets the historical time series source.
   * @param historicalTimeSeriesSource  the new value of the property
   */
  public void setHistoricalTimeSeriesSource(HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    this._historicalTimeSeriesSource = historicalTimeSeriesSource;
  }

  /**
   * Gets the the {@code historicalTimeSeriesSource} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesSource> historicalTimeSeriesSource() {
    return metaBean().historicalTimeSeriesSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the historical time series resolver.
   * @return the value of the property
   */
  public HistoricalTimeSeriesResolver getHistoricalTimeSeriesResolver() {
    return _historicalTimeSeriesResolver;
  }

  /**
   * Sets the historical time series resolver.
   * @param historicalTimeSeriesResolver  the new value of the property
   */
  public void setHistoricalTimeSeriesResolver(HistoricalTimeSeriesResolver historicalTimeSeriesResolver) {
    this._historicalTimeSeriesResolver = historicalTimeSeriesResolver;
  }

  /**
   * Gets the the {@code historicalTimeSeriesResolver} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesResolver> historicalTimeSeriesResolver() {
    return metaBean().historicalTimeSeriesResolver().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the holiday source.
   * @return the value of the property
   */
  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  /**
   * Sets the holiday source.
   * @param holidaySource  the new value of the property
   */
  public void setHolidaySource(HolidaySource holidaySource) {
    this._holidaySource = holidaySource;
  }

  /**
   * Gets the the {@code holidaySource} property.
   * @return the property, not null
   */
  public final Property<HolidaySource> holidaySource() {
    return metaBean().holidaySource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the legal entity source.
   * @return the value of the property
   */
  public LegalEntitySource getLegalEntitySource() {
    return _legalEntitySource;
  }

  /**
   * Sets the legal entity source.
   * @param legalEntitySource  the new value of the property
   */
  public void setLegalEntitySource(LegalEntitySource legalEntitySource) {
    this._legalEntitySource = legalEntitySource;
  }

  /**
   * Gets the the {@code legalEntitySource} property.
   * @return the property, not null
   */
  public final Property<LegalEntitySource> legalEntitySource() {
    return metaBean().legalEntitySource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region source.
   * @return the value of the property
   */
  public RegionSource getRegionSource() {
    return _regionSource;
  }

  /**
   * Sets the region source.
   * @param regionSource  the new value of the property
   */
  public void setRegionSource(RegionSource regionSource) {
    this._regionSource = regionSource;
  }

  /**
   * Gets the the {@code regionSource} property.
   * @return the property, not null
   */
  public final Property<RegionSource> regionSource() {
    return metaBean().regionSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the execution blacklist. View processors will not submit nodes matched by this blacklist for execution.
   * @return the value of the property
   */
  public FunctionBlacklist getExecutionBlacklist() {
    return _executionBlacklist;
  }

  /**
   * Sets the execution blacklist. View processors will not submit nodes matched by this blacklist for execution.
   * @param executionBlacklist  the new value of the property
   */
  public void setExecutionBlacklist(FunctionBlacklist executionBlacklist) {
    this._executionBlacklist = executionBlacklist;
  }

  /**
   * Gets the the {@code executionBlacklist} property.
   * @return the property, not null
   */
  public final Property<FunctionBlacklist> executionBlacklist() {
    return metaBean().executionBlacklist().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the compilation blacklist. Dependency graph builders will not produce graphs which contain nodes matched by this blacklist.
   * @return the value of the property
   */
  public FunctionBlacklist getCompilationBlacklist() {
    return _compilationBlacklist;
  }

  /**
   * Sets the compilation blacklist. Dependency graph builders will not produce graphs which contain nodes matched by this blacklist.
   * @param compilationBlacklist  the new value of the property
   */
  public void setCompilationBlacklist(FunctionBlacklist compilationBlacklist) {
    this._compilationBlacklist = compilationBlacklist;
  }

  /**
   * Gets the the {@code compilationBlacklist} property.
   * @return the property, not null
   */
  public final Property<FunctionBlacklist> compilationBlacklist() {
    return metaBean().compilationBlacklist().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the temporary target repository.
   * @return the value of the property
   */
  public TempTargetRepository getTempTargetRepository() {
    return _tempTargetRepository;
  }

  /**
   * Sets the temporary target repository.
   * @param tempTargetRepository  the new value of the property
   */
  public void setTempTargetRepository(TempTargetRepository tempTargetRepository) {
    this._tempTargetRepository = tempTargetRepository;
  }

  /**
   * Gets the the {@code tempTargetRepository} property.
   * @return the property, not null
   */
  public final Property<TempTargetRepository> tempTargetRepository() {
    return metaBean().tempTargetRepository().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the slave view processor that executing functions can make requests to. This might be the view processor that owns the context,
   * but might be a different but compatible one.
   * @return the value of the property
   */
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  /**
   * Sets the slave view processor that executing functions can make requests to. This might be the view processor that owns the context,
   * but might be a different but compatible one.
   * @param viewProcessor  the new value of the property
   */
  public void setViewProcessor(ViewProcessor viewProcessor) {
    this._viewProcessor = viewProcessor;
  }

  /**
   * Gets the the {@code viewProcessor} property.
   * but might be a different but compatible one.
   * @return the property, not null
   */
  public final Property<ViewProcessor> viewProcessor() {
    return metaBean().viewProcessor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public MinimalEngineContextsComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MinimalEngineContextsComponentFactory other = (MinimalEngineContextsComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getSecuritySource(), other.getSecuritySource()) &&
          JodaBeanUtils.equal(getPositionSource(), other.getPositionSource()) &&
          JodaBeanUtils.equal(getTargetResolver(), other.getTargetResolver()) &&
          JodaBeanUtils.equal(getConfigSource(), other.getConfigSource()) &&
          JodaBeanUtils.equal(getConventionSource(), other.getConventionSource()) &&
          JodaBeanUtils.equal(getExchangeSource(), other.getExchangeSource()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesSource(), other.getHistoricalTimeSeriesSource()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesResolver(), other.getHistoricalTimeSeriesResolver()) &&
          JodaBeanUtils.equal(getHolidaySource(), other.getHolidaySource()) &&
          JodaBeanUtils.equal(getLegalEntitySource(), other.getLegalEntitySource()) &&
          JodaBeanUtils.equal(getRegionSource(), other.getRegionSource()) &&
          JodaBeanUtils.equal(getExecutionBlacklist(), other.getExecutionBlacklist()) &&
          JodaBeanUtils.equal(getCompilationBlacklist(), other.getCompilationBlacklist()) &&
          JodaBeanUtils.equal(getTempTargetRepository(), other.getTempTargetRepository()) &&
          JodaBeanUtils.equal(getViewProcessor(), other.getViewProcessor()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSecuritySource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPositionSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTargetResolver());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConventionSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExchangeSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesResolver());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHolidaySource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLegalEntitySource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRegionSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExecutionBlacklist());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCompilationBlacklist());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTempTargetRepository());
    hash = hash * 31 + JodaBeanUtils.hashCode(getViewProcessor());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(544);
    buf.append("MinimalEngineContextsComponentFactory{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("classifier").append('=').append(JodaBeanUtils.toString(getClassifier())).append(',').append(' ');
    buf.append("securitySource").append('=').append(JodaBeanUtils.toString(getSecuritySource())).append(',').append(' ');
    buf.append("positionSource").append('=').append(JodaBeanUtils.toString(getPositionSource())).append(',').append(' ');
    buf.append("targetResolver").append('=').append(JodaBeanUtils.toString(getTargetResolver())).append(',').append(' ');
    buf.append("configSource").append('=').append(JodaBeanUtils.toString(getConfigSource())).append(',').append(' ');
    buf.append("conventionSource").append('=').append(JodaBeanUtils.toString(getConventionSource())).append(',').append(' ');
    buf.append("exchangeSource").append('=').append(JodaBeanUtils.toString(getExchangeSource())).append(',').append(' ');
    buf.append("historicalTimeSeriesSource").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesSource())).append(',').append(' ');
    buf.append("historicalTimeSeriesResolver").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesResolver())).append(',').append(' ');
    buf.append("holidaySource").append('=').append(JodaBeanUtils.toString(getHolidaySource())).append(',').append(' ');
    buf.append("legalEntitySource").append('=').append(JodaBeanUtils.toString(getLegalEntitySource())).append(',').append(' ');
    buf.append("regionSource").append('=').append(JodaBeanUtils.toString(getRegionSource())).append(',').append(' ');
    buf.append("executionBlacklist").append('=').append(JodaBeanUtils.toString(getExecutionBlacklist())).append(',').append(' ');
    buf.append("compilationBlacklist").append('=').append(JodaBeanUtils.toString(getCompilationBlacklist())).append(',').append(' ');
    buf.append("tempTargetRepository").append('=').append(JodaBeanUtils.toString(getTempTargetRepository())).append(',').append(' ');
    buf.append("viewProcessor").append('=').append(JodaBeanUtils.toString(getViewProcessor())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MinimalEngineContextsComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", MinimalEngineContextsComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code securitySource} property.
     */
    private final MetaProperty<SecuritySource> _securitySource = DirectMetaProperty.ofReadWrite(
        this, "securitySource", MinimalEngineContextsComponentFactory.class, SecuritySource.class);
    /**
     * The meta-property for the {@code positionSource} property.
     */
    private final MetaProperty<PositionSource> _positionSource = DirectMetaProperty.ofReadWrite(
        this, "positionSource", MinimalEngineContextsComponentFactory.class, PositionSource.class);
    /**
     * The meta-property for the {@code targetResolver} property.
     */
    private final MetaProperty<ComputationTargetResolver> _targetResolver = DirectMetaProperty.ofReadWrite(
        this, "targetResolver", MinimalEngineContextsComponentFactory.class, ComputationTargetResolver.class);
    /**
     * The meta-property for the {@code configSource} property.
     */
    private final MetaProperty<ConfigSource> _configSource = DirectMetaProperty.ofReadWrite(
        this, "configSource", MinimalEngineContextsComponentFactory.class, ConfigSource.class);
    /**
     * The meta-property for the {@code conventionSource} property.
     */
    private final MetaProperty<ConventionSource> _conventionSource = DirectMetaProperty.ofReadWrite(
        this, "conventionSource", MinimalEngineContextsComponentFactory.class, ConventionSource.class);
    /**
     * The meta-property for the {@code exchangeSource} property.
     */
    private final MetaProperty<ExchangeSource> _exchangeSource = DirectMetaProperty.ofReadWrite(
        this, "exchangeSource", MinimalEngineContextsComponentFactory.class, ExchangeSource.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     */
    private final MetaProperty<HistoricalTimeSeriesSource> _historicalTimeSeriesSource = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesSource", MinimalEngineContextsComponentFactory.class, HistoricalTimeSeriesSource.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesResolver} property.
     */
    private final MetaProperty<HistoricalTimeSeriesResolver> _historicalTimeSeriesResolver = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesResolver", MinimalEngineContextsComponentFactory.class, HistoricalTimeSeriesResolver.class);
    /**
     * The meta-property for the {@code holidaySource} property.
     */
    private final MetaProperty<HolidaySource> _holidaySource = DirectMetaProperty.ofReadWrite(
        this, "holidaySource", MinimalEngineContextsComponentFactory.class, HolidaySource.class);
    /**
     * The meta-property for the {@code legalEntitySource} property.
     */
    private final MetaProperty<LegalEntitySource> _legalEntitySource = DirectMetaProperty.ofReadWrite(
        this, "legalEntitySource", MinimalEngineContextsComponentFactory.class, LegalEntitySource.class);
    /**
     * The meta-property for the {@code regionSource} property.
     */
    private final MetaProperty<RegionSource> _regionSource = DirectMetaProperty.ofReadWrite(
        this, "regionSource", MinimalEngineContextsComponentFactory.class, RegionSource.class);
    /**
     * The meta-property for the {@code executionBlacklist} property.
     */
    private final MetaProperty<FunctionBlacklist> _executionBlacklist = DirectMetaProperty.ofReadWrite(
        this, "executionBlacklist", MinimalEngineContextsComponentFactory.class, FunctionBlacklist.class);
    /**
     * The meta-property for the {@code compilationBlacklist} property.
     */
    private final MetaProperty<FunctionBlacklist> _compilationBlacklist = DirectMetaProperty.ofReadWrite(
        this, "compilationBlacklist", MinimalEngineContextsComponentFactory.class, FunctionBlacklist.class);
    /**
     * The meta-property for the {@code tempTargetRepository} property.
     */
    private final MetaProperty<TempTargetRepository> _tempTargetRepository = DirectMetaProperty.ofReadWrite(
        this, "tempTargetRepository", MinimalEngineContextsComponentFactory.class, TempTargetRepository.class);
    /**
     * The meta-property for the {@code viewProcessor} property.
     */
    private final MetaProperty<ViewProcessor> _viewProcessor = DirectMetaProperty.ofReadWrite(
        this, "viewProcessor", MinimalEngineContextsComponentFactory.class, ViewProcessor.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "securitySource",
        "positionSource",
        "targetResolver",
        "configSource",
        "conventionSource",
        "exchangeSource",
        "historicalTimeSeriesSource",
        "historicalTimeSeriesResolver",
        "holidaySource",
        "legalEntitySource",
        "regionSource",
        "executionBlacklist",
        "compilationBlacklist",
        "tempTargetRepository",
        "viewProcessor");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -702456965:  // securitySource
          return _securitySource;
        case -1655657820:  // positionSource
          return _positionSource;
        case -1933414217:  // targetResolver
          return _targetResolver;
        case 195157501:  // configSource
          return _configSource;
        case 225875692:  // conventionSource
          return _conventionSource;
        case -467239906:  // exchangeSource
          return _exchangeSource;
        case 358729161:  // historicalTimeSeriesSource
          return _historicalTimeSeriesSource;
        case -946313676:  // historicalTimeSeriesResolver
          return _historicalTimeSeriesResolver;
        case 431020691:  // holidaySource
          return _holidaySource;
        case -1759712457:  // legalEntitySource
          return _legalEntitySource;
        case -1636207569:  // regionSource
          return _regionSource;
        case -557041435:  // executionBlacklist
          return _executionBlacklist;
        case 1210914458:  // compilationBlacklist
          return _compilationBlacklist;
        case 491227055:  // tempTargetRepository
          return _tempTargetRepository;
        case -1697555603:  // viewProcessor
          return _viewProcessor;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends MinimalEngineContextsComponentFactory> builder() {
      return new DirectBeanBuilder<MinimalEngineContextsComponentFactory>(new MinimalEngineContextsComponentFactory());
    }

    @Override
    public Class<? extends MinimalEngineContextsComponentFactory> beanType() {
      return MinimalEngineContextsComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code securitySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecuritySource> securitySource() {
      return _securitySource;
    }

    /**
     * The meta-property for the {@code positionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionSource> positionSource() {
      return _positionSource;
    }

    /**
     * The meta-property for the {@code targetResolver} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComputationTargetResolver> targetResolver() {
      return _targetResolver;
    }

    /**
     * The meta-property for the {@code configSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigSource> configSource() {
      return _configSource;
    }

    /**
     * The meta-property for the {@code conventionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConventionSource> conventionSource() {
      return _conventionSource;
    }

    /**
     * The meta-property for the {@code exchangeSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExchangeSource> exchangeSource() {
      return _exchangeSource;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesSource> historicalTimeSeriesSource() {
      return _historicalTimeSeriesSource;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesResolver} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesResolver> historicalTimeSeriesResolver() {
      return _historicalTimeSeriesResolver;
    }

    /**
     * The meta-property for the {@code holidaySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidaySource> holidaySource() {
      return _holidaySource;
    }

    /**
     * The meta-property for the {@code legalEntitySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LegalEntitySource> legalEntitySource() {
      return _legalEntitySource;
    }

    /**
     * The meta-property for the {@code regionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RegionSource> regionSource() {
      return _regionSource;
    }

    /**
     * The meta-property for the {@code executionBlacklist} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FunctionBlacklist> executionBlacklist() {
      return _executionBlacklist;
    }

    /**
     * The meta-property for the {@code compilationBlacklist} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FunctionBlacklist> compilationBlacklist() {
      return _compilationBlacklist;
    }

    /**
     * The meta-property for the {@code tempTargetRepository} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<TempTargetRepository> tempTargetRepository() {
      return _tempTargetRepository;
    }

    /**
     * The meta-property for the {@code viewProcessor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ViewProcessor> viewProcessor() {
      return _viewProcessor;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((MinimalEngineContextsComponentFactory) bean).getClassifier();
        case -702456965:  // securitySource
          return ((MinimalEngineContextsComponentFactory) bean).getSecuritySource();
        case -1655657820:  // positionSource
          return ((MinimalEngineContextsComponentFactory) bean).getPositionSource();
        case -1933414217:  // targetResolver
          return ((MinimalEngineContextsComponentFactory) bean).getTargetResolver();
        case 195157501:  // configSource
          return ((MinimalEngineContextsComponentFactory) bean).getConfigSource();
        case 225875692:  // conventionSource
          return ((MinimalEngineContextsComponentFactory) bean).getConventionSource();
        case -467239906:  // exchangeSource
          return ((MinimalEngineContextsComponentFactory) bean).getExchangeSource();
        case 358729161:  // historicalTimeSeriesSource
          return ((MinimalEngineContextsComponentFactory) bean).getHistoricalTimeSeriesSource();
        case -946313676:  // historicalTimeSeriesResolver
          return ((MinimalEngineContextsComponentFactory) bean).getHistoricalTimeSeriesResolver();
        case 431020691:  // holidaySource
          return ((MinimalEngineContextsComponentFactory) bean).getHolidaySource();
        case -1759712457:  // legalEntitySource
          return ((MinimalEngineContextsComponentFactory) bean).getLegalEntitySource();
        case -1636207569:  // regionSource
          return ((MinimalEngineContextsComponentFactory) bean).getRegionSource();
        case -557041435:  // executionBlacklist
          return ((MinimalEngineContextsComponentFactory) bean).getExecutionBlacklist();
        case 1210914458:  // compilationBlacklist
          return ((MinimalEngineContextsComponentFactory) bean).getCompilationBlacklist();
        case 491227055:  // tempTargetRepository
          return ((MinimalEngineContextsComponentFactory) bean).getTempTargetRepository();
        case -1697555603:  // viewProcessor
          return ((MinimalEngineContextsComponentFactory) bean).getViewProcessor();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((MinimalEngineContextsComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -702456965:  // securitySource
          ((MinimalEngineContextsComponentFactory) bean).setSecuritySource((SecuritySource) newValue);
          return;
        case -1655657820:  // positionSource
          ((MinimalEngineContextsComponentFactory) bean).setPositionSource((PositionSource) newValue);
          return;
        case -1933414217:  // targetResolver
          ((MinimalEngineContextsComponentFactory) bean).setTargetResolver((ComputationTargetResolver) newValue);
          return;
        case 195157501:  // configSource
          ((MinimalEngineContextsComponentFactory) bean).setConfigSource((ConfigSource) newValue);
          return;
        case 225875692:  // conventionSource
          ((MinimalEngineContextsComponentFactory) bean).setConventionSource((ConventionSource) newValue);
          return;
        case -467239906:  // exchangeSource
          ((MinimalEngineContextsComponentFactory) bean).setExchangeSource((ExchangeSource) newValue);
          return;
        case 358729161:  // historicalTimeSeriesSource
          ((MinimalEngineContextsComponentFactory) bean).setHistoricalTimeSeriesSource((HistoricalTimeSeriesSource) newValue);
          return;
        case -946313676:  // historicalTimeSeriesResolver
          ((MinimalEngineContextsComponentFactory) bean).setHistoricalTimeSeriesResolver((HistoricalTimeSeriesResolver) newValue);
          return;
        case 431020691:  // holidaySource
          ((MinimalEngineContextsComponentFactory) bean).setHolidaySource((HolidaySource) newValue);
          return;
        case -1759712457:  // legalEntitySource
          ((MinimalEngineContextsComponentFactory) bean).setLegalEntitySource((LegalEntitySource) newValue);
          return;
        case -1636207569:  // regionSource
          ((MinimalEngineContextsComponentFactory) bean).setRegionSource((RegionSource) newValue);
          return;
        case -557041435:  // executionBlacklist
          ((MinimalEngineContextsComponentFactory) bean).setExecutionBlacklist((FunctionBlacklist) newValue);
          return;
        case 1210914458:  // compilationBlacklist
          ((MinimalEngineContextsComponentFactory) bean).setCompilationBlacklist((FunctionBlacklist) newValue);
          return;
        case 491227055:  // tempTargetRepository
          ((MinimalEngineContextsComponentFactory) bean).setTempTargetRepository((TempTargetRepository) newValue);
          return;
        case -1697555603:  // viewProcessor
          ((MinimalEngineContextsComponentFactory) bean).setViewProcessor((ViewProcessor) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((MinimalEngineContextsComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((MinimalEngineContextsComponentFactory) bean)._securitySource, "securitySource");
      JodaBeanUtils.notNull(((MinimalEngineContextsComponentFactory) bean)._positionSource, "positionSource");
      JodaBeanUtils.notNull(((MinimalEngineContextsComponentFactory) bean)._targetResolver, "targetResolver");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
