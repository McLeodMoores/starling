/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.json;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Custom JSON builder to convert an {@link OvernightIndexConvention} to JSON and back again.
 */
public final class OvernightIndexConventionJsonBuilder extends AbstractJSONBuilder<OvernightIndexConvention> {
  /**
   * Static instance.
   */
  public static final OvernightIndexConventionJsonBuilder INSTANCE = new OvernightIndexConventionJsonBuilder();

  @Override
  public OvernightIndexConvention fromJSON(final String json) {
    return fromJSON(OvernightIndexConvention.class, ArgumentChecker.notNull(json, "json"));
  }

  @Override
  public String toJSON(final OvernightIndexConvention object) {
    return fudgeToJson(ArgumentChecker.notNull(object, "object"));
  }

  @Override
  public String getTemplate() {
    return OvernightIndexConventionJsonBuilder.INSTANCE.toJSON(getDummyOvernightIndexConvention());
  }

  private static OvernightIndexConvention getDummyOvernightIndexConvention() {
    return new OvernightIndexConvention("O/N", ExternalIdBundle.EMPTY, DayCounts.ACT_360, 1, Currency.USD, ExternalSchemes.financialRegionId("US"));
  }

  private OvernightIndexConventionJsonBuilder() {
  }

}
