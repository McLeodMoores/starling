/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json;

import java.util.Map;

import org.threeten.bp.LocalTime;

import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Custom JSON builder to convert a {@link SwapIndexConvention} to JSON and back again.
 */
public final class SwapIndexConventionJsonBuilder extends ConventionJsonBuilder<SwapIndexConvention> {
  /**
   * Static instance.
   */
  public static final SwapIndexConventionJsonBuilder INSTANCE = new SwapIndexConventionJsonBuilder();

  @Override
  SwapIndexConvention fromJson(final String json, final Map<String, String> attributes) {
    final SwapIndexConvention convention = fromJSON(SwapIndexConvention.class, json);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  SwapIndexConvention getCopy(final SwapIndexConvention convention) {
    return convention.clone();
  }

  @Override
  public String getTemplate() {
    return toJSON(new SwapIndexConvention("", ExternalIdBundle.EMPTY, LocalTime.of(11, 0), ExternalId.of(" ", " ")));
  }

  private SwapIndexConventionJsonBuilder() {
  }
}
