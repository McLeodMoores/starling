/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import org.threeten.bp.LocalTime;

import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Custom JSON builder to convert a {@link SwapIndexConvention} to JSON and back again.
 */
public final class SwapIndexConventionJsonBuilder extends ConventionJsonBuilder<SwapIndexConvention> {
  private static final String SWAP_CONVENTION = "swapConvention";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public SwapIndexConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  SwapIndexConvention fromJson(final String json, final Map<String, String> attributes) {
    final String toParse = replaceUnderlyingConventionName(json, SWAP_CONVENTION, SwapConvention.TYPE, _conventionMaster);
    final SwapIndexConvention convention = fromJSON(SwapIndexConvention.class, toParse);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  SwapIndexConvention getCopy(final SwapIndexConvention convention) {
    return convention.clone();
  }

  @Override
  public String toJSON(final SwapIndexConvention convention) {
    return toJSONWithUnderlyingConvention(convention, convention.getSwapConvention(), _conventionMaster);
  }

  @Override
  public String getTemplate() {
    return toJSON(new SwapIndexConvention("", ExternalIdBundle.EMPTY, LocalTime.of(11, 0), EMPTY_EID));
  }

}
