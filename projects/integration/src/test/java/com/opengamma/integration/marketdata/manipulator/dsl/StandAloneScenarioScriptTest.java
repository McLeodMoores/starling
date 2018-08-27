/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static com.opengamma.integration.marketdata.manipulator.dsl.MarketDataDelegate.MarketDataType.FIXED_HISTORICAL;
import static com.opengamma.integration.marketdata.manipulator.dsl.MarketDataDelegate.MarketDataType.LATEST_HISTORICAL;
import static com.opengamma.integration.marketdata.manipulator.dsl.MarketDataDelegate.MarketDataType.LIVE;
import static com.opengamma.integration.marketdata.manipulator.dsl.MarketDataDelegate.MarketDataType.SNAPSHOT;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.id.ExternalId;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.test.TestGroup;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

@Test(groups = TestGroup.UNIT)
public class StandAloneScenarioScriptTest {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private static final String FOO = "foo";
  private static final String BAR = "bar";

  @Test
  public void view() {
    final String scriptText =
        "view {\n" +
        "  name 'an example view'\n" +
        "  server 'svr:8080'\n" +
        "}";
    final StandAloneScenarioScript script = createScript(scriptText);
    final ViewDelegate viewDelegate = script.getViewDelegate();
    assertEquals("an example view", viewDelegate.getName());
    assertEquals("svr:8080", viewDelegate.getServer());
  }

  @Test
  public void marketData() {
    final String scriptText =
        "view {\n" +
        "  marketData {\n" +
        "    live 'Bloomberg'\n" +
        "    live 'Activ'\n" +
        "    snapshot 'the snapshot name'\n" +
        "    fixedHistorical '2011-03-08 11:30'\n" +
        "    latestHistorical\n" +
        "    fixedHistorical '2012-03-10 12:30'\n" +
        "  }\n" +
        "}";
    final StandAloneScenarioScript script = createScript(scriptText);
    final ImmutableList<MarketDataDelegate.MarketDataSpec> expectedSpecs =
        ImmutableList.of(spec(LIVE, "Bloomberg"),
                         spec(LIVE, "Activ"),
                         spec(SNAPSHOT, "the snapshot name"),
                         spec(FIXED_HISTORICAL, "2011-03-08 11:30"),
                         spec(LATEST_HISTORICAL, null),
                         spec(FIXED_HISTORICAL, "2012-03-10 12:30"));
    final MarketDataDelegate marketDataDelegate = script.getViewDelegate().getMarketDataDelegate();
    assertEquals(expectedSpecs, marketDataDelegate.getSpecifications());
  }

  @Test
  public void shockList() {
    final String scriptText =
        "shockList {\n" +
        "  foo = [1, 2, 3]\n" +
        "  bar = ['a', 'b', 'c']\n" +
        "}";
    final StandAloneScenarioScript script = createScript(scriptText);
    final List<Map<String, Object>> params = script.getScenarioParameterList();
    assertEquals(3, params.size());

    assertEquals(1, params.get(0).get(FOO));
    assertEquals("a", params.get(0).get(BAR));

    assertEquals(2, params.get(1).get(FOO));
    assertEquals("b", params.get(1).get(BAR));

    assertEquals(3, params.get(2).get(FOO));
    assertEquals("c", params.get(2).get(BAR));
  }

  @Test
  public void shockGrid() {
    final String scriptText =
        "shockGrid {\n" +
        "  foo = [1, 2]\n" +
        "  bar = ['a', 'b']\n" +
        "}";
    final StandAloneScenarioScript script = createScript(scriptText);
    final List<Map<String, Object>> params = script.getScenarioParameterList();
    assertEquals(4, params.size());

    assertEquals(1, params.get(0).get(FOO));
    assertEquals("a", params.get(0).get(BAR));

    assertEquals(1, params.get(1).get(FOO));
    assertEquals("b", params.get(1).get(BAR));

    assertEquals(2, params.get(2).get(FOO));
    assertEquals("a", params.get(2).get(BAR));

    assertEquals(2, params.get(3).get(FOO));
    assertEquals("b", params.get(3).get(BAR));
  }

