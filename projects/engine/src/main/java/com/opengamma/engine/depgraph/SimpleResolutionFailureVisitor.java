/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public class SimpleResolutionFailureVisitor extends ResolutionFailureVisitor<List<ResolutionFailure>> {

  @Override
  protected List<ResolutionFailure> visitCouldNotResolve(final ValueRequirement valueRequirement) {
    return Collections.singletonList(ResolutionFailureImpl.couldNotResolve(valueRequirement));
  }

  @Override
  protected List<ResolutionFailure> visitNoFunctions(final ValueRequirement valueRequirement) {
    return Collections.singletonList(ResolutionFailureImpl.noFunctions(valueRequirement));
  }

  @Override
  protected List<ResolutionFailure> visitRecursiveRequirement(final ValueRequirement valueRequirement) {
    return Collections.singletonList(ResolutionFailureImpl.recursiveRequirement(valueRequirement));
  }

  @Override
  protected List<ResolutionFailure> visitUnsatisfied(final ValueRequirement valueRequirement) {
    return Collections.singletonList(ResolutionFailureImpl.unsatisfied(valueRequirement));
  }

  @Override
  protected List<ResolutionFailure> visitMarketDataMissing(final ValueRequirement valueRequirement) {
    return Collections.singletonList(ResolutionFailureImpl.marketDataMissing(valueRequirement));
  }

  @Override
  protected List<ResolutionFailure> visitSuccessfulFunction(final ValueRequirement valueRequirement, final String function,
      final ValueSpecification desiredOutput, final Map<ValueSpecification, ValueRequirement> satisfied) {
    return Collections.emptyList(); //TODO is this correct?
  }

  @Override
  //TODO not on fudge builder visitor?
  protected List<ResolutionFailure> visitFailedFunction(final ValueRequirement valueRequirement, final String function,
      final ValueSpecification desiredOutput, final Map<ValueSpecification, ValueRequirement> satisfied,
      final Set<ResolutionFailure> unsatisfied) {
    return ImmutableList.copyOf(unsatisfied);
  }

  @Override
  protected List<ResolutionFailure> visitFailedFunction(final ValueRequirement valueRequirement, final String function,
      final ValueSpecification desiredOutput, final Map<ValueSpecification, ValueRequirement> satisfied,
      final Set<ResolutionFailure> unsatisfied, final Set<ResolutionFailure> unsatisfiedAdditional) {
    return ImmutableList.copyOf(Iterables.concat(unsatisfied, unsatisfiedAdditional));
  }

  @Override
  protected List<ResolutionFailure> visitFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied,
      final Set<ResolutionFailure> unsatisfiedAdditional) {
    return ImmutableList.copyOf(Iterables.concat(unsatisfied, unsatisfiedAdditional));
  }

  @Override
  protected List<ResolutionFailure> visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final String function,
      final ValueSpecification desiredOutput, final Map<ValueSpecification, ValueRequirement> requirements) {
    return Collections.emptyList(); //TODO is this correct?
  }

  @Override
  protected List<ResolutionFailure> visitGetResultsFailed(final ValueRequirement valueRequirement, final String function,
      final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return Collections.emptyList(); //TODO is this correct?
  }

  @Override
  protected List<ResolutionFailure> visitGetRequirementsFailed(final ValueRequirement valueRequirement, final String function,
      final ValueSpecification desiredOutput) {
    return Collections.emptyList(); //TODO is this correct?
  }

  @Override
  protected List<ResolutionFailure> visitLateResolutionFailure(final ValueRequirement valueRequirement, final String function,
      final ValueSpecification desiredOutput, final Map<ValueSpecification, ValueRequirement> requirements) {
    return Collections.emptyList(); //TODO is this correct?
  }

  @Override
  protected List<ResolutionFailure> visitBlacklistSuppressed(final ValueRequirement valueRequirement, final String function,
      final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return Collections.emptyList(); //TODO is this correct?
  }

}
