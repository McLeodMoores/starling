/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import com.opengamma.analytics.financial.greeks.PDEResultCollection;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public class PDEGridGreekCurveConverter implements ResultConverter<PDEResultCollection> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final PDEResultCollection value, final ConversionMode mode) {
    return null;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final PDEResultCollection value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final PDEResultCollection value) {

    valueSpec.getValueName();
    return null;
  }

  @Override
  public String getFormatterName() {
    return "PDE_GRID_GREEK";
  }

}
