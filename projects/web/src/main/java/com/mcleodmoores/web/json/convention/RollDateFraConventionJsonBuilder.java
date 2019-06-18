/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.io.StringReader;
import java.util.Map;

import org.fudgemsg.MutableFudgeMsg;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Custom JSON builder to convert a {@link RollDateFRAConvention} to JSON and back again.
 */
public class RollDateFraConventionJsonBuilder extends ConventionJsonBuilder<RollDateFRAConvention> {
  private static final String INDEX_CONVENTION = "indexConvention";
  private static final String ROLL_DATE_CONVENTION_FIELD = "rollDateConvention";
  private static final String ROLL_DATE_CONVENTION_NAME = "rollDateConventionName";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public RollDateFraConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  public String getTemplate() {
    return toJSON(new RollDateFRAConvention("", ExternalIdBundle.EMPTY, EMPTY_EID, EMPTY_EID));
  }

  @Override
  RollDateFRAConvention fromJson(final String json, final Map<String, String> attributes) {
    String toParse = replaceUnderlyingConventionName(json, INDEX_CONVENTION, IborIndexConvention.TYPE, _conventionMaster);
    try {
      final StringReader sr = new StringReader(toParse);
      final JSONObject jsonObject = new JSONObject(new JSONTokener(sr));
      final JSONObject data = jsonObject.getJSONObject("data");
      final ExternalId rollDateConventionId = ExternalId.of("CONVENTION", data.getString(ROLL_DATE_CONVENTION_NAME));
      data.remove(ROLL_DATE_CONVENTION_FIELD);
      data.put(ROLL_DATE_CONVENTION_FIELD, rollDateConventionId.toString());
      jsonObject.remove("data");
      jsonObject.put("data", data);
      toParse = jsonObject.toString();
    } catch (final JSONException e) {
      throw new IllegalArgumentException(e);
    }
    final RollDateFRAConvention convention = fromJSON(RollDateFRAConvention.class, toParse);
    convention.setAttributes(attributes);
    return convention;
  }

  @Override
  public String toJSON(final RollDateFRAConvention convention) {
    final MutableFudgeMsg newMsg = getFudgeSerializer().newMessage();
    addAttributes(convention, newMsg);
    addConventionName(convention.getIndexConvention(), _conventionMaster, newMsg);
    newMsg.add(ROLL_DATE_CONVENTION_NAME, convention.getRollDateConvention().getValue());
    return convertToJson(newMsg);
  }

  @Override
  RollDateFRAConvention getCopy(final RollDateFRAConvention convention) {
    return convention.clone();
  }

}
