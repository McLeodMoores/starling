/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithExternalIdVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Delegate class for curve node visitors.
 *
 * @param <T>
 *          The return type of the visitor.
 */
public class CurveNodeWithExternalIdVisitorDelegate<T> implements CurveNodeWithExternalIdVisitor<T> {

  /** The delegate */
  private final CurveNodeWithExternalIdVisitor<T> _delegate;

  /**
   * @param delegate
   *          The delegate, not null
   */
  public CurveNodeWithExternalIdVisitorDelegate(final CurveNodeWithExternalIdVisitor<T> delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public T visitCashNode(final CashNode node, final ExternalId externalId) {
    return _delegate.visitCashNode(node, externalId);
  }

  @Override
  public T visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node,
      final ExternalId externalId) {
    return _delegate.visitContinuouslyCompoundedRateNode(node, externalId);
  }

  @Override
  public T visitCreditSpreadNode(final CreditSpreadNode node, final ExternalId externalId) {
    return _delegate.visitCreditSpreadNode(node, externalId);
  }

  @Override
  public T visitDeliverableSwapFutureNode(final DeliverableSwapFutureNode node, final ExternalId externalId) {
    return _delegate.visitDeliverableSwapFutureNode(node, externalId);
  }

  @Override
  public T visitDiscountFactorNode(final DiscountFactorNode node, final ExternalId externalId) {
    return _delegate.visitDiscountFactorNode(node, externalId);
  }

  @Override
  public T visitFRANode(final FRANode node, final ExternalId externalId) {
    return _delegate.visitFRANode(node, externalId);
  }

  @Override
  public T visitFXForwardNode(final FXForwardNode node, final ExternalId externalId) {
    return _delegate.visitFXForwardNode(node, externalId);
  }

  @Override
  public T visitRateFutureNode(final RateFutureNode node, final ExternalId externalId) {
    return _delegate.visitRateFutureNode(node, externalId);
  }

  @Override
  public T visitSwapNode(final SwapNode node, final ExternalId externalId) {
    return _delegate.visitSwapNode(node, externalId);
  }

  @Override
  public T visitZeroCouponInflationNode(final ZeroCouponInflationNode node, final ExternalId externalId) {
    return _delegate.visitZeroCouponInflationNode(node, externalId);
  }
}
