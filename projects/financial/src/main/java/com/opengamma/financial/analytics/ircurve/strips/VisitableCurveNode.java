/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.ircurve.strips;

/**
 *
 */
public interface VisitableCurveNode {

  <T> T accept(CurveNodeVisitor<T> visitor);
}
