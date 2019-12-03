/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import com.opengamma.id.VersionCorrection;

/**
 *
 */
public interface FXForwardCurveSpecificationSource {

  FXForwardCurveSpecification getSpecification(String name, String currencyPair);

  FXForwardCurveSpecification getSpecification(String name, String currencyPair, VersionCorrection versionCorrection);
}
