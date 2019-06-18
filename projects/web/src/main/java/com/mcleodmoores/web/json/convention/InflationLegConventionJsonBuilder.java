/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Custom JSON builder to convert a {@link InflationLegConvention} to JSON and back again.
 */
public final class InflationLegConventionJsonBuilder extends ConventionJsonBuilder<InflationLegConvention> {
  private static final String PRICE_INDEX_CONVENTION = "priceIndexConvention";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public InflationLegConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  InflationLegConvention fromJson(final String json, final Map<String, String> attributes) {
    final String toParse = replaceUnderlyingConventionName(json, PRICE_INDEX_CONVENTION, PriceIndexConvention.TYPE, _conventionMaster);
    final InflationLegConvention convention = fromJSON(InflationLegConvention.class, toParse);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  InflationLegConvention getCopy(final InflationLegConvention convention) {
    return convention.clone();
  }

  @Override
  public String toJSON(final InflationLegConvention convention) {
    return toJSONWithUnderlyingConvention(convention, convention.getPriceIndexConvention(), _conventionMaster);
  }

  @Override
  public String getTemplate() {
    return toJSON(new InflationLegConvention("", ExternalIdBundle.EMPTY, BusinessDayConventions.FOLLOWING, DayCounts.ACT_360, false, 0, 0, EMPTY_EID));
  }

}
