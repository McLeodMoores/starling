/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Custom JSON builder to convert a {@link SwapFixedLegConvention} to JSON and back again.
 */
public final class SwapFixedLegConventionJsonBuilder extends ConventionJsonBuilder<SwapFixedLegConvention> {
  /**
   * Static instance.
   */
  public static final SwapFixedLegConventionJsonBuilder INSTANCE = new SwapFixedLegConventionJsonBuilder();

  @Override
  SwapFixedLegConvention fromJson(final String json, final Map<String, String> attributes) {
    final SwapFixedLegConvention convention = fromJSON(SwapFixedLegConvention.class, json);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  SwapFixedLegConvention getCopy(final SwapFixedLegConvention convention) {
    return convention.clone();
  }

  @Override
  public String getTemplate() {
    return toJSON(new SwapFixedLegConvention("", ExternalIdBundle.EMPTY, Tenor.THREE_MONTHS, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, Currency.USD,
        ExternalSchemes.financialRegionId("US"), 0, false, StubType.NONE, false, 0));
  }

  private SwapFixedLegConventionJsonBuilder() {
  }
}
