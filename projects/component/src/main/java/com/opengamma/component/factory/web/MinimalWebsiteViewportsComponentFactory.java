/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.component.factory.web;

import static com.opengamma.web.analytics.formatting.ResultsFormatter.CurrencyDisplay.DISPLAY_CURRENCY;
import static com.opengamma.web.analytics.formatting.ResultsFormatter.CurrencyDisplay.SUPPRESS_CURRENCY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.fudgemsg.FudgeContext;
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
import org.springframework.web.context.ServletContextAware;

import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionRepositoryFactory;
import com.opengamma.engine.marketdata.live.LiveMarketDataProviderFactory;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.financial.aggregation.PortfolioAggregationFunctions;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.financial.security.lookup.DefaultSecurityAttributeMappings;
import com.opengamma.financial.security.lookup.SecurityAttributeMapper;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.web.analytics.AnalyticsViewManager;
import com.opengamma.web.analytics.GridColumnsJsonWriter;
import com.opengamma.web.analytics.ViewportResultsJsonCsvWriter;
import com.opengamma.web.analytics.blotter.BlotterResource;
import com.opengamma.web.analytics.formatting.ResultsFormatter;
import com.opengamma.web.analytics.json.Compressor;
import com.opengamma.web.analytics.json.DependencyGraphGridStructureMessageBodyWriter;
import com.opengamma.web.analytics.json.ErrorInfoMessageBodyWriter;
import com.opengamma.web.analytics.json.GridColumnGroupsMessageBodyWriter;
import com.opengamma.web.analytics.json.PortfolioGridStructureMessageBodyWriter;
import com.opengamma.web.analytics.json.PrimitivesGridStructureMessageBodyWriter;
import com.opengamma.web.analytics.json.ValueRequirementMessageBodyWriter;
import com.opengamma.web.analytics.json.ViewportResultsMessageBodyWriter;
import com.opengamma.web.analytics.push.ConnectionManagerImpl;
import com.opengamma.web.analytics.push.LongPollingConnectionManager;
import com.opengamma.web.analytics.push.MasterChangeManager;
import com.opengamma.web.analytics.push.WebPushServletContextUtils;
import com.opengamma.web.analytics.rest.AggregatorNamesResource;
import com.opengamma.web.analytics.rest.LiveMarketDataProviderNamesResource;
import com.opengamma.web.analytics.rest.LogResource;
import com.opengamma.web.analytics.rest.MarketDataSnapshotListResource;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.TimeSeriesResolverKeysResource;
import com.opengamma.web.analytics.rest.UserResource;
import com.opengamma.web.analytics.rest.ViewDefinitionEntriesResource;
import com.opengamma.web.analytics.rest.WebUiResource;
import com.opengamma.web.server.AggregatedViewDefinitionManager;

/**
 * A version of {@link com.opengamma.component.factory.web.WebsiteViewportsComponentFactory} that does not reference deprecated
 * components and only requires:
 * <ul>
 *  <li> {@link SecurityMaster}: a master for securities that allows updates
 *  <li> {@link SecuritySource}: a source for securities
 *  <li> {@link PositionMaster}: a master for positions that allows updates
 *  <li> {@link PositionSource}: a source for positions
 *  <li> {@link PortfolioMaster}: a master for portfolios
 *  <li> {@link ComputationTargetResolver}: resolves the computation targets of a {@link com.opengamma.engine.function.FunctionDefinition}
 *  to a real target e.g. a security
 *  <li> {@link ViewProcessor}: manages the computation jobs for {@link com.opengamma.engine.view.ViewDefinition}s.
 *  <li> {@link PortfolioAggregationFunctions}: aggregates portfolios e.g. by currency
 *  <li> {@link UserPrincipal}: the user
 * </ul>
 * All other components are optional.
 */
@BeanDefinition
public class MinimalWebsiteViewportsComponentFactory extends AbstractComponentFactory {

  /**
   * The configuration master.
   */
  @PropertyDefinition
  private ConfigMaster _configMaster;

  /**
   * The security master.
   */
  @PropertyDefinition(validate = "notNull")
  private SecurityMaster _securityMaster;

  /**
   * The security source.
   */
  @PropertyDefinition(validate = "notNull")
  private SecuritySource _securitySource;

  /**
   * The position master.
   */
  @PropertyDefinition(validate = "notNull")
  private PositionMaster _positionMaster;

  /**
   * The portfolio master.
   */
  @PropertyDefinition(validate = "notNull")
  private PortfolioMaster _portfolioMaster;

  /**
   * The position source.
   */
  @PropertyDefinition(validate = "notNull")
  private PositionSource _positionSource;

  /**
   * The computation target resolver.
   */
  @PropertyDefinition(validate = "notNull")
  private ComputationTargetResolver _computationTargetResolver;

  /**
   * The function repository, null to not have additional function metadata available to the UI components.
   */
  @PropertyDefinition
  private FunctionConfigurationSource _functions;

  /**
   * The time-series master.
   */
  @PropertyDefinition
  private HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;

  /**
   * The organization master.
   */
  @PropertyDefinition
  private LegalEntityMaster _legalEntityMaster;

  /**
   * The user master.
   */
  @PropertyDefinition(validate = "notNull")
  private PositionMaster _userPositionMaster;

  /**
   * The user master.
   */
  @PropertyDefinition(validate = "notNull")
  private PortfolioMaster _userPortfolioMaster;

  /**
   * The user view definition repository.
   */
  @PropertyDefinition
  private ConfigMaster _userConfigMaster;

  /**
   * The combined config source.
   */
  @PropertyDefinition
  private ConfigSource _combinedConfigSource;

  /**
   * The view processor.
   */
  @PropertyDefinition(validate = "notNull")
  private ViewProcessor _viewProcessor;

  /**
   * The parallel view recompilation mode flag.
   */
  @PropertyDefinition
  private ExecutionFlags.ParallelRecompilationMode _parallelViewRecompilation;

  /**
   * The portfolio aggregation functions.
   */
  @PropertyDefinition(validate = "notNull")
  private PortfolioAggregationFunctions _portfolioAggregationFunctions;

  /**
   * The market data snapshot master.
   */
  @PropertyDefinition
  private MarketDataSnapshotMaster _marketDataSnapshotMaster;

  /**
   * The user.
   */
  @PropertyDefinition(validate = "notNull")
  private UserPrincipal _user;

  /**
   * The fudge context.
   */
  @PropertyDefinition(validate = "notNull")
  private FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();

  /**
   * For obtaining the live market data provider names. Either this or marketDataSpecificationRepository must be set.
   */
  @PropertyDefinition
  private LiveMarketDataProviderFactory _liveMarketDataProviderFactory;

