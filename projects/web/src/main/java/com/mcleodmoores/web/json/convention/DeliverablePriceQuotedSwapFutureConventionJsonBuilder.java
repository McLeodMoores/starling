/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.io.StringReader;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.expirycalc.IMMFutureAndFutureOptionQuarterlyExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Custom JSON builder to convert a {@link DeliverablePriceQuotedSwapFutureConvention} to JSON and back again.
 */
public final class DeliverablePriceQuotedSwapFutureConventionJsonBuilder extends ConventionJsonBuilder<DeliverablePriceQuotedSwapFutureConvention> {
  private static final String SWAP_CONVENTION = "swapConvention";
  private static final String EXPIRY_CONVENTION = "expiryConvention";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public DeliverablePriceQuotedSwapFutureConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  DeliverablePriceQuotedSwapFutureConvention fromJson(final String json, final Map<String, String> attributes) {
    String toParse = replaceUnderlyingConventionName(json, SWAP_CONVENTION, SwapConvention.TYPE, _conventionMaster);
    try {
      final StringReader sr = new StringReader(toParse);
      final JSONObject jsonObject = new JSONObject(new JSONTokener(sr));
      final JSONObject data = jsonObject.getJSONObject("data");
      final ExternalId rollDateConventionId = ExternalId.of("CONVENTION", data.getString(EXPIRY_CONVENTION));
      data.remove(EXPIRY_CONVENTION);
      data.put(EXPIRY_CONVENTION, rollDateConventionId.toString());
      jsonObject.remove("data");
      jsonObject.put("data", data);
      toParse = jsonObject.toString();
    } catch (final JSONException e) {
      throw new IllegalArgumentException(e);
    }
    final DeliverablePriceQuotedSwapFutureConvention convention = fromJSON(DeliverablePriceQuotedSwapFutureConvention.class, toParse);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  DeliverablePriceQuotedSwapFutureConvention getCopy(final DeliverablePriceQuotedSwapFutureConvention convention) {
    return convention.clone();
  }

  @Override
  public String toJSON(final DeliverablePriceQuotedSwapFutureConvention convention) {
    return toJSONWithUnderlyingConvention(convention, convention.getSwapConvention(), _conventionMaster);
  }

  @Override
  public String getTemplate() {
    return toJSON(new DeliverablePriceQuotedSwapFutureConvention("", ExternalIdBundle.EMPTY,
        ExternalId.of("CONVENTION", IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME), EMPTY_EID, EMPTY_EID, 250000));
  }

}
