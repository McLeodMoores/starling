/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.id.ExternalIdBundle;

/**
 * Custom JSON builder to convert a {@link BondConvention} to JSON and back again.
 */
public final class BondConventionJsonBuilder extends ConventionJsonBuilder<BondConvention> {
  /**
   * Static instance.
   */
  public static final BondConventionJsonBuilder INSTANCE = new BondConventionJsonBuilder();

  @Override
  BondConvention fromJson(final String json, final Map<String, String> attributes) {
    final BondConvention convention = fromJSON(BondConvention.class, json);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  BondConvention getCopy(final BondConvention convention) {
    return convention.clone();
  }

  @Override
  public String getTemplate() {
    return toJSON(new BondConvention("", ExternalIdBundle.EMPTY, 0, 0, BusinessDayConventions.NONE, false, false));
  }

  private BondConventionJsonBuilder() {
  }
}
