/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import com.opengamma.engine.target.ComputationTargetType;

/**
 * 
 */
public class CAPMBetaModelSecurityFunction extends CAPMBetaModelFunction {

  public CAPMBetaModelSecurityFunction(final String resolutionKey) {
    super(ComputationTargetType.SECURITY, resolutionKey);
  }

}
