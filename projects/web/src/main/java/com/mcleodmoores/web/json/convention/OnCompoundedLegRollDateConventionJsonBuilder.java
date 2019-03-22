/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.financial.convention.ONCompoundedLegRollDateConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Custom JSON builder to convert a {@link ONCompoundedLegRollDateConvention} to JSON and back again.
 */
public class OnCompoundedLegRollDateConventionJsonBuilder extends ConventionJsonBuilder<ONCompoundedLegRollDateConvention> {
  private static final String OVERNIGHT_INDEX_CONVENTION = "overnightIndexConvention";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public OnCompoundedLegRollDateConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  ONCompoundedLegRollDateConvention fromJson(final String json, final Map<String, String> attributes) {
    final String toParse = replaceUnderlyingConventionName(json, OVERNIGHT_INDEX_CONVENTION, OvernightIndexConvention.TYPE, _conventionMaster);
    final ONCompoundedLegRollDateConvention convention = fromJSON(ONCompoundedLegRollDateConvention.class, toParse);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  ONCompoundedLegRollDateConvention getCopy(final ONCompoundedLegRollDateConvention convention) {
    return convention.clone();
  }

  @Override
  public String toJSON(final ONCompoundedLegRollDateConvention convention) {
    return toJSONWithUnderlyingConvention(convention, convention.getOvernightIndexConvention(), _conventionMaster);
  }

  @Override
  public String getTemplate() {
    return toJSON(new ONCompoundedLegRollDateConvention("", ExternalIdBundle.EMPTY, EMPTY_EID, Tenor.THREE_MONTHS, StubType.NONE, false, 0));
  }

}
