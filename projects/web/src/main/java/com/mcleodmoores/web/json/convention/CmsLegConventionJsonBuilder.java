/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Custom JSON builder to convert a {@link CMSLegConvention} to JSON and back again.
 */
public class CmsLegConventionJsonBuilder extends ConventionJsonBuilder<CMSLegConvention> {
  private static final String SWAP_INDEX_CONVENTION = "swapIndexConvention";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public CmsLegConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  public String getTemplate() {
    return toJSON(new CMSLegConvention("", ExternalIdBundle.EMPTY, EMPTY_EID, Tenor.THREE_MONTHS, false));
  }

  @Override
  CMSLegConvention fromJson(final String json, final Map<String, String> attributes) {
    final String toParse = replaceUnderlyingConventionName(json, SWAP_INDEX_CONVENTION, SwapIndexConvention.TYPE, _conventionMaster);
    final CMSLegConvention convention = fromJSON(CMSLegConvention.class, toParse);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  CMSLegConvention getCopy(final CMSLegConvention convention) {
    return convention.clone();
  }

  @Override
  public String toJSON(final CMSLegConvention convention) {
    return toJSONWithUnderlyingConvention(convention, convention.getSwapIndexConvention(), _conventionMaster);
  }
}
