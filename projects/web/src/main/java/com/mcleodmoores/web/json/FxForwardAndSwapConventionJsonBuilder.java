/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json;

import java.util.Map;

import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Custom JSON builder to convert a {@link FXForwardAndSwapConvention} to JSON and back again.
 */
public final class FxForwardAndSwapConventionJsonBuilder extends ConventionJsonBuilder<FXForwardAndSwapConvention> {
  /**
   * Static instance.
   */
  public static final FxForwardAndSwapConventionJsonBuilder INSTANCE = new FxForwardAndSwapConventionJsonBuilder();

  @Override
  FXForwardAndSwapConvention fromJson(final String json, final Map<String, String> attributes) {
    final FXForwardAndSwapConvention convention = fromJSON(FXForwardAndSwapConvention.class, json);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  FXForwardAndSwapConvention getCopy(final FXForwardAndSwapConvention convention) {
    return convention.clone();
  }

  @Override
  public String getTemplate() {
    return toJSON(new FXForwardAndSwapConvention("", ExternalIdBundle.EMPTY, ExternalId.of(" ", " "), BusinessDayConventions.FOLLOWING, false));
  }

  private FxForwardAndSwapConventionJsonBuilder() {
  }
}
