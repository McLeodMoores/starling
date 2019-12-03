/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.FixedLegRollDateConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Custom JSON builder to convert a {@link FixedLegRollDateConvention} to JSON and back again.
 */
public final class FixedLegRollDateConventionJsonBuilder extends ConventionJsonBuilder<FixedLegRollDateConvention> {
  /**
   * Static instance.
   */
  public static final FixedLegRollDateConventionJsonBuilder INSTANCE = new FixedLegRollDateConventionJsonBuilder();

  @Override
  FixedLegRollDateConvention fromJson(final String json, final Map<String, String> attributes) {
    final FixedLegRollDateConvention convention = fromJSON(FixedLegRollDateConvention.class, json);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  FixedLegRollDateConvention getCopy(final FixedLegRollDateConvention convention) {
    return convention.clone();
  }

  @Override
  public String getTemplate() {
    return toJSON(new FixedLegRollDateConvention("", ExternalIdBundle.EMPTY, Tenor.THREE_MONTHS, DayCounts.ACT_360, Currency.USD,
        ExternalSchemes.financialRegionId("US"), StubType.NONE, false, 0));
  }

  private FixedLegRollDateConventionJsonBuilder() {
  }
}
