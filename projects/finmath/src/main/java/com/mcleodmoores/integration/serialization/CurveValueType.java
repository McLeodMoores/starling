/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.serialization;


/**
 * Enum representing the possible types of y values that can be used to construct a
 * {@link net.finmath.marketdata.model.curves.AbstractCurve}.
 */
public enum CurveValueType {

  /**
   * Discount factors.
   */
  DISCOUNT_FACTORS,
  /**
   * Zero rates.
   */
  ZERO_RATES,
  /**
   * Forward rates.
   */
  FORWARD_RATES
}
