/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Map;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.util.test.Profiler;

/**
 * Debugging/profiling utilities for identifying bottlenecks in dependency graph function resolution.
 */
public final class DebugUtils {

  private static final Profiler CAN_APPLY_TO = Profiler.create(DebugUtils.class, "canApplyTo");
  private static final Profiler GET_RESULTS_1 = Profiler.create(DebugUtils.class, "getResults1");
  private static final Profiler GET_REQUIREMENTS = Profiler.create(DebugUtils.class, "getRequirements");
  private static final Profiler GET_RESULTS_2 = Profiler.create(DebugUtils.class, "getResults2");
  private static final Profiler GET_ADDITIONAL_REQUIREMENTS = Profiler.create(DebugUtils.class, "getAdditionalRequirements");

  private DebugUtils() {
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#canApplyTo}.
   */
  public static void canApplyTo_enter() { //CSIGNORE
    CAN_APPLY_TO.begin();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#canApplyTo}.
   */
  public static void canApplyTo_leave() { //CSIGNORE
    CAN_APPLY_TO.end();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#getResults(FunctionCompilationContext,ComputationTarget)}.
   */
  public static void getResults1_enter() { //CSIGNORE
    GET_RESULTS_1.begin();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#getResults(FunctionCompilationContext,ComputationTarget)}.
   */
  public static void getResults1_leave() { //CSIGNORE
    GET_RESULTS_1.end();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#getRequirements}.
   */
  public static void getRequirements_enter() { //CSIGNORE
    GET_REQUIREMENTS.begin();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#getRequirements}.
   */
  public static void getRequirements_leave() { //CSIGNORE
    GET_REQUIREMENTS.end();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#getResults(FunctionCompilationContext,ComputationTarget,Map)}.
   */
  public static void getResults2_enter() { //CSIGNORE
    GET_RESULTS_2.begin();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#getResults(FunctionCompilationContext,ComputationTarget,Map)}.
   */
  public static void getResults2_leave() { //CSIGNORE
    GET_RESULTS_2.end();
  }

  /**
   * Records entry to {@link CompiledFunctionDefinition#getAdditionalRequirements}.
   */
  public static void getAdditionalRequirements_enter() { //CSIGNORE
    GET_ADDITIONAL_REQUIREMENTS.begin();
  }

  /**
   * Records exit from {@link CompiledFunctionDefinition#getAdditionalRequirements}.
   */
  public static void getAdditionalRequirements_leave() { //CSIGNORE
    GET_ADDITIONAL_REQUIREMENTS.end();
  }

}
