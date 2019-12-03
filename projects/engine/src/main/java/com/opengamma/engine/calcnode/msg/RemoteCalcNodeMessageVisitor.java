/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.calcnode.msg;

/**
 * Visitor to {@link RemoteCalcNodeMessage} subclasses.
 */
public abstract class RemoteCalcNodeMessageVisitor {

  protected abstract void visitUnexpectedMessage(RemoteCalcNodeMessage message);

  protected void visitCancelMessage(final Cancel message) {
    visitUnexpectedMessage(message);
  }

  protected void visitExecuteMessage(final Execute message) {
    visitUnexpectedMessage(message);
  }

  protected void visitFailureMessage(final Failure message) {
    visitUnexpectedMessage(message);
  }

  protected void visitInitMessage(final Init message) {
    visitUnexpectedMessage(message);
  }

  protected void visitInvocationsMessage(final Invocations message) {
    visitUnexpectedMessage(message);
  }

  protected void visitIsAliveMessage(final IsAlive message) {
    visitUnexpectedMessage(message);
  }

  protected void visitReadyMessage(final Ready message) {
    visitUnexpectedMessage(message);
  }

  protected void visitResultMessage(final Result message) {
    visitUnexpectedMessage(message);
  }

  protected void visitScalingMessage(final Scaling message) {
    visitUnexpectedMessage(message);
  }

}