  /**
   * Indicates if currency amounts should be displayed in the UI without the currency code.
   * Note that this will affect all views and should only be used where all results for all views will always be
   * in a single, well-known currency. Default value is false, indicating that currencies will be displayed by default.
   */
  @PropertyDefinition
  private boolean _suppressCurrencyDisplay;

  //-------------------------------------------------------------------------
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final LongPollingConnectionManager longPolling = buildLongPolling();
    final ChangeManager changeMgr = buildChangeManager();
    final MasterChangeManager masterChangeMgr = buildMasterChangeManager();
    final ConnectionManagerImpl connectionMgr = new ConnectionManagerImpl(changeMgr, masterChangeMgr, longPolling);
    final AggregatorNamesResource aggregatorsResource = new AggregatorNamesResource(getPortfolioAggregationFunctions().getMappedFunctions().keySet());
    repo.getRestComponents().publishResource(aggregatorsResource);
    if (getMarketDataSnapshotMaster() != null) {
      final MarketDataSnapshotListResource snapshotResource = new MarketDataSnapshotListResource(getMarketDataSnapshotMaster());
      repo.getRestComponents().publishResource(snapshotResource);
    }
    final SecurityAttributeMapper blotterColumnMapper;
    if (getConfigMaster() != null) {
      final MasterConfigSource configSource = new MasterConfigSource(getConfigMaster());
      final CurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
      // TODO should be able to configure the currency pairs
      final CurrencyPairs currencyPairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
      blotterColumnMapper = DefaultSecurityAttributeMappings.create(currencyPairs);
      repo.getRestComponents().publishHelper(new ViewDefinitionEntriesResource(configSource));
    } else {
      blotterColumnMapper = null;
    }
    final AggregatedViewDefinitionManager aggregatedViewDefManager =
        new AggregatedViewDefinitionManager(getPositionSource(), getSecuritySource(), getCombinedConfigSource(),
            getUserConfigMaster(), getUserPortfolioMaster(), getUserPositionMaster(),
            getPortfolioAggregationFunctions().getMappedFunctions());
    final AnalyticsViewManager analyticsViewManager =
        new AnalyticsViewManager(getViewProcessor(), getParallelViewRecompilation(), aggregatedViewDefManager, getComputationTargetResolver(),
            getFunctionRepository(), null, blotterColumnMapper, getPositionSource(), getCombinedConfigSource(), getSecuritySource(), getSecurityMaster(),
            getPositionMaster());
    final ResultsFormatter resultsFormatter = new ResultsFormatter(_suppressCurrencyDisplay ? SUPPRESS_CURRENCY : DISPLAY_CURRENCY);
    final GridColumnsJsonWriter columnWriter = new GridColumnsJsonWriter(resultsFormatter);
    final ViewportResultsJsonCsvWriter viewportResultsWriter = new ViewportResultsJsonCsvWriter(resultsFormatter);

    repo.getRestComponents().publishResource(new LiveMarketDataProviderNamesResource(getLiveMarketDataProviderFactory()));
    repo.getRestComponents().publishResource(new WebUiResource(analyticsViewManager, connectionMgr));
    repo.getRestComponents().publishResource(new Compressor());
    repo.getRestComponents().publishResource(new LogResource());
    repo.getRestComponents().publishResource(new UserResource());
    repo.getRestComponents().publishResource(new BlotterResource(getSecurityMaster(), getPortfolioMaster(), getPositionMaster()));
    repo.getRestComponents().publishResource(new TimeSeriesResolverKeysResource(getConfigMaster()));
    repo.getRestComponents().publishHelper(new PrimitivesGridStructureMessageBodyWriter(columnWriter));
    repo.getRestComponents().publishHelper(new PortfolioGridStructureMessageBodyWriter(columnWriter));
    repo.getRestComponents().publishHelper(new DependencyGraphGridStructureMessageBodyWriter(columnWriter));
    repo.getRestComponents().publishHelper(new ValueRequirementMessageBodyWriter());
    repo.getRestComponents().publishHelper(new GridColumnGroupsMessageBodyWriter(columnWriter));
    repo.getRestComponents().publishHelper(new ViewportResultsMessageBodyWriter(viewportResultsWriter));
    repo.getRestComponents().publishHelper(new ErrorInfoMessageBodyWriter());

