/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.Map;

import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Custom JSON builder to convert a {@link VanillaIborLegConvention} to JSON and back again.
 */
public class VanillaIborLegConventionJsonBuilder extends ConventionJsonBuilder<VanillaIborLegConvention> {
  private static final String IBOR_INDEX_CONVENTION = "iborIndexConvention";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public VanillaIborLegConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  VanillaIborLegConvention fromJson(final String json, final Map<String, String> attributes) {
    final String toParse = replaceUnderlyingConventionName(json, IBOR_INDEX_CONVENTION, IborIndexConvention.TYPE, _conventionMaster);
    final VanillaIborLegConvention convention = fromJSON(VanillaIborLegConvention.class, toParse);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  VanillaIborLegConvention getCopy(final VanillaIborLegConvention convention) {
    return convention.clone();
  }

  @Override
  public String toJSON(final VanillaIborLegConvention convention) {
    return toJSONWithUnderlyingConvention(convention, convention.getIborIndexConvention(), _conventionMaster);
  }

  @Override
  public String getTemplate() {
    return toJSON(new VanillaIborLegConvention("", ExternalIdBundle.EMPTY, EMPTY_EID, false, InterpolationMethod.NONE.name(), Tenor.ofMonths(3), 0, false,
        StubType.NONE, false, 0));
  }

}
