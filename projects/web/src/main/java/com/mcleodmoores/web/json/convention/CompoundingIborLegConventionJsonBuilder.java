/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Custom JSON builder to convert a {@link CompoundingIborLegConvention} to JSON and back again.
 */
public class CompoundingIborLegConventionJsonBuilder extends ConventionJsonBuilder<CompoundingIborLegConvention> {
  private static final String IBOR_INDEX_CONVENTION = "iborIndexConvention";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public CompoundingIborLegConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  public String getTemplate() {
    return toJSON(new CompoundingIborLegConvention("", ExternalIdBundle.EMPTY, EMPTY_EID, Tenor.THREE_MONTHS, CompoundingType.FLAT_COMPOUNDING, Tenor.ONE_MONTH,
        StubType.NONE, 0, false, StubType.NONE, false, 0));
  }

  @Override
  CompoundingIborLegConvention fromJson(final String json, final Map<String, String> attributes) {
    final String toParse = replaceUnderlyingConventionName(json, IBOR_INDEX_CONVENTION, IborIndexConvention.TYPE, _conventionMaster);
    final CompoundingIborLegConvention convention = fromJSON(CompoundingIborLegConvention.class, toParse);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  CompoundingIborLegConvention getCopy(final CompoundingIborLegConvention convention) {
    return convention.clone();
  }

  @Override
  public String toJSON(final CompoundingIborLegConvention convention) {
    return toJSONWithUnderlyingConvention(convention, convention.getIborIndexConvention(), _conventionMaster);
  }
}
