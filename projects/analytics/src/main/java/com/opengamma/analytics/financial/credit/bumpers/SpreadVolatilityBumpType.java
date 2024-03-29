/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.bumpers;

// CSOFF
/**
 * Enumerate the types of bumps that can be applied to the spread volatility (to compute spread volatility sensitivities in CDS Swaption contracts).
 * 
 * @deprecated this will be deleted
 */
@Deprecated
public enum SpreadVolatilityBumpType {
  /**
   * sigma -&gt; sigma + bump.
   */
  ADDITIVE,
  /**
   * sigma -&gt; sigma x (1 + bump).
   */
  MULTIPLICATIVE;

}
