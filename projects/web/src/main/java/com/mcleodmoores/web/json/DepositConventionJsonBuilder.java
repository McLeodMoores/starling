/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json;

import java.util.Map;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Custom JSON builder to convert a {@link DepositConvention} to JSON and back again.
 */
public final class DepositConventionJsonBuilder extends ConventionJsonBuilder<DepositConvention> {
  /**
   * Static instance.
   */
  public static final DepositConventionJsonBuilder INSTANCE = new DepositConventionJsonBuilder();

  @Override
  DepositConvention fromJson(final String json, final Map<String, String> attributes) {
    final DepositConvention convention = fromJSON(DepositConvention.class, json);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  DepositConvention getCopy(final DepositConvention convention) {
    return convention.clone();
  }

  @Override
  public String getTemplate() {
    return toJSON(new DepositConvention("", ExternalIdBundle.EMPTY, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING,
        0, false, Currency.USD, ExternalSchemes.countryRegionId(Country.US)));
  }

  private DepositConventionJsonBuilder() {
  }
}
