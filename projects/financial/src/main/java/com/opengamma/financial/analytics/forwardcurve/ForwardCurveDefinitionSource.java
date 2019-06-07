/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import com.opengamma.id.VersionCorrection;

/**
 *
 */
public interface ForwardCurveDefinitionSource {

  ForwardCurveDefinition getDefinition(String name, String uniqueIdName);

  ForwardCurveDefinition getDefinition(String name, String uniqueIdName, VersionCorrection versionCorrection);
}
