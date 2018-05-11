/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json;

import java.util.Map;

import com.opengamma.financial.convention.FXSpotConvention;

/**
 * Custom JSON builder to convert a {@link FXSpotConvention} to JSON and back again.
 */
public class FxSpotConventionJsonBuilder extends ConventionJsonBuilder<FXSpotConvention> {

  /* (non-Javadoc)
   * @see com.opengamma.web.json.JSONBuilder#getTemplate()
   */
  @Override
  public String getTemplate() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.mcleodmoores.web.json.ConventionJsonBuilder#fromJson(java.lang.String, java.util.Map)
   */
  @Override
  FXSpotConvention fromJson(final String json, final Map<String, String> attributes) {
    return null;
  }

  /* (non-Javadoc)
   * @see com.mcleodmoores.web.json.ConventionJsonBuilder#getCopy(com.opengamma.core.convention.Convention)
   */
  @Override
  FXSpotConvention getCopy(final FXSpotConvention convention) {
    return null;
  }

}
