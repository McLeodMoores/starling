/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.management.ValueMappings;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.cycle.ViewCycle;

/**
 * Builds the row and column structure of a dependency graph grid given the compiled view definition and the target at the root of the graph.
 */
/* package */class DependencyGraphStructureBuilder {

  /** {@link ValueSpecification}s for all rows in the grid in row index order. */
  private final List<ValueSpecification> _valueSpecifications = Lists.newArrayList();
  /** Function names for all rows in the grid in row index order. */
  private final List<String> _fnNames = Lists.newArrayList();
  /** The grid structure. */
  private final DependencyGraphGridStructure _structure;
  private final FunctionRepository _functions;
  @SuppressWarnings("unused")
  private final ValueMappings _valueMappings;

  /** Mutable variable for keeping track of the index of the last row */
  private int _lastRow;

  /**
   * @param compiledViewDef
   *          The compiled view definition containing the dependency graph
   * @param rootValueRequirement
   *          value requirement for the root cell
   * @param calcConfigName
   *          The calculation configuration used when calculating the value
   * @param targetResolver
   *          For looking up calculation targets given their specification
   * @param functions
   *          the function repository
   * @param cycle
   *          The most recent view cycle
   * @param valueMappings
   *          the mappings between requirements and specifcations
   */
  /* package */DependencyGraphStructureBuilder(final CompiledViewDefinition compiledViewDef, final ValueRequirement rootValueRequirement, final String calcConfigName,
      final ComputationTargetResolver targetResolver, final FunctionRepository functions, final ViewCycle cycle, final ValueMappings valueMappings) {
    // TODO see [PLAT-2478] this is a bit nasty
    // with this hack in place the user can open a dependency graph before the first set of results arrives
    // and see the graph structure with no values. without this hack the graph would be completely empty.
    // it only works if this class is running in the same VM as the engine
    //
    // if the engine and the web components are in a different VM then compiledViewDef won't be an instance of
    // CompiledViewDefinitionWithGraphs and the hack won't work. in that case the view cycle will be empty and the
    // user won't see a dependency graph if this is called before the first set of results arrives.
    // as soon as the first set of results arrives it will work the same as if all the components are in the same VM
    CompiledViewDefinitionWithGraphs viewDef;
    if (compiledViewDef instanceof CompiledViewDefinitionWithGraphs) {
      viewDef = (CompiledViewDefinitionWithGraphs) compiledViewDef;
    } else {
      viewDef = cycle.getCompiledViewDefinition();
    }
    final ValueSpecification rootValueSpecification = valueMappings.getValueSpecification(calcConfigName, rootValueRequirement);
    final DependencyGraphExplorer depGraphExplorer = viewDef.getDependencyGraphExplorer(calcConfigName);
    final DependencyNode rootNode = depGraphExplorer.getNodeProducing(rootValueSpecification);
    _functions = functions;
    _valueMappings = valueMappings;
    final AnalyticsNode node = rootNode != null ? createNode(rootValueSpecification, rootNode, true) : null;
    _structure = new DependencyGraphGridStructure(node, calcConfigName, _valueSpecifications, _fnNames, targetResolver);
  }

  private String getFunctionName(final String functionId) {
    final FunctionDefinition function = _functions.getFunction(functionId);
    if (function != null) {
      return function.getShortName();
    }
    return functionId;
  }

  /**
   * Builds the tree structure of the graph starting at a node and working up the dependency graph through all the nodes it depends on. Recursively builds up the node structure representing whole the
   * dependency graph.
   *
   * @param valueSpecification The value specification of the target that is the current root
   * @param targetNode The node producing {@code valueSpec}, not null
   * @param rootNode Whether the value specification is for the root node of the dependency graph
   * @return Root node of the grid structure representing the dependency graph for the value
   */
  private AnalyticsNode createNode(final ValueSpecification valueSpecification, final DependencyNode targetNode, final boolean rootNode) {
    _valueSpecifications.add(valueSpecification);
    _fnNames.add(getFunctionName(targetNode.getFunction().getFunctionId()));
    final int nodeStart = _lastRow;
    final List<AnalyticsNode> nodes = Lists.newArrayList();
    final int inputCount = targetNode.getInputCount();
    if (inputCount == 0) {
      if (rootNode) {
        // the root node should never be null even if it has no children
        return new AnalyticsNode(nodeStart, _lastRow, Collections.<AnalyticsNode>emptyList(), false);
      }
      // non-root leaf nodes don't need a node of their own, their place in the structure is handled by their parent
      return null;
    }
    for (int i = 0; i < inputCount; i++) {
      ++_lastRow;
      final AnalyticsNode newNode = createNode(targetNode.getInputValue(i), targetNode.getInputNode(i), false);
      if (newNode != null) {
        nodes.add(newNode);
      }
    }
    return new AnalyticsNode(nodeStart, _lastRow, Collections.unmodifiableList(nodes), false);
  }

  /**
   * @return The grid structure
   */
  /* package */DependencyGraphGridStructure getStructure() {
    return _structure;
  }
}
