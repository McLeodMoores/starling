/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Script;

/**
 * TODO enforce ordering ordering: view, shocks, scenarios.
 */
@SuppressWarnings("unused") // it is used reflectively by Groovy
public abstract class StandAloneScenarioScript extends Script {

  private final ViewDelegate _viewDelegate = new ViewDelegate();
  private final List<Map<String, Object>> _scenarioParamList = Lists.newArrayList();
  private final Simulation _simulation = new Simulation("todo - what name for the simulation? does it matter?");

  /** Scenario parameters keyed by scenario name. The parameters are parameter values keyed by parameter name. */
  private final Map<String, Map<String, Object>> _scenarioParameters = Maps.newHashMap();

  public StandAloneScenarioScript() {
    final InputStream scriptStream = SimulationScript.class.getResourceAsStream("InitializeScript.groovy");
    try {
      evaluate(IOUtils.toString(scriptStream));
    } catch (final IOException e) {
      throw new OpenGammaRuntimeException("Failed to initialize DSL script", e);
    }
  }

  public void view(final Closure<?> body) {
    body.setDelegate(_viewDelegate);
    body.call();
  }

  public void shockGrid(final Closure<?> body) {
    final ShocksDelegate delegate = new ShocksDelegate();
    body.setDelegate(delegate);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    _scenarioParamList.addAll(delegate.cartesianProduct());
  }

  public void shockList(final Closure<?> body) {
    final ShocksDelegate delegate = new ShocksDelegate();
    body.setDelegate(delegate);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    _scenarioParamList.addAll(delegate.list());
  }

  public void scenarios(final Closure<?> body) {
    for (final Map<String, Object> params : _scenarioParamList) {
      final List<String> varNamesAndValues = Lists.newArrayListWithCapacity(params.size());
      for (final Map.Entry<String, Object> entry : params.entrySet()) {
        final String varName = entry.getKey();
        final Object varValue = entry.getValue();
        getBinding().setVariable(varName, varValue);
        final String varStr = varValue instanceof String ? "'" + varValue + "'" : varValue.toString();
        varNamesAndValues.add(varName + "=" + varStr);
      }
      final String scenarioName = StringUtils.join(varNamesAndValues, " ");
      final Scenario scenario = _simulation.scenario(scenarioName);
      _scenarioParameters.put(scenarioName, params);
      body.setDelegate(new ScenarioDelegate(scenario));
      body.call();
    }
  }

  /**
   * Visible for testing.
   *
   * @return the view delegate
   */
  /* package */ ViewDelegate getViewDelegate() {
    return _viewDelegate;
  }

  /* package */ List<Map<String, Object>> getScenarioParameterList() {
    return Collections.unmodifiableList(_scenarioParamList);
  }

  /* package */ Map<String, Object> getScenarioParameters(final String scenarioName) {
    final Map<String, Object> parameters = _scenarioParameters.get(scenarioName);
    if (parameters == null) {
      throw new IllegalArgumentException("No scenario found named " + scenarioName);
    }
    return parameters;
  }

  /* package */ Simulation getSimulation() {
    return _simulation;
  }
}

// TODO abstract delegate that catches unexpected method and property calls and logs them

// TODO extend the abstract delegate
// TODO should this contain the logic to validate and convert the fields? or should it just be a dumb container?
@SuppressWarnings("unused")
/* package */ class ViewDelegate {

  // private final DateTimeFormatter _dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private String _name;
  private String _server;
  private final MarketDataDelegate _marketDataDelegate = new MarketDataDelegate();

  /* package */ void name(final String name) {
    _name = name;
  }

  /* package */ void server(final String server) {
    _server = server;
  }

  /* package */ void marketData(final Closure<?> body) {
    body.setDelegate(_marketDataDelegate);
    body.call();
  }

  /* package */ String getName() {
    return _name;
  }

  /* package */ String getServer() {
    return _server;
  }

  /**
   * Visible for testing.
   *
   * @return the market data delegate
   */
  /* package */ MarketDataDelegate getMarketDataDelegate() {
    return _marketDataDelegate;
  }
}

@SuppressWarnings("unused")
/* package */ class MarketDataDelegate {

  private final List<MarketDataSpec> _specifications = Lists.newArrayList();

  /* package */ void live(final String dataSource) {
    _specifications.add(new MarketDataSpec(MarketDataType.LIVE, dataSource));
  }

  /* package */ void snapshot(final String snapshotName) {
    _specifications.add(new MarketDataSpec(MarketDataType.SNAPSHOT, snapshotName));
  }

  /* package */ void fixedHistorical(final String date) {
    _specifications.add(new MarketDataSpec(MarketDataType.FIXED_HISTORICAL, date));
  }

  /* latestHistorical doesn't have arguments so Groovy views it as a property get */
  /* package */ Object getLatestHistorical() {
    _specifications.add(new MarketDataSpec(MarketDataType.LATEST_HISTORICAL, null));
    return null;
  }

  /* package */ List<MarketDataSpec> getSpecifications() {
    return _specifications;
  }

  /* package */ enum MarketDataType {
    LIVE, SNAPSHOT, FIXED_HISTORICAL, LATEST_HISTORICAL
  }

  /* package */ static class MarketDataSpec {

    private final MarketDataType _type;
    private final String _spec;

    /* package */ MarketDataSpec(final MarketDataType type, final String spec) {
      _type = type;
      _spec = spec;
    }

    /* package */ MarketDataType getType() {
      return _type;
    }

    /* package */ String getSpec() {
      return _spec;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_type, _spec);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final MarketDataSpec other = (MarketDataSpec) obj;
      return Objects.equals(this._type, other._type) && Objects.equals(this._spec, other._spec);
    }
  }
}

