/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

/**
 * Custom JSON builder to convert an {@link OvernightIndexConvention} to JSON and back again.
 */
public final class OvernightIndexConventionJsonBuilder extends ConventionJsonBuilder<OvernightIndexConvention> {
  /**
   * Static instance.
   */
  public static final OvernightIndexConventionJsonBuilder INSTANCE = new OvernightIndexConventionJsonBuilder();

  @Override
  OvernightIndexConvention fromJson(final String json, final Map<String, String> attributes) {
    final OvernightIndexConvention convention = fromJSON(OvernightIndexConvention.class, json);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  OvernightIndexConvention getCopy(final OvernightIndexConvention convention) {
    return convention.clone();
  }

  @Override
  public String getTemplate() {
    return toJSON(new OvernightIndexConvention("", ExternalIdBundle.EMPTY, DayCounts.ACT_360, 1, Currency.USD,
        ExternalSchemes.financialRegionId("US")));
  }

  private OvernightIndexConventionJsonBuilder() {
  }

}
