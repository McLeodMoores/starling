/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Custom JSON builder to convert a {@link FXForwardAndSwapConvention} to JSON and back again.
 */
public final class FxForwardAndSwapConventionJsonBuilder extends ConventionJsonBuilder<FXForwardAndSwapConvention> {
  private static final String FX_SPOT_CONVENTION = "spotConvention";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public FxForwardAndSwapConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  FXForwardAndSwapConvention fromJson(final String json, final Map<String, String> attributes) {
    final String toParse = replaceUnderlyingConventionName(json, FX_SPOT_CONVENTION, FXForwardAndSwapConvention.TYPE, _conventionMaster);
    final FXForwardAndSwapConvention convention = fromJSON(FXForwardAndSwapConvention.class, toParse);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  FXForwardAndSwapConvention getCopy(final FXForwardAndSwapConvention convention) {
    return convention.clone();
  }

  @Override
  public String toJSON(final FXForwardAndSwapConvention convention) {
    return toJSONWithUnderlyingConvention(convention, convention.getSpotConvention(), _conventionMaster);
  }

  @Override
  public String getTemplate() {
    return toJSON(new FXForwardAndSwapConvention("", ExternalIdBundle.EMPTY, EMPTY_EID, BusinessDayConventions.FOLLOWING, false));
  }

}