/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio.fpml5_8;

/**
 * Describes the quote basis for a FX rate: currency1/currency2 or currency2/currency1.
 */
public enum QuoteBasis {

  /**
   * Quotes are in currency1 per currency2 units.
   */
  CURRENCY2_PER_CURRENCY1,

  /**
   * Quotes are in currency2 per currency1 units.
   */
  CURRENCY1_PER_CURRENCY2
}