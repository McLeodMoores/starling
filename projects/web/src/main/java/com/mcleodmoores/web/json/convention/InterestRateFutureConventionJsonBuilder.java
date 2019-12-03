/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.io.StringReader;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.expirycalc.IMMFutureAndFutureOptionQuarterlyExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Custom JSON builder to convert a {@link InterestRateFutureConvention} to JSON and back again.
 */
public final class InterestRateFutureConventionJsonBuilder extends ConventionJsonBuilder<InterestRateFutureConvention> {
  private static final String INDEX_CONVENTION = "indexConvention";
  private static final String EXPIRY_CONVENTION = "expiryConvention";
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
    String toParse = replaceUnderlyingConventionName(json, INDEX_CONVENTION, IborIndexConvention.TYPE, _conventionMaster);
    try {
      final StringReader sr = new StringReader(toParse);
      final JSONObject jsonObject = new JSONObject(new JSONTokener(sr));
      final JSONObject data = jsonObject.getJSONObject("data");
      final ExternalId expiryConventionId = ExternalId.of("CONVENTION", data.getString(EXPIRY_CONVENTION));
      data.remove(EXPIRY_CONVENTION);
      data.put(EXPIRY_CONVENTION, expiryConventionId.toString());
      jsonObject.remove("data");
      jsonObject.put("data", data);
      toParse = jsonObject.toString();
    } catch (final JSONException e) {
      throw new IllegalArgumentException(e);
    }
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
    return toJSON(new InterestRateFutureConvention("", ExternalIdBundle.EMPTY,
        ExternalId.of("CONVENTION", IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME), EMPTY_EID, EMPTY_EID));
  }

}
