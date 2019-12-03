/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.function;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.threeten.bp.Instant;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;

/**
 * Converts function configurations into a format suitable for viewing on the web. Can also handle filtering of query results via a predicate method.
 */
class WebFunctionQueryDelegate {

  private static final Pattern SIMPLE_NAME = Pattern.compile("([^\\.]+)$");

  private final FunctionConfigurationSource _functionConfigurationSource;

  WebFunctionQueryDelegate(final FunctionConfigurationSource functionConfigurationSource) {
    _functionConfigurationSource = functionConfigurationSource;
  }

  /**
   * @return all function type details
   */
  public SortedMap<String, WebFunctionTypeDetails> queryAll() {
    return query(Predicates.<WebFunctionTypeDetails> alwaysTrue());
  }

  /**
   * Returns the types which match the predicate.
   *
   * @param predicate
   *          the predicate
   * @return a map from name to function details that matched predicate
   */
  public SortedMap<String, WebFunctionTypeDetails> query(final Predicate<WebFunctionTypeDetails> predicate) {
    final FunctionConfigurationBundle functionConfiguration = _functionConfigurationSource.getFunctionConfiguration(Instant.now());

    final SortedMap<String, WebFunctionTypeDetails> allFunctions = Maps.newTreeMap();

    for (final FunctionConfiguration input : functionConfiguration.getFunctions()) {
      final StaticFunctionConfiguration config = (StaticFunctionConfiguration) input;
      final String fullName = config.getDefinitionClassName();

      final Matcher matcher = SIMPLE_NAME.matcher(fullName);
      final String simpleName = matcher.find() ? matcher.group(1) : "Unknown";

      final WebFunctionTypeDetails typeDetails = new WebFunctionTypeDetails();
      typeDetails.setFullyQualifiedName(fullName);
      typeDetails.setSimpleName(simpleName);

      final boolean isParameterized = config instanceof ParameterizedFunctionConfiguration;
      typeDetails.setParameterized(isParameterized);

      List<String> parameters;
      if (isParameterized) {
        parameters = ((ParameterizedFunctionConfiguration) config).getParameter();
      } else {
        parameters = Lists.newArrayList();
      }

      final List<List<String>> parametersList = Lists.newLinkedList();
      parametersList.add(parameters);
      typeDetails.setParameters(parametersList);

      if (!predicate.apply(typeDetails)) {
        continue;
      }

      if (allFunctions.containsKey(simpleName)) {
        allFunctions.get(simpleName).getParameters().add(parameters);
      } else {
        allFunctions.put(simpleName, typeDetails);
      }
    }

    for (final WebFunctionTypeDetails typeDetails : allFunctions.values()) {
      if (typeDetails.isParameterized()) {
        sortParameters(typeDetails);
      }
    }

    return allFunctions;

  }

  private static void sortParameters(final WebFunctionTypeDetails typeDetails) {

    final List<List<String>> parameters = typeDetails.getParameters();

    final Ordering<Object> stringOrdering = Ordering.usingToString();

    Collections.sort(parameters, new Comparator<List<String>>() {

      @Override
      public int compare(final List<String> o1, final List<String> o2) {

        for (int i = 0; i < Ints.min(o1.size(), o2.size()); i++) {
          final int compare = stringOrdering.compare(o1, o2);
          if (compare != 0) {
            return compare;
          }
        }

        return 0;
      }
    });

  }

}
