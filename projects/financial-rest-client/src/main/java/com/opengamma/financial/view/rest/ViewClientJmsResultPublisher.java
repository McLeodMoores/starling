/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ClientShutdownCall;
import com.opengamma.engine.view.listener.CycleCompletedCall;
import com.opengamma.engine.view.listener.CycleExecutionFailedCall;
import com.opengamma.engine.view.listener.CycleFragmentCompletedCall;
import com.opengamma.engine.view.listener.CycleStartedCall;
import com.opengamma.engine.view.listener.ProcessCompletedCall;
import com.opengamma.engine.view.listener.ProcessTerminatedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompilationFailedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompiledCall;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.financial.rest.AbstractJmsResultPublisher;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.jms.JmsConnector;

/**
 * Publishes {@code ViewClient} results over JMS.
 */
public class ViewClientJmsResultPublisher extends AbstractJmsResultPublisher implements ViewResultListener  {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewClientJmsResultPublisher.class);

  /**
   * The view client.
   */
  private final ViewClient _viewClient;

  /**
   * Creates an instance.
   *
   * @param viewClient  the view client, not null
   * @param fudgeContext  the Fudge context, not null
   * @param jmsConnector  the JMS connector, not null
   */
  public ViewClientJmsResultPublisher(final ViewClient viewClient, final FudgeContext fudgeContext, final JmsConnector jmsConnector) {
    super(fudgeContext, jmsConnector);
    _viewClient = viewClient;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void startListener() {
    LOGGER.debug("Setting listener {} on view client {}'s results", this, _viewClient);
    _viewClient.setResultListener(this);
  }

  @Override
  protected void stopListener() {
    LOGGER.debug("Removing listener {} on view client {}'s results", this, _viewClient);
    _viewClient.setResultListener(null);
  }

  //-------------------------------------------------------------------------
  @Override
  public UserPrincipal getUser() {
    return _viewClient.getUser();
  }

  @Override
  public void viewDefinitionCompiled(final CompiledViewDefinition compiledViewDefinition, final boolean hasMarketDataPermissions) {
    send(new ViewDefinitionCompiledCall(compiledViewDefinition, hasMarketDataPermissions));
  }

  @Override
  public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
    send(new ViewDefinitionCompilationFailedCall(valuationTime, exception));
  }

  @Override
  public void cycleStarted(final ViewCycleMetadata cycleMetadata) {
    send(new CycleStartedCall(cycleMetadata));
  }

  @Override
  public void cycleFragmentCompleted(final ViewComputationResultModel fullFragment, final ViewDeltaResultModel deltaFragment) {
    send(new CycleFragmentCompletedCall(fullFragment, deltaFragment));
  }

  @Override
  public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
    send(new CycleCompletedCall(fullResult, deltaResult));
  }

  @Override
  public void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
    send(new CycleExecutionFailedCall(executionOptions, exception));
  }

  @Override
  public void processCompleted() {
    send(new ProcessCompletedCall());
  }

  @Override
  public void processTerminated(final boolean executionInterrupted) {
    send(new ProcessTerminatedCall(executionInterrupted));
  }

  @Override
  public void clientShutdown(final Exception e) {
    send(new ClientShutdownCall(e));
  }

}
