/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.trigger;

/**
 * Encapsulates a trigger's response to being queried.
 */
public class ViewCycleTriggerResult {

  private final ViewCycleType _cycleType;
  private final ViewCycleEligibility _cycleEligibility;
  private final Long _stateValidityNanos;

  public ViewCycleTriggerResult(final ViewCycleEligibility cycleEligibility) {
    this(cycleEligibility, null, null);
  }

  public ViewCycleTriggerResult(final ViewCycleType cycleType) {
    this(null, cycleType, null);
  }

  public ViewCycleTriggerResult(final long nanos) {
    this(null, null, nanos);
  }

  public ViewCycleTriggerResult(final ViewCycleEligibility cycleEligibility, final ViewCycleType cycleType) {
    this(cycleEligibility, cycleType, null);
  }

  public ViewCycleTriggerResult(final ViewCycleEligibility cycleEligibility, final ViewCycleType cycleType, final Long stateValidityNanos) {
    _cycleEligibility = cycleEligibility;
    _cycleType = cycleType;
    _stateValidityNanos = stateValidityNanos;
  }

  public static ViewCycleTriggerResult preventUntil(final long nanos) {
    return new ViewCycleTriggerResult(ViewCycleEligibility.PREVENT, null, nanos);
  }

  public static ViewCycleTriggerResult nothingUntil(final long nanos) {
    return new ViewCycleTriggerResult(nanos);
  }

  public static ViewCycleTriggerResult forceFull() {
    return new ViewCycleTriggerResult(ViewCycleEligibility.FORCE, ViewCycleType.FULL);
  }

  public ViewCycleType getCycleType() {
    return _cycleType;
  }

  public ViewCycleEligibility getCycleEligibility() {
    return _cycleEligibility;
  }

  public Long getNextStateChangeNanos() {
    return _stateValidityNanos;
  }

  @Override
  public String toString() {
    return "ViewCycleTriggerResult[cycleType=" + _cycleType + ", cycleEligibility=" + _cycleEligibility + ", stateValidityNanos=" + _stateValidityNanos + "]";
  }

}
