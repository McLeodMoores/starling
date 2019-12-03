/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.LinkedList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Lists;
import com.opengamma.engine.function.config.FunctionConfigurationDefinition;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;

/**
 * Fudge message builder for {@link FunctionConfigurationDefinition}.
 */
@FudgeBuilderFor(FunctionConfigurationDefinition.class)
public class FunctionConfigurationDefinitionFudgeBuilder implements FudgeBuilder<FunctionConfigurationDefinition> {

  private static final String NAME_FIELD = "name";
  private static final String FUNCTION_CONFIG_DEFINITION_FIELD = "configName";
  private static final String STATIC_FUNCTION_FIELD = "staticFunction";
  private static final String PARAMETERIZED_FUNCTION_FIELD = "parameterizedFunction";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FunctionConfigurationDefinition functionConfig) {

    final MutableFudgeMsg message = serializer.newMessage();

    message.add(NAME_FIELD, null, functionConfig.getName());

    final List<String> configurationDefinitions = functionConfig.getFunctionConfigurationDefinitions();
    if (!configurationDefinitions.isEmpty()) {
      for (final String functionConfigName : configurationDefinitions) {
        message.add(FUNCTION_CONFIG_DEFINITION_FIELD, null, functionConfigName);
      }
    }

    final List<StaticFunctionConfiguration> staticFunctions = functionConfig.getStaticFunctions();
    if (!staticFunctions.isEmpty()) {
      for (final StaticFunctionConfiguration staticFunctionConfiguration : staticFunctions) {
        message.add(STATIC_FUNCTION_FIELD, null, staticFunctionConfiguration.getDefinitionClassName());
      }
    }

    final List<ParameterizedFunctionConfiguration> parameterizedFunctions = functionConfig.getParameterizedFunctions();
    if (!parameterizedFunctions.isEmpty()) {
      for (final ParameterizedFunctionConfiguration configuration : parameterizedFunctions) {
        final MutableFudgeMsg parametizedMsg = serializer.newMessage();
        parametizedMsg.add("func", null, configuration.getDefinitionClassName());
        for (final String parameter : configuration.getParameter()) {
          parametizedMsg.add("param", null, parameter);
        }
        message.add(PARAMETERIZED_FUNCTION_FIELD, null, parametizedMsg);
      }
    }
    return message;
  }

  @Override
  public FunctionConfigurationDefinition buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {

    final String name = message.getString(NAME_FIELD);

    final List<String> functionConfigurationDefinitions = new LinkedList<>();
    final List<StaticFunctionConfiguration> staticFunctions = new LinkedList<>();
    final List<ParameterizedFunctionConfiguration> parameterizedFunctions = new LinkedList<>();

    if (message.hasField(FUNCTION_CONFIG_DEFINITION_FIELD)) {
      final List<FudgeField> allConfigs = message.getAllByName(FUNCTION_CONFIG_DEFINITION_FIELD);
      for (final FudgeField fudgeField : allConfigs) {
        functionConfigurationDefinitions.add((String) fudgeField.getValue());
      }
    }

    if (message.hasField(STATIC_FUNCTION_FIELD)) {
      final List<FudgeField> allStaticFunctions = message.getAllByName(STATIC_FUNCTION_FIELD);
      for (final FudgeField fudgeField : allStaticFunctions) {
        staticFunctions.add(new StaticFunctionConfiguration((String) fudgeField.getValue()));
      }
    }

    if (message.hasField(PARAMETERIZED_FUNCTION_FIELD)) {
      final List<FudgeField> allConfigs = message.getAllByName(PARAMETERIZED_FUNCTION_FIELD);
      for (final FudgeField configField : allConfigs) {
        final FudgeMsg parameterizedMsg = (FudgeMsg) configField.getValue();
        final String definitionClassName = parameterizedMsg.getString("func");
        final List<FudgeField> parameterFields = parameterizedMsg.getAllByName("param");
        final List<String> parameters = Lists.newArrayList();
        for (final FudgeField parameterField : parameterFields) {
          parameters.add((String) parameterField.getValue());
        }
        parameterizedFunctions.add(new ParameterizedFunctionConfiguration(definitionClassName, parameters));
      }
    }

    return new FunctionConfigurationDefinition(name, functionConfigurationDefinitions, staticFunctions, parameterizedFunctions);
  }

}
