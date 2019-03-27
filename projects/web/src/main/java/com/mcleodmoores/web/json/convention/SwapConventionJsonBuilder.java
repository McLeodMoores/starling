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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Custom JSON builder to convert a {@link SwapConvention} to JSON and back again.
 */
public class SwapConventionJsonBuilder extends ConventionJsonBuilder<SwapConvention> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SwapConventionJsonBuilder.class);
  private static final String PAY_LEG_FIELD_NAME = "payLegConvention";
  private static final String PAY_LEG_CONVENTION_NAME = "payLegConventionName";
  private static final String RECEIVE_LEG_FIELD_NAME = "receiveLegConvention";
  private static final String RECEIVE_LEG_CONVENTION_NAME = "receiveLegConventionName";
  private final ConventionMaster _conventionMaster;

  /**
   * @param conventionMaster
   *          a convention master, not null
   */
  public SwapConventionJsonBuilder(final ConventionMaster conventionMaster) {
    _conventionMaster = ArgumentChecker.notNull(conventionMaster, "conventionMaster");
  }

  @Override
  public String getTemplate() {
    return toJSON(new SwapConvention("", ExternalIdBundle.EMPTY, EMPTY_EID, EMPTY_EID));
  }

  @Override
  SwapConvention fromJson(final String json, final Map<String, String> attributes) {
    // TODO temporary fix - only the name of the underlying convention is returned from the GUI
    final String toParse = json;
    try {
      final StringReader sr = new StringReader(toParse);
      final JSONObject jsonObject = new JSONObject(new JSONTokener(sr));
      final JSONObject data = jsonObject.getJSONObject("data");
      if (data != null) {
        try {
          // can the field be parsed as an external id
          ExternalId.parse(data.getString(PAY_LEG_FIELD_NAME));
        } catch (final IllegalArgumentException e) {
          // if not, use the convention name to get the id from the master
          final String conventionName;
          if (data.has(PAY_LEG_CONVENTION_NAME)) {
            conventionName = data.getString(PAY_LEG_FIELD_NAME);
          } else if (data.has(PAY_LEG_FIELD_NAME)) {
            conventionName = data.getString(PAY_LEG_FIELD_NAME);
          } else {
            conventionName = null;
          }
          data.remove(PAY_LEG_FIELD_NAME);
          final ConventionSearchRequest request = new ConventionSearchRequest();
          request.setName(conventionName);
          final ConventionSearchResult result = _conventionMaster.search(request);
          if (result.getConventions().size() == 1) {
            final ExternalId conventionId = result.getSingleConvention().getExternalIdBundle().iterator().next();
            data.put(PAY_LEG_FIELD_NAME, conventionId.toString());
          }
          jsonObject.remove("data");
          jsonObject.put("data", data);
        }
        try {
          // can the field be parsed as an external id
          ExternalId.parse(data.getString(RECEIVE_LEG_FIELD_NAME));
        } catch (final IllegalArgumentException e) {
          // if not, use the convention name to get the id from the master
          final String conventionName;
          if (data.has(RECEIVE_LEG_CONVENTION_NAME)) {
            conventionName = data.getString(RECEIVE_LEG_FIELD_NAME);
          } else if (data.has(RECEIVE_LEG_FIELD_NAME)) {
            conventionName = data.getString(RECEIVE_LEG_FIELD_NAME);
          } else {
            conventionName = null;
          }
          data.remove(RECEIVE_LEG_FIELD_NAME);
          final ConventionSearchRequest request = new ConventionSearchRequest();
          request.setName(conventionName);
          final ConventionSearchResult result = _conventionMaster.search(request);
          if (result.getConventions().size() == 1) {
            final ExternalId conventionId = result.getSingleConvention().getExternalIdBundle().iterator().next();
            data.put(RECEIVE_LEG_FIELD_NAME, conventionId.toString());
          }
          jsonObject.remove("data");
          jsonObject.put("data", data);
        }
      }
      final SwapConvention convention = fromJSON(SwapConvention.class, jsonObject.toString());
      convention.setAttributes(attributes);
      return convention;
    } catch (final JSONException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  SwapConvention getCopy(final SwapConvention convention) {
    return convention.clone();
  }

  @Override
  public String toJSON(final SwapConvention convention) {
    final MutableFudgeMsg newMsg = getFudgeSerializer().newMessage();
    addAttributes(convention, newMsg);
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.ANY, convention.getPayLegConvention()));
    try {
      final ConventionSearchResult result = _conventionMaster.search(request);
      if (result.getConventions().size() == 1) {
        final String legConventionName = result.getSingleConvention().getName();
        newMsg.add(PAY_LEG_CONVENTION_NAME, legConventionName);
      }
    } catch (final DataNotFoundException e) {
      // log but ignore this for now
      LOGGER.warn("Could not get any convention for request {}", request);
    }
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.ANY, convention.getReceiveLegConvention()));
    try {
      final ConventionSearchResult result = _conventionMaster.search(request);
      if (result.getConventions().size() == 1) {
        final String legConventionName = result.getSingleConvention().getName();
        newMsg.add(RECEIVE_LEG_CONVENTION_NAME, legConventionName);
      }
    } catch (final DataNotFoundException e) {
      // log but ignore this for now
      LOGGER.warn("Could not get any convention for request {}", request);
    }
    return convertToJson(newMsg);

  }
}
