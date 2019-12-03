/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple immutable class defining a scenario which holds a map of the market
 * data manipulation targets (e.g. USD 3M Yield Curve) and the manipulations
 * to be performed (e.g. shift by +10bps).
 *
 * ScenarioDefinitions can be stored in the config master and used in the
 * setup of ViewDefinitions.
 */
public class ScenarioDefinition implements ScenarioDefinitionFactory {

  private static final String NAME = "name";
  private static final String SELECTOR = "selector";
  private static final String DEFINITION_MAP = "definitionMap";
  private static final String FUNCTION_PARAMETERS = "functionParameters";

  private final String _name;
  private final Map<DistinctMarketDataSelector, FunctionParameters> _definitionMap;

  public ScenarioDefinition(final String name, final Map<DistinctMarketDataSelector, FunctionParameters> definitionMap) {
    ArgumentChecker.notEmpty(name, "name");
    _name = name;
    _definitionMap = ImmutableMap.copyOf(definitionMap);
  }

  /**
   * Return an immutable map of the market data selectors to function parameters.
   *
   * @return market data to function parameters mapping
   */
  public Map<DistinctMarketDataSelector, FunctionParameters> getDefinitionMap() {
    return _definitionMap;
  }

  /**
   * @return The scenario name, not null
   */
  public String getName() {
    return _name;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, NAME, null, _name);
    final MutableFudgeMsg mapMsg = serializer.newMessage();
    for (final Map.Entry<DistinctMarketDataSelector, FunctionParameters> entry : _definitionMap.entrySet()) {
      final MutableFudgeMsg entryMsg = serializer.newMessage();
      final DistinctMarketDataSelector selector = entry.getKey();
      final FunctionParameters parameters = entry.getValue();
      serializer.addToMessageWithClassHeaders(entryMsg, SELECTOR, null, selector);
      serializer.addToMessageWithClassHeaders(entryMsg, FUNCTION_PARAMETERS, null, parameters);
      serializer.addToMessage(mapMsg, null, null, entryMsg);
    }
    serializer.addToMessage(msg, DEFINITION_MAP, null, mapMsg);
    return msg;
  }

  public static ScenarioDefinition fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final String name = deserializer.fieldValueToObject(String.class, msg.getByName(NAME));
    final Map<DistinctMarketDataSelector, FunctionParameters> definitionMap = Maps.newHashMap();
    if (msg.hasField(DEFINITION_MAP)) {
      final FudgeMsg mapMsg = msg.getMessage(DEFINITION_MAP);
      for (final FudgeField field : mapMsg) {
        final FudgeMsg entryMsg = (FudgeMsg) field.getValue();
        final FudgeField selectorField = entryMsg.getByName(SELECTOR);
        final DistinctMarketDataSelector selector = deserializer.fieldValueToObject(DistinctMarketDataSelector.class, selectorField);
        final FudgeField paramsField = entryMsg.getByName(FUNCTION_PARAMETERS);
        final FunctionParameters parameters = deserializer.fieldValueToObject(FunctionParameters.class, paramsField);
        definitionMap.put(selector, parameters);
      }
    }
    return new ScenarioDefinition(name, definitionMap);
  }

  @Override
  public ScenarioDefinition create(final Map<String, Object> parameters) {
    return this;
  }
}
