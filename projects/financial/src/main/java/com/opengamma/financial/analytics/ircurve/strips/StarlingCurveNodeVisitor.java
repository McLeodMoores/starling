/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.ircurve.strips;

/**
 *
 */
public interface StarlingCurveNodeVisitor<T> extends CurveNodeVisitor<T> {

  T visitFxSpotNode(FxSpotNode node);
}