/* package */ class ShocksDelegate extends GroovyObjectSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(ShocksDelegate.class);

  /**
   * Shock variables. Keys are the variable names, values must be lists of values. It's a linked map so the declaration order in the script is reflected in the
   * way the cartesian product is generated.
   */
  private final Map<String, List<?>> _vars = Maps.newLinkedHashMap();

  @Override
  public void setProperty(final String property, final Object newValue) {
    if (!(newValue instanceof List)) {
      LOGGER.warn("Shocks must be a list, type=" + newValue.getClass().getName() + ", value=" + newValue);
      return;
    }
    if (((List<?>) newValue).size() == 0) {
      LOGGER.warn("Shocks must have at least one value");
      return;
    }
    _vars.put(property, (List<?>) newValue);
  }

  /* package */ List<Map<String, Object>> cartesianProduct() {
    if (_vars.isEmpty()) {
      return Collections.emptyList();
    }
    if (_vars.size() != 2) {
      throw new IllegalArgumentException("There must be 2 sets of shocks for shockGrid. For 1 set, use shockList.");
    }
    final Iterator<Map.Entry<String, List<?>>> itr = _vars.entrySet().iterator();

    final Map.Entry<String, List<?>> outer = itr.next();
    final String outerVarName = outer.getKey();
    final List<?> outerVarValues = outer.getValue();

    final Map.Entry<String, List<?>> inner = itr.next();
    final String innerVarName = inner.getKey();
    final List<?> innerVarValues = inner.getValue();

    // list of parameters, one map for each scenario
    final List<Map<String, Object>> params = Lists.newArrayListWithCapacity(outerVarValues.size() * innerVarValues.size());

    for (final Object outerVarValue : outerVarValues) {
      for (final Object innerVarValue : innerVarValues) {
        // use a linked map so the var names appear in insertion order - this makes for predictable scenario names
        final Map<String, Object> paramMap = Maps.newLinkedHashMap();
        paramMap.put(outerVarName, outerVarValue);
        paramMap.put(innerVarName, innerVarValue);
        params.add(paramMap);
      }
    }
    return params;
  }

  /* package */ List<Map<String, Object>> list() {
    // list of parameters, one map for each scenario
    final List<Map<String, Object>> params = Lists.newArrayList();

    // calculate number of scenarios and make sure all shock lists have the right number of values
    int nScenarios = 0;
    for (final List<?> varValues : _vars.values()) {
      if (nScenarios == 0) {
        nScenarios = varValues.size();
      } else if (nScenarios != varValues.size()) {
        throw new IllegalArgumentException("All scenario parameters must be lists of the same length");
      }
    }
    // create a map for each scenario and populate it with a value from each shock list
    for (int i = 0; i < nScenarios; i++) {
      // use a linked map so the var names appear in insertion order - this makes for predictable scenario names
      final Map<String, Object> map = Maps.newLinkedHashMap();
      for (final Map.Entry<String, List<?>> entry : _vars.entrySet()) {
        final String varName = entry.getKey();
        final List<?> varValues = entry.getValue();
        map.put(varName, varValues.get(i));
      }
      params.add(map);
    }
    return params;
  }
}

/**
 * Delegate for the closure passed to the {@code scenarios} block in the script.
 */
/* package */ class ScenarioDelegate {

  private final Scenario _scenario;

  /* package */ ScenarioDelegate(final Scenario scenario) {
    _scenario = scenario;
  }

  public void valuationTime(final String valuationTime) {
    _scenario.valuationTime(valuationTime);
  }

  public void calculationConfigurations(final String... configNames) {
    _scenario.calculationConfigurations(configNames);
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform a curve.
   *
   * @param body
   *          The block that defines the selection and transformation
   */
  public void curve(final Closure<?> body) {
    final DslYieldCurveSelectorBuilder selector = new DslYieldCurveSelectorBuilder(_scenario);
    body.setDelegate(selector);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform a market data point.
   *
   * @param body
   *          The block that defines the selection and transformation
   */
  public void marketData(final Closure<?> body) {
    final DslPointSelectorBuilder selector = new DslPointSelectorBuilder(_scenario);
    body.setDelegate(selector);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform a volatility surface.
   *
   * @param body
   *          The block that defines the selection and transformation
   */
  public void surface(final Closure<?> body) {
    final DslVolatilitySurfaceSelectorBuilder selector = new DslVolatilitySurfaceSelectorBuilder(_scenario);
    body.setDelegate(selector);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform spot rates.
   *
   * @param body
   *          The block that defines the selection and transformation
   */
  public void spotRate(final Closure<?> body) {
    final DslSpotRateSelectorBuilder builder = new DslSpotRateSelectorBuilder(_scenario);
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }
}
