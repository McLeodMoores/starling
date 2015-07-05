/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class BlackVolatilitySurfaceMoneynessFormatter extends AbstractFormatter<BlackVolatilitySurfaceMoneyness> {

  /* package */ BlackVolatilitySurfaceMoneynessFormatter() {
    super(BlackVolatilitySurfaceMoneyness.class);
    addFormatter(new Formatter<BlackVolatilitySurfaceMoneyness>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final BlackVolatilitySurfaceMoneyness value, final ValueSpecification valueSpec, final Object inlineKey) {
        return SurfaceFormatterUtils.formatExpanded(value.getSurface());
      }
    });
  }

  @Override
  public Object formatCell(final BlackVolatilitySurfaceMoneyness value, final ValueSpecification valueSpec, final Object inlineKey) {
    return SurfaceFormatterUtils.formatCell(value.getSurface());
  }

  /**
   * Returns {@link DataType#UNKNOWN UNKNOWN} because the type can be differ for different instances depending on
   * the type returned by {@link VolatilitySurface#getSurface() getSurface()}.
   * The type for a given surface instance can be obtained from {@link #getDataTypeForValue}
   * @return {@link DataType#UNKNOWN}
   */
  @Override
  public DataType getDataType() {
    return DataType.UNKNOWN;
  }

  @Override
  public DataType getDataTypeForValue(final BlackVolatilitySurfaceMoneyness value) {
    return SurfaceFormatterUtils.getDataType(value.getSurface());
  }
}
