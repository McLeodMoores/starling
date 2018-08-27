/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.DepthFirstPortfolioNodeTraverser;
import com.opengamma.core.position.impl.PortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.MissingValue;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Class to analyze view processor result sets and return statistics about available results.
 */
public class ViewProcessStatsProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewProcessStatsProcessor.class);

  private final CompiledViewDefinition _compiledViewDef;
  private final ViewComputationResultModel _viewComputationResultModel;
  private int _successes;
  private int _failures;
  private int _errors;
  private int _total;

  public ViewProcessStatsProcessor(final CompiledViewDefinition compiledViewDef, final ViewComputationResultModel viewComputationResultModel) {
    _compiledViewDef = compiledViewDef;
    _viewComputationResultModel = viewComputationResultModel;
  }

  public void processResult() {

    final ViewDefinition viewDefinition = _compiledViewDef.getViewDefinition();
    for (final String calcConfigName : viewDefinition.getAllCalculationConfigurationNames()) {
      final ViewCalculationConfiguration calcConfig = viewDefinition.getCalculationConfiguration(calcConfigName);
      final ValueMappings valueMappings = new ValueMappings(_compiledViewDef);
      final ViewCalculationResultModel calculationResult = _viewComputationResultModel.getCalculationResult(calcConfigName);
      final Map<String, Set<Pair<String, ValueProperties>>> portfolioRequirementsBySecurityType = calcConfig.getPortfolioRequirementsBySecurityType();
      final Portfolio portfolio = _compiledViewDef.getPortfolio();
      final PortfolioNodeTraverser traverser = new DepthFirstPortfolioNodeTraverser(new PortfolioNodeTraversalCallback() {

        @Override
        public void preOrderOperation(final PortfolioNode parentNode, final Position position) {
          final UniqueId positionId = position.getUniqueId().toLatest();
          // then construct a chained target spec pointing at a specific position.
          final ComputationTargetSpecification breadcrumbTargetSpec = ComputationTargetSpecification.of(parentNode).containing(ComputationTargetType.POSITION, positionId);
          final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(position);
          final Map<Pair<String, ValueProperties>, ComputedValueResult> values = calculationResult.getValues(targetSpec);
          final String securityType = position.getSecurity().getSecurityType();
          final Set<Pair<String, ValueProperties>> valueRequirements = portfolioRequirementsBySecurityType.get(securityType);
          LOGGER.info("Processing valueRequirement " + valueRequirements + " for security type " + securityType);
          if (valueRequirements != null) {
            for (final Pair<String, ValueProperties> valueRequirement : valueRequirements) {
              final ValueRequirement valueReq = new ValueRequirement(valueRequirement.getFirst(), breadcrumbTargetSpec, valueRequirement.getSecond());
              final ValueSpecification valueSpec = valueMappings.getValueSpecification(calcConfigName, valueReq);
              if (valueSpec == null) {
                LOGGER.debug("Couldn't get reverse value spec mapping from requirement: " + valueReq.toString());
                _failures++;
              } else {
                final Pair<String, ValueProperties> valueKey = Pairs.of(valueSpec.getValueName(), valueSpec.getProperties());
                final ComputedValueResult computedValueResult = values != null ? values.get(valueKey) : null;
                if (computedValueResult != null) {
                  if (computedValueResult.getValue() instanceof MissingValue) {
                    _errors++;
                  } else {
                    _successes++;
                  }
                } else {
                  _failures++;
                }
              }
              _total++;
            }
          }
        }

        @Override
        public void preOrderOperation(final PortfolioNode portfolioNode) {}

        @Override
        public void postOrderOperation(final PortfolioNode parentNode, final Position position) {}

        @Override
        public void postOrderOperation(final PortfolioNode portfolioNode) {}

      });
      traverser.traverse(portfolio.getRootNode());
    }
  }



  public int getSuccesses() {
    return _successes;
  }

  public int getFailures() {
    return _failures;
  }

  public int getErrors() {
    return _errors;
  }

  public int getTotal() {
    return _total;
  }

  public double getSuccessPercentage() {
    return 100d * _successes / _total;
  }

}
