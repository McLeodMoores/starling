/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.covariance;

import java.util.Set;

import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Uses the risk factor values to create the covariance matrix.
 */
public class RiskFactorCovarianceMatrixFunction extends SampledCovarianceMatrixFunction {

  /* (non-Javadoc)
   * @see com.opengamma.engine.function.FunctionInvoker#execute(com.opengamma.engine.function.FunctionExecutionContext, com.opengamma.engine.function.FunctionInputs, com.opengamma.engine.ComputationTarget, java.util.Set)
   */
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.opengamma.financial.analytics.covariance.SampledCovarianceMatrixFunction#getDataType()
   */
  @Override
  protected String getDataType() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.opengamma.financial.analytics.covariance.SampledCovarianceMatrixFunction#addValueRequirements(com.opengamma.engine.function.FunctionCompilationContext, com.opengamma.core.position.Position, com.opengamma.engine.view.ViewCalculationConfiguration)
   */
  @Override
  protected void addValueRequirements(final FunctionCompilationContext context, final Position position, final ViewCalculationConfiguration calcConfig) {
    // TODO Auto-generated method stub

  }

  //  // FunctionDefinition
  //
  //  /**
  //   * Initializes the function.
  //   *
  //   * @param context
  //   *          the compilation context
  //   * @deprecated [PLAT-2240] This just checks whether the context is suitably configured
  //   */
  //  @Deprecated
  //  @Override
  //  public void init(final FunctionCompilationContext context) {
  //    super.init(context);
  //    if (OpenGammaCompilationContext.getRiskFactorsGatherer(context) == null) {
  //      throw new IllegalStateException("Function compilation context does not contain " + OpenGammaCompilationContext.RISK_FACTORS_GATHERER_NAME);
  //    }
  //  }
  //
  //  // SampledCovarianceMatrix
  //
  //  @Override
  //  protected String getDataType() {
  //    return "RiskFactors";
  //  }
  //
  //  @Override
  //  protected void addValueRequirements(final FunctionCompilationContext context, final Portfolio portfolio, final ViewCalculationConfiguration calcConfig) {
  //    final RiskFactorsGatherer riskFactors = OpenGammaCompilationContext.getRiskFactorsGatherer(context);
  //    calcConfig.addSpecificRequirements(riskFactors.getPositionRiskFactors(portfolio));
  //  }
  //
  //  @Override
  //  protected void addValueRequirements(final FunctionCompilationContext context, final Position position, final ViewCalculationConfiguration calcConfig) {
  //    final RiskFactorsGatherer riskFactors = OpenGammaCompilationContext.getRiskFactorsGatherer(context);
  //    calcConfig.addSpecificRequirements(riskFactors.getPositionRiskFactors(position));
  //  }
  //
  //  // FunctionInvoker
  //
  //  @SuppressWarnings("unchecked")
  //  @Override
  //  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
  //      final Set<ValueRequirement> desiredValues) {
  //    final HistoricalViewEvaluationResult riskFactors = (HistoricalViewEvaluationResult) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
  //    // Grab the value specifications as a list so that we iterate over them in a consistent order to construct the matrix
  //    final ValueRequirement[] riskFactorReqs = riskFactors.getValueRequirements().toArray(new ValueRequirement[riskFactors.getValueRequirements().size()]);
  //    @SuppressWarnings("rawtypes")
  //    final DoubleTimeSeries[] timeSeries = new DoubleTimeSeries[riskFactorReqs.length];
  //    for (int i = 0; i < riskFactorReqs.length; i++) {
  //      timeSeries[i] = riskFactors.getDoubleTimeSeries(riskFactorReqs[i]);
  //    }
  //    final ValueRequirement desiredValueReq = desiredValues.iterator().next();
  //    final ValueSpecification desiredValueSpec = new ValueSpecification(ValueRequirementNames.COVARIANCE_MATRIX, target.toSpecification(),
  //        desiredValueReq.getConstraints());
  //    return Collections.singleton(new ComputedValue(desiredValueSpec, createCovarianceMatrix(timeSeries, riskFactorReqs)));
  //  }
  //
}
