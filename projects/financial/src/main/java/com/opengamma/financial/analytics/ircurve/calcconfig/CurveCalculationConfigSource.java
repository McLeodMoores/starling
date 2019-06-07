/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import com.opengamma.id.VersionCorrection;

/**
 * @deprecated {@link MultiCurveCalculationConfig} is deprecated.
 */
@Deprecated
public interface CurveCalculationConfigSource {

  MultiCurveCalculationConfig getConfig(String name);

  MultiCurveCalculationConfig getConfig(String name, VersionCorrection versionCorrection);

}
