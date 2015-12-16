/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.ircurve.strips;

/**
 *
 */
public interface StarlingVisitableCurveNode extends VisitableCurveNode {

  <T> T accept(StarlingCurveNodeVisitor<T> visitor);
}
