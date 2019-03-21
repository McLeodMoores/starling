/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Custom JSON builder to convert a {@link InterestRateFutureConvention} to JSON and back again.
 */
public final class InterestRateFutureConventionJsonBuilder extends ConventionJsonBuilder<InterestRateFutureConvention> {
  private static final String INDEX_CONVENTION = "indexConvention";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public InterestRateFutureConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  InterestRateFutureConvention fromJson(final String json, final Map<String, String> attributes) {
    final String toParse = replaceUnderlyingConventionName(json, INDEX_CONVENTION, IborIndexConvention.TYPE, _conventionMaster);
    final InterestRateFutureConvention convention = fromJSON(InterestRateFutureConvention.class, toParse);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  InterestRateFutureConvention getCopy(final InterestRateFutureConvention convention) {
    return convention.clone();
  }

  @Override
  public String toJSON(final InterestRateFutureConvention convention) {
    return toJSONWithUnderlyingConvention(convention, convention.getIndexConvention(), _conventionMaster);
  }

  @Override
  public String getTemplate() {
    return toJSON(new InterestRateFutureConvention("", ExternalIdBundle.EMPTY, EMPTY_EID, EMPTY_EID, EMPTY_EID));
  }

}
