/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

/**
 * @deprecated {@link FixedIncomeStrip}s are deprecated.
 */
@Deprecated
public enum IndexType {
  /** Libor */
  Libor,
  /** Tibor */
  Tibor,
  /** Euribor */
  Euribor,
  /** BBSW (AUD) */
  BBSW,
  /** Swap */
  Swap
}
