/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.financial.convention.ONArithmeticAverageLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Custom JSON builder to convert a {@link ONArithmeticAverageLegConvention} to JSON and back again.
 */
public class OnArithmeticAverageLegConventionJsonBuilder extends ConventionJsonBuilder<ONArithmeticAverageLegConvention> {
  private static final String OVERNIGHT_INDEX_CONVENTION = "overnightIndexConvention";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public OnArithmeticAverageLegConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  ONArithmeticAverageLegConvention fromJson(final String json, final Map<String, String> attributes) {
    final String toParse = replaceUnderlyingConventionName(json, OVERNIGHT_INDEX_CONVENTION, OvernightIndexConvention.TYPE, _conventionMaster);
    final ONArithmeticAverageLegConvention convention = fromJSON(ONArithmeticAverageLegConvention.class, toParse);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  ONArithmeticAverageLegConvention getCopy(final ONArithmeticAverageLegConvention convention) {
    return convention.clone();
  }

  @Override
  public String toJSON(final ONArithmeticAverageLegConvention convention) {
    return toJSONWithUnderlyingConvention(convention, convention.getOvernightIndexConvention(), _conventionMaster);
  }

  @Override
  public String getTemplate() {
    return toJSON(new ONArithmeticAverageLegConvention("", ExternalIdBundle.EMPTY, EMPTY_EID, Tenor.THREE_MONTHS, BusinessDayConventions.FOLLOWING, 0, false,
        StubType.NONE, false, 0));
  }

}
