/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import org.threeten.bp.LocalTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

/**
 * Custom JSON builder to convert an {@link IborIndexConvention} to JSON and back again.
 */
public final class IborIndexConventionJsonBuilder extends ConventionJsonBuilder<IborIndexConvention> {
  /**
   * Static instance.
   */
  public static final IborIndexConventionJsonBuilder INSTANCE = new IborIndexConventionJsonBuilder();

  @Override
  IborIndexConvention fromJson(final String json, final Map<String, String> attributes) {
    final IborIndexConvention convention = fromJSON(IborIndexConvention.class, json);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  IborIndexConvention getCopy(final IborIndexConvention convention) {
    return convention.clone();
  }

  @Override
  public String getTemplate() {
    return toJSON(new IborIndexConvention("", ExternalIdBundle.EMPTY, DayCounts.ACT_360,
        BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "",
        ExternalSchemes.financialRegionId("US"), ExternalSchemes.financialRegionId("US"),
        ""));
  }

}
