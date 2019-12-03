/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

/**
 * Description of how a date has been generated relative to a period.
 *
 */
public enum DateRelativeTo {
  /** Date is relative to start of period. */
  START,
  /** Date is relative to end of period. */
  END
}