/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.id.ExternalIdBundle;

/**
 * Custom JSON builder to convert a {@link FXSpotConvention} to JSON and back again.
 */
public final class FxSpotConventionJsonBuilder extends ConventionJsonBuilder<FXSpotConvention> {
  /**
   * Static instance.
   */
  public static final FxSpotConventionJsonBuilder INSTANCE = new FxSpotConventionJsonBuilder();

  @Override
  FXSpotConvention fromJson(final String json, final Map<String, String> attributes) {
    final FXSpotConvention convention = fromJSON(FXSpotConvention.class, json);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  FXSpotConvention getCopy(final FXSpotConvention convention) {
    return convention.clone();
  }

  @Override
  public String getTemplate() {
    return toJSON(new FXSpotConvention("", ExternalIdBundle.EMPTY, 2, false));
  }

  private FxSpotConventionJsonBuilder() {
  }
}
