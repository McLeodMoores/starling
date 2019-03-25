/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.VanillaIborLegRollDateConvention;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Custom JSON builder to convert a {@link VanillaIborLegRollDateConvention} to JSON and back again.
 */
public class VanillaIborLegRollDateConventionJsonBuilder extends ConventionJsonBuilder<VanillaIborLegRollDateConvention> {
  private static final String IBOR_INDEX_CONVENTION = "iborIndexConvention";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public VanillaIborLegRollDateConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  VanillaIborLegRollDateConvention fromJson(final String json, final Map<String, String> attributes) {
    final String toParse = replaceUnderlyingConventionName(json, IBOR_INDEX_CONVENTION, IborIndexConvention.TYPE, _conventionMaster);
    final VanillaIborLegRollDateConvention convention = fromJSON(VanillaIborLegRollDateConvention.class, toParse);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  VanillaIborLegRollDateConvention getCopy(final VanillaIborLegRollDateConvention convention) {
    return convention.clone();
  }

  @Override
  public String toJSON(final VanillaIborLegRollDateConvention convention) {
    return toJSONWithUnderlyingConvention(convention, convention.getIborIndexConvention(), _conventionMaster);
  }

  @Override
  public String getTemplate() {
    return toJSON(new VanillaIborLegRollDateConvention("", ExternalIdBundle.EMPTY, EMPTY_EID, false, Tenor.ofMonths(3), StubType.NONE, false, 0));
  }

}