    // these items need to be available to the servlet, but aren't important enough to be published components
    repo.registerServletContextAware(new ServletContextAware() {
      @Override
      public void setServletContext(final ServletContext servletContext) {
        WebPushServletContextUtils.setConnectionManager(servletContext, connectionMgr);
        WebPushServletContextUtils.setLongPollingConnectionManager(servletContext, longPolling);
      }
    });
  }

  /**
   * Gets the function repository, returning an empty repository if a {@link FunctionConfigurationSource} is not available.
   * @return  the function repository
   */
  protected FunctionRepositoryFactory getFunctionRepository() {
    // TODO: This is slightly wasteful if the view processor is in the same process and has created its own repository. Ideally we
    // should be able to inject either the constructed repository or a configuration source depending on what's available
    final FunctionConfigurationSource functions = getFunctions();
    if (functions == null) {
      // Supply an empty repo if the configuration is omitted
      return FunctionRepositoryFactory.constructRepositoryFactory(new InMemoryFunctionRepository());
    }
    return FunctionRepositoryFactory.constructRepositoryFactory(functions);
  }

  /**
   * Builds a long polling connection manager.
   * @return  a long polling connection manager
   */
  protected LongPollingConnectionManager buildLongPolling() {
    return new LongPollingConnectionManager();
  }

  /**
   * Builds a change manager and adds the historical time series master, config master and legal entity master
   * if they are available.
   * @return  the change manager
   */
  protected ChangeManager buildChangeManager() {
    final List<ChangeProvider> providers = new ArrayList<>();
    providers.add(getPositionMaster());
    providers.add(getPortfolioMaster());
    providers.add(getSecurityMaster());
    if (getHistoricalTimeSeriesMaster() != null) {
      providers.add(getHistoricalTimeSeriesMaster());
    }
    if (getConfigMaster() != null) {
      providers.add(getConfigMaster());
    }
    if (getLegalEntityMaster() != null) {
      providers.add(getLegalEntityMaster());
    }
    return new AggregatingChangeManager(providers);
  }

  /**
   * Builds a change manager and adds the masters if they are available.
   * @return  the change manager
   */
  protected MasterChangeManager buildMasterChangeManager() {
    final Map<MasterType, ChangeProvider> providers = new HashMap<>();
    providers.put(MasterType.POSITION, getPositionMaster());
    providers.put(MasterType.PORTFOLIO, getPortfolioMaster());
    providers.put(MasterType.SECURITY, getSecurityMaster());
    if (getHistoricalTimeSeriesMaster() != null) {
      providers.put(MasterType.TIME_SERIES, getHistoricalTimeSeriesMaster());
    }
    if (getConfigMaster() != null) {
      providers.put(MasterType.CONFIG, getConfigMaster());
    }
    if (getLegalEntityMaster() != null) {
      providers.put(MasterType.ORGANIZATION, getLegalEntityMaster());
    }
    if (getMarketDataSnapshotMaster() != null) {
      providers.put(MasterType.MARKET_DATA_SNAPSHOT, getMarketDataSnapshotMaster());
    }
    return new MasterChangeManager(providers);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MinimalWebsiteViewportsComponentFactory}.
   * @return the meta-bean, not null
   */
  public static MinimalWebsiteViewportsComponentFactory.Meta meta() {
    return MinimalWebsiteViewportsComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MinimalWebsiteViewportsComponentFactory.Meta.INSTANCE);
  }

  @Override
  public MinimalWebsiteViewportsComponentFactory.Meta metaBean() {
    return MinimalWebsiteViewportsComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the configuration master.
   * @return the value of the property
   */
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Sets the configuration master.
   * @param configMaster  the new value of the property
   */
  public void setConfigMaster(ConfigMaster configMaster) {
    this._configMaster = configMaster;
  }

  /**
   * Gets the the {@code configMaster} property.
   * @return the property, not null
   */
  public final Property<ConfigMaster> configMaster() {
    return metaBean().configMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security master.
   * @return the value of the property, not null
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * Sets the security master.
   * @param securityMaster  the new value of the property, not null
   */
  public void setSecurityMaster(SecurityMaster securityMaster) {
    JodaBeanUtils.notNull(securityMaster, "securityMaster");
    this._securityMaster = securityMaster;
  }

  /**
   * Gets the the {@code securityMaster} property.
   * @return the property, not null
   */
  public final Property<SecurityMaster> securityMaster() {
    return metaBean().securityMaster().createProperty(this);
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
   * Gets the position master.
   * @return the value of the property, not null
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Sets the position master.
   * @param positionMaster  the new value of the property, not null
   */
  public void setPositionMaster(PositionMaster positionMaster) {
    JodaBeanUtils.notNull(positionMaster, "positionMaster");
    this._positionMaster = positionMaster;
  }

  /**
   * Gets the the {@code positionMaster} property.
   * @return the property, not null
   */
  public final Property<PositionMaster> positionMaster() {
    return metaBean().positionMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio master.
   * @return the value of the property, not null
   */
  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  /**
   * Sets the portfolio master.
   * @param portfolioMaster  the new value of the property, not null
   */
  public void setPortfolioMaster(PortfolioMaster portfolioMaster) {
    JodaBeanUtils.notNull(portfolioMaster, "portfolioMaster");
    this._portfolioMaster = portfolioMaster;
  }

  /**
   * Gets the the {@code portfolioMaster} property.
   * @return the property, not null
   */
  public final Property<PortfolioMaster> portfolioMaster() {
    return metaBean().portfolioMaster().createProperty(this);
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
   * Gets the computation target resolver.
   * @return the value of the property, not null
   */
  public ComputationTargetResolver getComputationTargetResolver() {
    return _computationTargetResolver;
  }

  /**
   * Sets the computation target resolver.
   * @param computationTargetResolver  the new value of the property, not null
   */
  public void setComputationTargetResolver(ComputationTargetResolver computationTargetResolver) {
    JodaBeanUtils.notNull(computationTargetResolver, "computationTargetResolver");
    this._computationTargetResolver = computationTargetResolver;
  }

  /**
   * Gets the the {@code computationTargetResolver} property.
   * @return the property, not null
   */
  public final Property<ComputationTargetResolver> computationTargetResolver() {
    return metaBean().computationTargetResolver().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the function repository, null to not have additional function metadata available to the UI components.
   * @return the value of the property
   */
  public FunctionConfigurationSource getFunctions() {
    return _functions;
  }

  /**
   * Sets the function repository, null to not have additional function metadata available to the UI components.
   * @param functions  the new value of the property
   */
  public void setFunctions(FunctionConfigurationSource functions) {
    this._functions = functions;
  }

  /**
   * Gets the the {@code functions} property.
   * @return the property, not null
   */
  public final Property<FunctionConfigurationSource> functions() {
    return metaBean().functions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time-series master.
   * @return the value of the property
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return _historicalTimeSeriesMaster;
  }

  /**
   * Sets the time-series master.
   * @param historicalTimeSeriesMaster  the new value of the property
   */
  public void setHistoricalTimeSeriesMaster(HistoricalTimeSeriesMaster historicalTimeSeriesMaster) {
    this._historicalTimeSeriesMaster = historicalTimeSeriesMaster;
  }

  /**
   * Gets the the {@code historicalTimeSeriesMaster} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesMaster> historicalTimeSeriesMaster() {
    return metaBean().historicalTimeSeriesMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the organization master.
   * @return the value of the property
   */
  public LegalEntityMaster getLegalEntityMaster() {
    return _legalEntityMaster;
  }

  /**
   * Sets the organization master.
   * @param legalEntityMaster  the new value of the property
   */
  public void setLegalEntityMaster(LegalEntityMaster legalEntityMaster) {
    this._legalEntityMaster = legalEntityMaster;
  }

  /**
   * Gets the the {@code legalEntityMaster} property.
   * @return the property, not null
   */
  public final Property<LegalEntityMaster> legalEntityMaster() {
    return metaBean().legalEntityMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the user master.
   * @return the value of the property, not null
   */
  public PositionMaster getUserPositionMaster() {
    return _userPositionMaster;
  }

  /**
   * Sets the user master.
   * @param userPositionMaster  the new value of the property, not null
   */
  public void setUserPositionMaster(PositionMaster userPositionMaster) {
    JodaBeanUtils.notNull(userPositionMaster, "userPositionMaster");
    this._userPositionMaster = userPositionMaster;
  }

  /**
   * Gets the the {@code userPositionMaster} property.
   * @return the property, not null
   */
  public final Property<PositionMaster> userPositionMaster() {
    return metaBean().userPositionMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the user master.
   * @return the value of the property, not null
   */
  public PortfolioMaster getUserPortfolioMaster() {
    return _userPortfolioMaster;
  }

  /**
   * Sets the user master.
   * @param userPortfolioMaster  the new value of the property, not null
   */
  public void setUserPortfolioMaster(PortfolioMaster userPortfolioMaster) {
    JodaBeanUtils.notNull(userPortfolioMaster, "userPortfolioMaster");
    this._userPortfolioMaster = userPortfolioMaster;
  }

  /**
   * Gets the the {@code userPortfolioMaster} property.
   * @return the property, not null
   */
  public final Property<PortfolioMaster> userPortfolioMaster() {
    return metaBean().userPortfolioMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the user view definition repository.
   * @return the value of the property
   */
  public ConfigMaster getUserConfigMaster() {
    return _userConfigMaster;
  }

  /**
   * Sets the user view definition repository.
   * @param userConfigMaster  the new value of the property
   */
  public void setUserConfigMaster(ConfigMaster userConfigMaster) {
    this._userConfigMaster = userConfigMaster;
  }

  /**
   * Gets the the {@code userConfigMaster} property.
   * @return the property, not null
   */
  public final Property<ConfigMaster> userConfigMaster() {
    return metaBean().userConfigMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the combined config source.
   * @return the value of the property
   */
  public ConfigSource getCombinedConfigSource() {
    return _combinedConfigSource;
  }

  /**
   * Sets the combined config source.
   * @param combinedConfigSource  the new value of the property
   */
  public void setCombinedConfigSource(ConfigSource combinedConfigSource) {
    this._combinedConfigSource = combinedConfigSource;
  }

  /**
   * Gets the the {@code combinedConfigSource} property.
   * @return the property, not null
   */
  public final Property<ConfigSource> combinedConfigSource() {
    return metaBean().combinedConfigSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the view processor.
   * @return the value of the property, not null
   */
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  /**
   * Sets the view processor.
   * @param viewProcessor  the new value of the property, not null
   */
  public void setViewProcessor(ViewProcessor viewProcessor) {
    JodaBeanUtils.notNull(viewProcessor, "viewProcessor");
    this._viewProcessor = viewProcessor;
  }

  /**
   * Gets the the {@code viewProcessor} property.
   * @return the property, not null
   */
  public final Property<ViewProcessor> viewProcessor() {
    return metaBean().viewProcessor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the parallel view recompilation mode flag.
   * @return the value of the property
   */
  public ExecutionFlags.ParallelRecompilationMode getParallelViewRecompilation() {
    return _parallelViewRecompilation;
  }

  /**
   * Sets the parallel view recompilation mode flag.
   * @param parallelViewRecompilation  the new value of the property
   */
  public void setParallelViewRecompilation(ExecutionFlags.ParallelRecompilationMode parallelViewRecompilation) {
    this._parallelViewRecompilation = parallelViewRecompilation;
  }

  /**
   * Gets the the {@code parallelViewRecompilation} property.
   * @return the property, not null
   */
  public final Property<ExecutionFlags.ParallelRecompilationMode> parallelViewRecompilation() {
    return metaBean().parallelViewRecompilation().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio aggregation functions.
   * @return the value of the property, not null
   */
  public PortfolioAggregationFunctions getPortfolioAggregationFunctions() {
    return _portfolioAggregationFunctions;
  }

  /**
   * Sets the portfolio aggregation functions.
   * @param portfolioAggregationFunctions  the new value of the property, not null
   */
  public void setPortfolioAggregationFunctions(PortfolioAggregationFunctions portfolioAggregationFunctions) {
    JodaBeanUtils.notNull(portfolioAggregationFunctions, "portfolioAggregationFunctions");
    this._portfolioAggregationFunctions = portfolioAggregationFunctions;
  }

  /**
   * Gets the the {@code portfolioAggregationFunctions} property.
   * @return the property, not null
   */
  public final Property<PortfolioAggregationFunctions> portfolioAggregationFunctions() {
    return metaBean().portfolioAggregationFunctions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market data snapshot master.
   * @return the value of the property
   */
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster() {
    return _marketDataSnapshotMaster;
  }

  /**
   * Sets the market data snapshot master.
   * @param marketDataSnapshotMaster  the new value of the property
   */
  public void setMarketDataSnapshotMaster(MarketDataSnapshotMaster marketDataSnapshotMaster) {
    this._marketDataSnapshotMaster = marketDataSnapshotMaster;
  }

  /**
   * Gets the the {@code marketDataSnapshotMaster} property.
   * @return the property, not null
   */
  public final Property<MarketDataSnapshotMaster> marketDataSnapshotMaster() {
    return metaBean().marketDataSnapshotMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the user.
   * @return the value of the property, not null
   */
  public UserPrincipal getUser() {
    return _user;
  }

  /**
   * Sets the user.
   * @param user  the new value of the property, not null
   */
  public void setUser(UserPrincipal user) {
    JodaBeanUtils.notNull(user, "user");
    this._user = user;
  }

  /**
   * Gets the the {@code user} property.
   * @return the property, not null
   */
  public final Property<UserPrincipal> user() {
    return metaBean().user().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fudge context.
   * @return the value of the property, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Sets the fudge context.
   * @param fudgeContext  the new value of the property, not null
   */
  public void setFudgeContext(FudgeContext fudgeContext) {
    JodaBeanUtils.notNull(fudgeContext, "fudgeContext");
    this._fudgeContext = fudgeContext;
  }

  /**
   * Gets the the {@code fudgeContext} property.
   * @return the property, not null
   */
  public final Property<FudgeContext> fudgeContext() {
    return metaBean().fudgeContext().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets for obtaining the live market data provider names. Either this or marketDataSpecificationRepository must be set.
   * @return the value of the property
   */
  public LiveMarketDataProviderFactory getLiveMarketDataProviderFactory() {
    return _liveMarketDataProviderFactory;
  }

  /**
   * Sets for obtaining the live market data provider names. Either this or marketDataSpecificationRepository must be set.
   * @param liveMarketDataProviderFactory  the new value of the property
   */
  public void setLiveMarketDataProviderFactory(LiveMarketDataProviderFactory liveMarketDataProviderFactory) {
    this._liveMarketDataProviderFactory = liveMarketDataProviderFactory;
  }

  /**
   * Gets the the {@code liveMarketDataProviderFactory} property.
   * @return the property, not null
   */
  public final Property<LiveMarketDataProviderFactory> liveMarketDataProviderFactory() {
    return metaBean().liveMarketDataProviderFactory().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets indicates if currency amounts should be displayed in the UI without the currency code.
   * Note that this will affect all views and should only be used where all results for all views will always be
   * in a single, well-known currency. Default value is false, indicating that currencies will be displayed by default.
   * @return the value of the property
   */
  public boolean isSuppressCurrencyDisplay() {
    return _suppressCurrencyDisplay;
  }

  /**
   * Sets indicates if currency amounts should be displayed in the UI without the currency code.
   * Note that this will affect all views and should only be used where all results for all views will always be
   * in a single, well-known currency. Default value is false, indicating that currencies will be displayed by default.
   * @param suppressCurrencyDisplay  the new value of the property
   */
  public void setSuppressCurrencyDisplay(boolean suppressCurrencyDisplay) {
    this._suppressCurrencyDisplay = suppressCurrencyDisplay;
  }

  /**
   * Gets the the {@code suppressCurrencyDisplay} property.
   * Note that this will affect all views and should only be used where all results for all views will always be
   * in a single, well-known currency. Default value is false, indicating that currencies will be displayed by default.
   * @return the property, not null
   */
  public final Property<Boolean> suppressCurrencyDisplay() {
    return metaBean().suppressCurrencyDisplay().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public MinimalWebsiteViewportsComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MinimalWebsiteViewportsComponentFactory other = (MinimalWebsiteViewportsComponentFactory) obj;
      return JodaBeanUtils.equal(getConfigMaster(), other.getConfigMaster()) &&
          JodaBeanUtils.equal(getSecurityMaster(), other.getSecurityMaster()) &&
          JodaBeanUtils.equal(getSecuritySource(), other.getSecuritySource()) &&
          JodaBeanUtils.equal(getPositionMaster(), other.getPositionMaster()) &&
          JodaBeanUtils.equal(getPortfolioMaster(), other.getPortfolioMaster()) &&
          JodaBeanUtils.equal(getPositionSource(), other.getPositionSource()) &&
          JodaBeanUtils.equal(getComputationTargetResolver(), other.getComputationTargetResolver()) &&
          JodaBeanUtils.equal(getFunctions(), other.getFunctions()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesMaster(), other.getHistoricalTimeSeriesMaster()) &&
          JodaBeanUtils.equal(getLegalEntityMaster(), other.getLegalEntityMaster()) &&
          JodaBeanUtils.equal(getUserPositionMaster(), other.getUserPositionMaster()) &&
          JodaBeanUtils.equal(getUserPortfolioMaster(), other.getUserPortfolioMaster()) &&
          JodaBeanUtils.equal(getUserConfigMaster(), other.getUserConfigMaster()) &&
          JodaBeanUtils.equal(getCombinedConfigSource(), other.getCombinedConfigSource()) &&
          JodaBeanUtils.equal(getViewProcessor(), other.getViewProcessor()) &&
          JodaBeanUtils.equal(getParallelViewRecompilation(), other.getParallelViewRecompilation()) &&
          JodaBeanUtils.equal(getPortfolioAggregationFunctions(), other.getPortfolioAggregationFunctions()) &&
          JodaBeanUtils.equal(getMarketDataSnapshotMaster(), other.getMarketDataSnapshotMaster()) &&
          JodaBeanUtils.equal(getUser(), other.getUser()) &&
          JodaBeanUtils.equal(getFudgeContext(), other.getFudgeContext()) &&
          JodaBeanUtils.equal(getLiveMarketDataProviderFactory(), other.getLiveMarketDataProviderFactory()) &&
          (isSuppressCurrencyDisplay() == other.isSuppressCurrencyDisplay()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSecurityMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSecuritySource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPositionMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPortfolioMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPositionSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getComputationTargetResolver());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFunctions());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLegalEntityMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserPositionMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserPortfolioMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserConfigMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCombinedConfigSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getViewProcessor());
    hash = hash * 31 + JodaBeanUtils.hashCode(getParallelViewRecompilation());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPortfolioAggregationFunctions());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMarketDataSnapshotMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUser());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFudgeContext());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLiveMarketDataProviderFactory());
    hash = hash * 31 + JodaBeanUtils.hashCode(isSuppressCurrencyDisplay());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(736);
    buf.append("MinimalWebsiteViewportsComponentFactory{");
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
    buf.append("configMaster").append('=').append(JodaBeanUtils.toString(getConfigMaster())).append(',').append(' ');
    buf.append("securityMaster").append('=').append(JodaBeanUtils.toString(getSecurityMaster())).append(',').append(' ');
    buf.append("securitySource").append('=').append(JodaBeanUtils.toString(getSecuritySource())).append(',').append(' ');
    buf.append("positionMaster").append('=').append(JodaBeanUtils.toString(getPositionMaster())).append(',').append(' ');
    buf.append("portfolioMaster").append('=').append(JodaBeanUtils.toString(getPortfolioMaster())).append(',').append(' ');
    buf.append("positionSource").append('=').append(JodaBeanUtils.toString(getPositionSource())).append(',').append(' ');
    buf.append("computationTargetResolver").append('=').append(JodaBeanUtils.toString(getComputationTargetResolver())).append(',').append(' ');
    buf.append("functions").append('=').append(JodaBeanUtils.toString(getFunctions())).append(',').append(' ');
    buf.append("historicalTimeSeriesMaster").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesMaster())).append(',').append(' ');
    buf.append("legalEntityMaster").append('=').append(JodaBeanUtils.toString(getLegalEntityMaster())).append(',').append(' ');
    buf.append("userPositionMaster").append('=').append(JodaBeanUtils.toString(getUserPositionMaster())).append(',').append(' ');
    buf.append("userPortfolioMaster").append('=').append(JodaBeanUtils.toString(getUserPortfolioMaster())).append(',').append(' ');
    buf.append("userConfigMaster").append('=').append(JodaBeanUtils.toString(getUserConfigMaster())).append(',').append(' ');
    buf.append("combinedConfigSource").append('=').append(JodaBeanUtils.toString(getCombinedConfigSource())).append(',').append(' ');
    buf.append("viewProcessor").append('=').append(JodaBeanUtils.toString(getViewProcessor())).append(',').append(' ');
    buf.append("parallelViewRecompilation").append('=').append(JodaBeanUtils.toString(getParallelViewRecompilation())).append(',').append(' ');
    buf.append("portfolioAggregationFunctions").append('=').append(JodaBeanUtils.toString(getPortfolioAggregationFunctions())).append(',').append(' ');
    buf.append("marketDataSnapshotMaster").append('=').append(JodaBeanUtils.toString(getMarketDataSnapshotMaster())).append(',').append(' ');
    buf.append("user").append('=').append(JodaBeanUtils.toString(getUser())).append(',').append(' ');
    buf.append("fudgeContext").append('=').append(JodaBeanUtils.toString(getFudgeContext())).append(',').append(' ');
    buf.append("liveMarketDataProviderFactory").append('=').append(JodaBeanUtils.toString(getLiveMarketDataProviderFactory())).append(',').append(' ');
    buf.append("suppressCurrencyDisplay").append('=').append(JodaBeanUtils.toString(isSuppressCurrencyDisplay())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MinimalWebsiteViewportsComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code configMaster} property.
     */
    private final MetaProperty<ConfigMaster> _configMaster = DirectMetaProperty.ofReadWrite(
        this, "configMaster", MinimalWebsiteViewportsComponentFactory.class, ConfigMaster.class);
    /**
     * The meta-property for the {@code securityMaster} property.
     */
    private final MetaProperty<SecurityMaster> _securityMaster = DirectMetaProperty.ofReadWrite(
        this, "securityMaster", MinimalWebsiteViewportsComponentFactory.class, SecurityMaster.class);
    /**
     * The meta-property for the {@code securitySource} property.
     */
    private final MetaProperty<SecuritySource> _securitySource = DirectMetaProperty.ofReadWrite(
        this, "securitySource", MinimalWebsiteViewportsComponentFactory.class, SecuritySource.class);
    /**
     * The meta-property for the {@code positionMaster} property.
     */
    private final MetaProperty<PositionMaster> _positionMaster = DirectMetaProperty.ofReadWrite(
        this, "positionMaster", MinimalWebsiteViewportsComponentFactory.class, PositionMaster.class);
    /**
     * The meta-property for the {@code portfolioMaster} property.
     */
    private final MetaProperty<PortfolioMaster> _portfolioMaster = DirectMetaProperty.ofReadWrite(
        this, "portfolioMaster", MinimalWebsiteViewportsComponentFactory.class, PortfolioMaster.class);
    /**
     * The meta-property for the {@code positionSource} property.
     */
    private final MetaProperty<PositionSource> _positionSource = DirectMetaProperty.ofReadWrite(
        this, "positionSource", MinimalWebsiteViewportsComponentFactory.class, PositionSource.class);
    /**
     * The meta-property for the {@code computationTargetResolver} property.
     */
    private final MetaProperty<ComputationTargetResolver> _computationTargetResolver = DirectMetaProperty.ofReadWrite(
        this, "computationTargetResolver", MinimalWebsiteViewportsComponentFactory.class, ComputationTargetResolver.class);
    /**
     * The meta-property for the {@code functions} property.
     */
    private final MetaProperty<FunctionConfigurationSource> _functions = DirectMetaProperty.ofReadWrite(
        this, "functions", MinimalWebsiteViewportsComponentFactory.class, FunctionConfigurationSource.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesMaster} property.
     */
    private final MetaProperty<HistoricalTimeSeriesMaster> _historicalTimeSeriesMaster = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesMaster", MinimalWebsiteViewportsComponentFactory.class, HistoricalTimeSeriesMaster.class);
    /**
     * The meta-property for the {@code legalEntityMaster} property.
     */
    private final MetaProperty<LegalEntityMaster> _legalEntityMaster = DirectMetaProperty.ofReadWrite(
        this, "legalEntityMaster", MinimalWebsiteViewportsComponentFactory.class, LegalEntityMaster.class);
    /**
     * The meta-property for the {@code userPositionMaster} property.
     */
    private final MetaProperty<PositionMaster> _userPositionMaster = DirectMetaProperty.ofReadWrite(
        this, "userPositionMaster", MinimalWebsiteViewportsComponentFactory.class, PositionMaster.class);
    /**
     * The meta-property for the {@code userPortfolioMaster} property.
     */
    private final MetaProperty<PortfolioMaster> _userPortfolioMaster = DirectMetaProperty.ofReadWrite(
        this, "userPortfolioMaster", MinimalWebsiteViewportsComponentFactory.class, PortfolioMaster.class);
    /**
     * The meta-property for the {@code userConfigMaster} property.
     */
    private final MetaProperty<ConfigMaster> _userConfigMaster = DirectMetaProperty.ofReadWrite(
        this, "userConfigMaster", MinimalWebsiteViewportsComponentFactory.class, ConfigMaster.class);
    /**
     * The meta-property for the {@code combinedConfigSource} property.
     */
    private final MetaProperty<ConfigSource> _combinedConfigSource = DirectMetaProperty.ofReadWrite(
        this, "combinedConfigSource", MinimalWebsiteViewportsComponentFactory.class, ConfigSource.class);
    /**
     * The meta-property for the {@code viewProcessor} property.
     */
    private final MetaProperty<ViewProcessor> _viewProcessor = DirectMetaProperty.ofReadWrite(
        this, "viewProcessor", MinimalWebsiteViewportsComponentFactory.class, ViewProcessor.class);
    /**
     * The meta-property for the {@code parallelViewRecompilation} property.
     */
    private final MetaProperty<ExecutionFlags.ParallelRecompilationMode> _parallelViewRecompilation = DirectMetaProperty.ofReadWrite(
        this, "parallelViewRecompilation", MinimalWebsiteViewportsComponentFactory.class, ExecutionFlags.ParallelRecompilationMode.class);
    /**
     * The meta-property for the {@code portfolioAggregationFunctions} property.
     */
    private final MetaProperty<PortfolioAggregationFunctions> _portfolioAggregationFunctions = DirectMetaProperty.ofReadWrite(
        this, "portfolioAggregationFunctions", MinimalWebsiteViewportsComponentFactory.class, PortfolioAggregationFunctions.class);
    /**
     * The meta-property for the {@code marketDataSnapshotMaster} property.
     */
    private final MetaProperty<MarketDataSnapshotMaster> _marketDataSnapshotMaster = DirectMetaProperty.ofReadWrite(
        this, "marketDataSnapshotMaster", MinimalWebsiteViewportsComponentFactory.class, MarketDataSnapshotMaster.class);
    /**
     * The meta-property for the {@code user} property.
     */
    private final MetaProperty<UserPrincipal> _user = DirectMetaProperty.ofReadWrite(
        this, "user", MinimalWebsiteViewportsComponentFactory.class, UserPrincipal.class);
    /**
     * The meta-property for the {@code fudgeContext} property.
     */
    private final MetaProperty<FudgeContext> _fudgeContext = DirectMetaProperty.ofReadWrite(
        this, "fudgeContext", MinimalWebsiteViewportsComponentFactory.class, FudgeContext.class);
    /**
     * The meta-property for the {@code liveMarketDataProviderFactory} property.
     */
    private final MetaProperty<LiveMarketDataProviderFactory> _liveMarketDataProviderFactory = DirectMetaProperty.ofReadWrite(
        this, "liveMarketDataProviderFactory", MinimalWebsiteViewportsComponentFactory.class, LiveMarketDataProviderFactory.class);
    /**
     * The meta-property for the {@code suppressCurrencyDisplay} property.
     */
    private final MetaProperty<Boolean> _suppressCurrencyDisplay = DirectMetaProperty.ofReadWrite(
        this, "suppressCurrencyDisplay", MinimalWebsiteViewportsComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "configMaster",
        "securityMaster",
        "securitySource",
        "positionMaster",
        "portfolioMaster",
        "positionSource",
        "computationTargetResolver",
        "functions",
        "historicalTimeSeriesMaster",
        "legalEntityMaster",
        "userPositionMaster",
        "userPortfolioMaster",
        "userConfigMaster",
        "combinedConfigSource",
        "viewProcessor",
        "parallelViewRecompilation",
        "portfolioAggregationFunctions",
        "marketDataSnapshotMaster",
        "user",
        "fudgeContext",
        "liveMarketDataProviderFactory",
        "suppressCurrencyDisplay");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 10395716:  // configMaster
          return _configMaster;
        case -887218750:  // securityMaster
          return _securityMaster;
        case -702456965:  // securitySource
          return _securitySource;
        case -1840419605:  // positionMaster
          return _positionMaster;
        case -772274742:  // portfolioMaster
          return _portfolioMaster;
        case -1655657820:  // positionSource
          return _positionSource;
        case 1562222174:  // computationTargetResolver
          return _computationTargetResolver;
        case -140572773:  // functions
          return _functions;
        case 173967376:  // historicalTimeSeriesMaster
          return _historicalTimeSeriesMaster;
        case -1944474242:  // legalEntityMaster
          return _legalEntityMaster;
        case 1808868758:  // userPositionMaster
          return _userPositionMaster;
        case 686514815:  // userPortfolioMaster
          return _userPortfolioMaster;
        case -763459665:  // userConfigMaster
          return _userConfigMaster;
        case -774734430:  // combinedConfigSource
          return _combinedConfigSource;
        case -1697555603:  // viewProcessor
          return _viewProcessor;
        case 1910524868:  // parallelViewRecompilation
          return _parallelViewRecompilation;
        case 940303425:  // portfolioAggregationFunctions
          return _portfolioAggregationFunctions;
        case 2090650860:  // marketDataSnapshotMaster
          return _marketDataSnapshotMaster;
        case 3599307:  // user
          return _user;
        case -917704420:  // fudgeContext
          return _fudgeContext;
        case -301472921:  // liveMarketDataProviderFactory
          return _liveMarketDataProviderFactory;
        case -1406342148:  // suppressCurrencyDisplay
          return _suppressCurrencyDisplay;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends MinimalWebsiteViewportsComponentFactory> builder() {
      return new DirectBeanBuilder<MinimalWebsiteViewportsComponentFactory>(new MinimalWebsiteViewportsComponentFactory());
    }

    @Override
    public Class<? extends MinimalWebsiteViewportsComponentFactory> beanType() {
      return MinimalWebsiteViewportsComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code configMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigMaster> configMaster() {
      return _configMaster;
    }

    /**
     * The meta-property for the {@code securityMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecurityMaster> securityMaster() {
      return _securityMaster;
    }

    /**
     * The meta-property for the {@code securitySource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecuritySource> securitySource() {
      return _securitySource;
    }

    /**
     * The meta-property for the {@code positionMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionMaster> positionMaster() {
      return _positionMaster;
    }

    /**
     * The meta-property for the {@code portfolioMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PortfolioMaster> portfolioMaster() {
      return _portfolioMaster;
    }

    /**
     * The meta-property for the {@code positionSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionSource> positionSource() {
      return _positionSource;
    }

    /**
     * The meta-property for the {@code computationTargetResolver} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComputationTargetResolver> computationTargetResolver() {
      return _computationTargetResolver;
    }

    /**
     * The meta-property for the {@code functions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FunctionConfigurationSource> functions() {
      return _functions;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesMaster> historicalTimeSeriesMaster() {
      return _historicalTimeSeriesMaster;
    }

    /**
     * The meta-property for the {@code legalEntityMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LegalEntityMaster> legalEntityMaster() {
      return _legalEntityMaster;
    }

    /**
     * The meta-property for the {@code userPositionMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionMaster> userPositionMaster() {
      return _userPositionMaster;
    }

    /**
     * The meta-property for the {@code userPortfolioMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PortfolioMaster> userPortfolioMaster() {
      return _userPortfolioMaster;
    }

    /**
     * The meta-property for the {@code userConfigMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigMaster> userConfigMaster() {
      return _userConfigMaster;
    }

    /**
     * The meta-property for the {@code combinedConfigSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigSource> combinedConfigSource() {
      return _combinedConfigSource;
    }

    /**
     * The meta-property for the {@code viewProcessor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ViewProcessor> viewProcessor() {
      return _viewProcessor;
    }

    /**
     * The meta-property for the {@code parallelViewRecompilation} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExecutionFlags.ParallelRecompilationMode> parallelViewRecompilation() {
      return _parallelViewRecompilation;
    }

    /**
     * The meta-property for the {@code portfolioAggregationFunctions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PortfolioAggregationFunctions> portfolioAggregationFunctions() {
      return _portfolioAggregationFunctions;
    }

    /**
     * The meta-property for the {@code marketDataSnapshotMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataSnapshotMaster> marketDataSnapshotMaster() {
      return _marketDataSnapshotMaster;
    }

    /**
     * The meta-property for the {@code user} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UserPrincipal> user() {
      return _user;
    }

    /**
     * The meta-property for the {@code fudgeContext} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FudgeContext> fudgeContext() {
      return _fudgeContext;
    }

    /**
     * The meta-property for the {@code liveMarketDataProviderFactory} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LiveMarketDataProviderFactory> liveMarketDataProviderFactory() {
      return _liveMarketDataProviderFactory;
    }

    /**
     * The meta-property for the {@code suppressCurrencyDisplay} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> suppressCurrencyDisplay() {
      return _suppressCurrencyDisplay;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 10395716:  // configMaster
          return ((MinimalWebsiteViewportsComponentFactory) bean).getConfigMaster();
        case -887218750:  // securityMaster
          return ((MinimalWebsiteViewportsComponentFactory) bean).getSecurityMaster();
        case -702456965:  // securitySource
          return ((MinimalWebsiteViewportsComponentFactory) bean).getSecuritySource();
        case -1840419605:  // positionMaster
          return ((MinimalWebsiteViewportsComponentFactory) bean).getPositionMaster();
        case -772274742:  // portfolioMaster
          return ((MinimalWebsiteViewportsComponentFactory) bean).getPortfolioMaster();
        case -1655657820:  // positionSource
          return ((MinimalWebsiteViewportsComponentFactory) bean).getPositionSource();
        case 1562222174:  // computationTargetResolver
          return ((MinimalWebsiteViewportsComponentFactory) bean).getComputationTargetResolver();
        case -140572773:  // functions
          return ((MinimalWebsiteViewportsComponentFactory) bean).getFunctions();
        case 173967376:  // historicalTimeSeriesMaster
          return ((MinimalWebsiteViewportsComponentFactory) bean).getHistoricalTimeSeriesMaster();
        case -1944474242:  // legalEntityMaster
          return ((MinimalWebsiteViewportsComponentFactory) bean).getLegalEntityMaster();
        case 1808868758:  // userPositionMaster
          return ((MinimalWebsiteViewportsComponentFactory) bean).getUserPositionMaster();
        case 686514815:  // userPortfolioMaster
          return ((MinimalWebsiteViewportsComponentFactory) bean).getUserPortfolioMaster();
        case -763459665:  // userConfigMaster
          return ((MinimalWebsiteViewportsComponentFactory) bean).getUserConfigMaster();
        case -774734430:  // combinedConfigSource
          return ((MinimalWebsiteViewportsComponentFactory) bean).getCombinedConfigSource();
        case -1697555603:  // viewProcessor
          return ((MinimalWebsiteViewportsComponentFactory) bean).getViewProcessor();
        case 1910524868:  // parallelViewRecompilation
          return ((MinimalWebsiteViewportsComponentFactory) bean).getParallelViewRecompilation();
        case 940303425:  // portfolioAggregationFunctions
          return ((MinimalWebsiteViewportsComponentFactory) bean).getPortfolioAggregationFunctions();
        case 2090650860:  // marketDataSnapshotMaster
          return ((MinimalWebsiteViewportsComponentFactory) bean).getMarketDataSnapshotMaster();
        case 3599307:  // user
          return ((MinimalWebsiteViewportsComponentFactory) bean).getUser();
        case -917704420:  // fudgeContext
          return ((MinimalWebsiteViewportsComponentFactory) bean).getFudgeContext();
        case -301472921:  // liveMarketDataProviderFactory
          return ((MinimalWebsiteViewportsComponentFactory) bean).getLiveMarketDataProviderFactory();
        case -1406342148:  // suppressCurrencyDisplay
          return ((MinimalWebsiteViewportsComponentFactory) bean).isSuppressCurrencyDisplay();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 10395716:  // configMaster
          ((MinimalWebsiteViewportsComponentFactory) bean).setConfigMaster((ConfigMaster) newValue);
          return;
        case -887218750:  // securityMaster
          ((MinimalWebsiteViewportsComponentFactory) bean).setSecurityMaster((SecurityMaster) newValue);
          return;
        case -702456965:  // securitySource
          ((MinimalWebsiteViewportsComponentFactory) bean).setSecuritySource((SecuritySource) newValue);
          return;
        case -1840419605:  // positionMaster
          ((MinimalWebsiteViewportsComponentFactory) bean).setPositionMaster((PositionMaster) newValue);
          return;
        case -772274742:  // portfolioMaster
          ((MinimalWebsiteViewportsComponentFactory) bean).setPortfolioMaster((PortfolioMaster) newValue);
          return;
        case -1655657820:  // positionSource
          ((MinimalWebsiteViewportsComponentFactory) bean).setPositionSource((PositionSource) newValue);
          return;
        case 1562222174:  // computationTargetResolver
          ((MinimalWebsiteViewportsComponentFactory) bean).setComputationTargetResolver((ComputationTargetResolver) newValue);
          return;
        case -140572773:  // functions
          ((MinimalWebsiteViewportsComponentFactory) bean).setFunctions((FunctionConfigurationSource) newValue);
          return;
        case 173967376:  // historicalTimeSeriesMaster
          ((MinimalWebsiteViewportsComponentFactory) bean).setHistoricalTimeSeriesMaster((HistoricalTimeSeriesMaster) newValue);
          return;
        case -1944474242:  // legalEntityMaster
          ((MinimalWebsiteViewportsComponentFactory) bean).setLegalEntityMaster((LegalEntityMaster) newValue);
          return;
        case 1808868758:  // userPositionMaster
          ((MinimalWebsiteViewportsComponentFactory) bean).setUserPositionMaster((PositionMaster) newValue);
          return;
        case 686514815:  // userPortfolioMaster
          ((MinimalWebsiteViewportsComponentFactory) bean).setUserPortfolioMaster((PortfolioMaster) newValue);
          return;
        case -763459665:  // userConfigMaster
          ((MinimalWebsiteViewportsComponentFactory) bean).setUserConfigMaster((ConfigMaster) newValue);
          return;
        case -774734430:  // combinedConfigSource
          ((MinimalWebsiteViewportsComponentFactory) bean).setCombinedConfigSource((ConfigSource) newValue);
          return;
        case -1697555603:  // viewProcessor
          ((MinimalWebsiteViewportsComponentFactory) bean).setViewProcessor((ViewProcessor) newValue);
          return;
        case 1910524868:  // parallelViewRecompilation
          ((MinimalWebsiteViewportsComponentFactory) bean).setParallelViewRecompilation((ExecutionFlags.ParallelRecompilationMode) newValue);
          return;
        case 940303425:  // portfolioAggregationFunctions
          ((MinimalWebsiteViewportsComponentFactory) bean).setPortfolioAggregationFunctions((PortfolioAggregationFunctions) newValue);
          return;
        case 2090650860:  // marketDataSnapshotMaster
          ((MinimalWebsiteViewportsComponentFactory) bean).setMarketDataSnapshotMaster((MarketDataSnapshotMaster) newValue);
          return;
        case 3599307:  // user
          ((MinimalWebsiteViewportsComponentFactory) bean).setUser((UserPrincipal) newValue);
          return;
        case -917704420:  // fudgeContext
          ((MinimalWebsiteViewportsComponentFactory) bean).setFudgeContext((FudgeContext) newValue);
          return;
        case -301472921:  // liveMarketDataProviderFactory
          ((MinimalWebsiteViewportsComponentFactory) bean).setLiveMarketDataProviderFactory((LiveMarketDataProviderFactory) newValue);
          return;
        case -1406342148:  // suppressCurrencyDisplay
          ((MinimalWebsiteViewportsComponentFactory) bean).setSuppressCurrencyDisplay((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((MinimalWebsiteViewportsComponentFactory) bean)._securityMaster, "securityMaster");
      JodaBeanUtils.notNull(((MinimalWebsiteViewportsComponentFactory) bean)._securitySource, "securitySource");
      JodaBeanUtils.notNull(((MinimalWebsiteViewportsComponentFactory) bean)._positionMaster, "positionMaster");
      JodaBeanUtils.notNull(((MinimalWebsiteViewportsComponentFactory) bean)._portfolioMaster, "portfolioMaster");
      JodaBeanUtils.notNull(((MinimalWebsiteViewportsComponentFactory) bean)._positionSource, "positionSource");
      JodaBeanUtils.notNull(((MinimalWebsiteViewportsComponentFactory) bean)._computationTargetResolver, "computationTargetResolver");
      JodaBeanUtils.notNull(((MinimalWebsiteViewportsComponentFactory) bean)._userPositionMaster, "userPositionMaster");
      JodaBeanUtils.notNull(((MinimalWebsiteViewportsComponentFactory) bean)._userPortfolioMaster, "userPortfolioMaster");
      JodaBeanUtils.notNull(((MinimalWebsiteViewportsComponentFactory) bean)._viewProcessor, "viewProcessor");
      JodaBeanUtils.notNull(((MinimalWebsiteViewportsComponentFactory) bean)._portfolioAggregationFunctions, "portfolioAggregationFunctions");
      JodaBeanUtils.notNull(((MinimalWebsiteViewportsComponentFactory) bean)._user, "user");
      JodaBeanUtils.notNull(((MinimalWebsiteViewportsComponentFactory) bean)._fudgeContext, "fudgeContext");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