  @Test
  public void scenarioList() {
    final String scriptText =
        "shockList {\n" +
        "  foo = [1, 2]\n" +
        "  bar = ['a', 'b']\n" +
        "  valTime = ['2014-01-14 12:03', '2014-02-14 12:03']\n" +
        "}\n" +
        "scenarios {\n" +
        "  valuationTime valTime\n" +
        "  marketData {\n" +
        "    id 'SCHEME', bar\n" +
        "    apply {\n" +
        "      shift Absolute, foo\n" +
        "    }\n" +
        "  }\n" +
        "}";
    final StandAloneScenarioScript script = createScript(scriptText);

    final Simulation simulation = script.getSimulation();
    final Map<String, Scenario> scenarios = simulation.getScenarios();
    assertEquals(2, scenarios.size());

    checkScenario(scenarios, "a", 1, "2014-01-14 12:03");
    checkScenario(scenarios, "b", 2, "2014-02-14 12:03");
  }

  @Test
  public void scenarioGrid() {
    final String scriptText =
        "shockGrid {\n" +
        "  foo = [1, 2]\n" +
        "  bar = ['a', 'b']\n" +
        "}\n" +
        "scenarios {\n" +
        "  marketData {\n" +
        "    id 'SCHEME', bar\n" +
        "    apply {\n" +
        "      shift Absolute, foo\n" +
        "    }\n" +
        "  }\n" +
        "}";
    final StandAloneScenarioScript script = createScript(scriptText);

    final Simulation simulation = script.getSimulation();
    final Map<String, Scenario> scenarios = simulation.getScenarios();
    assertEquals(4, scenarios.size());
    checkScenario(scenarios, "a", 1, null);
    checkScenario(scenarios, "a", 2, null);
    checkScenario(scenarios, "b", 1, null);
    checkScenario(scenarios, "b", 2, null);
  }

  @Test
  public void initialize() {
    final String scriptText =
        "shockList {\n" +
        "  foo = [100.bp, 20.pc, 1.y]\n" +
        "}";
    final StandAloneScenarioScript script = createScript(scriptText);
    final List<Map<String, Object>> params = script.getScenarioParameterList();
    assertEquals(3, params.size());

    assertEquals(new BigDecimal("0.01"), params.get(0).get(FOO));
    assertEquals(new BigDecimal("0.2"), params.get(1).get(FOO));
    assertEquals(Period.ofYears(1), params.get(2).get(FOO));
  }

  private static void checkScenario(final Map<String, Scenario> scenarios, final String id, final double shiftAmount, final String valuationTime) {
    String scenarioName;
    if (valuationTime != null) {
      scenarioName = "foo=" + (int) shiftAmount + " bar='" + id + "' valTime='" + valuationTime + "'";
    } else {
      scenarioName = "foo=" + (int) shiftAmount + " bar='" + id + "'";
    }
    final Scenario scenario = scenarios.get(scenarioName);
    assertNotNull(scenario);
    final ScenarioDefinition definition = scenario.createDefinition();
    Instant valuationInstant;
    if (valuationTime != null) {
      final LocalDateTime localTime = LocalDateTime.parse(valuationTime, DATE_FORMATTER);
      valuationInstant = ZonedDateTime.of(localTime, OpenGammaClock.getZone()).toInstant();
    } else {
      valuationInstant = null;
    }
    assertEquals(valuationInstant, scenario.getValuationTime());
    final Map<DistinctMarketDataSelector, FunctionParameters> definitionMap = definition.getDefinitionMap();
    final PointSelector selector =
        new PointSelector(null, ImmutableSet.of(ExternalId.of("SCHEME", id)), null, null, null, null, null);
    final FunctionParameters parameters = definitionMap.get(selector);
    final CompositeStructureManipulator compositeManipulator =
        ((SimpleFunctionParameters) parameters).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    final MarketDataShift shift = (MarketDataShift) compositeManipulator.getManipulators().get(0);
    assertEquals(1 + shiftAmount, shift.execute(1.0, null, null));
  }

  private static StandAloneScenarioScript createScript(final String scriptText) {
    final CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(StandAloneScenarioScript.class.getName());
    final GroovyShell shell = new GroovyShell(config);
    final StandAloneScenarioScript script = (StandAloneScenarioScript) shell.parse(scriptText);
    final Binding binding = new Binding();
    SimulationUtils.registerAliases(binding);
    script.setBinding(binding);
    script.run();
    return script;
  }

  private static MarketDataDelegate.MarketDataSpec spec(final MarketDataDelegate.MarketDataType type, final String spec) {
    return new MarketDataDelegate.MarketDataSpec(type, spec);
  }
}
